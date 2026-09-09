(() => {
  "use strict";

  const form = document.querySelector("#evaluation-form");
  const casesContainer = document.querySelector("#cases");
  const caseTemplate = document.querySelector("#case-template");
  const addCaseButton = document.querySelector("#add-case");
  const resetButton = document.querySelector("#reset-button");
  const runButton = document.querySelector("#run-button");
  const formMessage = document.querySelector("#form-message");
  const resultsPanel = document.querySelector("#results-panel");
  const emptyState = document.querySelector("#empty-state");
  const loadingState = document.querySelector("#loading-state");
  const loadingDetail = document.querySelector("#loading-detail");
  const resultsContent = document.querySelector("#results-content");
  const runStatus = document.querySelector("#run-status");

  const example = {
    promptName: "support-triage",
    variableName: "ticket",
    promptContent: "Classify this support request: {{ticket}}",
    datasetName: "support-cases",
    cases: [
      { input: "Cannot sign in", expected: "Classify this support request: Cannot sign in" },
      { input: "My invoice is incorrect", expected: "Billing issue" }
    ]
  };

  function addCase(values = { input: "", expected: "" }) {
    const fragment = caseTemplate.content.cloneNode(true);
    const card = fragment.querySelector(".case-card");
    card.querySelector(".case-input").value = values.input;
    card.querySelector(".case-expected").value = values.expected;
    card.querySelector(".remove-case").addEventListener("click", () => {
      card.remove();
      renumberCases();
    });
    casesContainer.append(fragment);
    renumberCases();
  }

  function renumberCases() {
    const cards = [...casesContainer.querySelectorAll(".case-card")];
    cards.forEach((card, index) => {
      const number = index + 1;
      const inputId = `case-${number}-input`;
      const expectedId = `case-${number}-expected`;
      card.querySelector(".case-number").textContent = number;
      card.querySelector(".case-input").id = inputId;
      card.querySelector(".case-input-label").htmlFor = inputId;
      card.querySelector(".case-input-label").textContent = `Input value for case ${number}`;
      card.querySelector(".case-expected").id = expectedId;
      card.querySelector(".case-expected-label").htmlFor = expectedId;
      card.querySelector(".case-expected-label").textContent = `Expected output for case ${number}`;
      const removeButton = card.querySelector(".remove-case");
      removeButton.disabled = cards.length === 1;
      removeButton.hidden = cards.length === 1;
    });
  }

  function resetExample() {
    form.reset();
    document.querySelector("#prompt-name").value = example.promptName;
    document.querySelector("#variable-name").value = example.variableName;
    document.querySelector("#prompt-content").value = example.promptContent;
    document.querySelector("#dataset-name").value = example.datasetName;
    casesContainer.replaceChildren();
    example.cases.forEach(addCase);
    clearMessage();
    showEmpty();
  }

  function clearMessage() {
    formMessage.hidden = true;
    formMessage.textContent = "";
  }

  function showMessage(message) {
    formMessage.textContent = message;
    formMessage.hidden = false;
    formMessage.focus();
  }

  function setStatus(label, style) {
    runStatus.textContent = label;
    runStatus.className = `status-pill ${style}`;
  }

  function showEmpty() {
    resultsPanel.setAttribute("aria-busy", "false");
    emptyState.hidden = false;
    loadingState.hidden = true;
    resultsContent.hidden = true;
    setStatus("Ready", "neutral");
  }

  function showLoading(detail) {
    resultsPanel.setAttribute("aria-busy", "true");
    emptyState.hidden = true;
    loadingState.hidden = false;
    resultsContent.hidden = true;
    loadingDetail.textContent = detail;
    setStatus("Running", "running");
  }

  function setLoadingDetail(detail) {
    loadingDetail.textContent = detail;
  }

  async function postJson(path, body) {
    const response = await fetch(path, {
      method: "POST",
      headers: { "Content-Type": "application/json", "Accept": "application/json" },
      body: JSON.stringify(body)
    });

    if (!response.ok) {
      let detail = "The application could not complete this step.";
      try {
        const problem = await response.json();
        detail = problem.detail || problem.message || detail;
      } catch (_) {
        // Keep the useful fallback for non-JSON server errors.
      }
      throw new Error(`${detail} (HTTP ${response.status})`);
    }
    return response.json();
  }

  function collectCases() {
    const variableName = document.querySelector("#variable-name").value.trim();
    return [...casesContainer.querySelectorAll(".case-card")].map(card => ({
      inputVariables: { [variableName]: card.querySelector(".case-input").value },
      expectedOutput: card.querySelector(".case-expected").value
    }));
  }

  function element(tag, className, text) {
    const node = document.createElement(tag);
    if (className) node.className = className;
    if (text !== undefined) node.textContent = text;
    return node;
  }

  function renderResultItem(result, expectedOutput) {
    const details = element("details", "result-item");
    const summary = element("summary");
    const icon = element("span", `result-icon ${result.passed ? "pass" : "fail"}`, result.passed ? "✓" : "×");
    icon.setAttribute("aria-hidden", "true");
    const name = element("span", "result-name", `Case ${result.caseNumber}`);
    const status = element("span", "result-status", `${result.passed ? "Passed" : "Failed"} · ${result.latencyMs} ms`);
    summary.append(icon, name, status);

    const detail = element("div", "result-detail");
    detail.append(
      element("p", "output-label", "Provider output"),
      element("pre", "output-value", result.providerOutput),
      element("p", "output-label", "Expected output"),
      element("p", "expected-value", expectedOutput)
    );
    details.append(summary, detail);
    return details;
  }

  function renderResults(run, submittedCases) {
    const results = run.results || [];
    const passed = results.filter(result => result.passed).length;
    const percentage = Math.round((run.passRate || 0) * 100);
    const totalLatency = results.reduce((sum, result) => sum + result.latencyMs, 0);

    document.querySelector("#pass-rate").textContent = `${percentage}%`;
    document.querySelector("#score-ring").style.setProperty("--score", `${percentage * 3.6}deg`);
    document.querySelector("#score-summary").textContent = `${passed} of ${results.length} ${results.length === 1 ? "case" : "cases"} passed`;
    document.querySelector("#run-meta").textContent = `Run ${run.id}`;
    document.querySelector("#total-latency").textContent = `${totalLatency} ms total latency`;

    const list = document.querySelector("#result-list");
    list.replaceChildren();
    results.forEach((result, index) => list.append(renderResultItem(result, submittedCases[index]?.expectedOutput || "")));

    resultsPanel.setAttribute("aria-busy", "false");
    loadingState.hidden = true;
    resultsContent.hidden = false;
    setStatus(run.status === "COMPLETED" ? "Complete" : run.status, "complete");
  }

  async function runEvaluation(event) {
    event.preventDefault();
    clearMessage();
    if (!form.reportValidity()) return;

    const submittedCases = collectCases();
    runButton.disabled = true;
    runButton.querySelector(".button-label").textContent = "Running locally…";
    showLoading("Creating prompt");

    try {
      const template = await postJson("/api/prompt-templates", {
        name: document.querySelector("#prompt-name").value.trim(),
        content: document.querySelector("#prompt-content").value
      });
      const promptVersion = template.versions[template.versions.length - 1];

      setLoadingDetail("Creating dataset");
      const dataset = await postJson("/api/evaluation-datasets", {
        name: document.querySelector("#dataset-name").value.trim(),
        cases: submittedCases
      });

      setLoadingDetail("Evaluating cases with the local provider");
      const run = await postJson("/api/evaluation-runs", {
        promptVersionId: promptVersion.id,
        datasetId: dataset.id
      });
      renderResults(run, submittedCases);
    } catch (error) {
      resultsPanel.setAttribute("aria-busy", "false");
      loadingState.hidden = true;
      emptyState.hidden = false;
      setStatus("Error", "error");
      showMessage(`Evaluation failed: ${error.message}`);
    } finally {
      runButton.disabled = false;
      runButton.querySelector(".button-label").textContent = "Run deterministic evaluation";
    }
  }

  addCaseButton.addEventListener("click", () => {
    addCase();
    casesContainer.lastElementChild.querySelector(".case-input").focus();
  });
  resetButton.addEventListener("click", resetExample);
  form.addEventListener("submit", runEvaluation);
  resetExample();
})();
