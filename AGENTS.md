# Autonomous development policy

This repository is an explicitly approved target for local scheduled Pi runs.

- Work only in this repository during a run; never create, delete, transfer, rename, or change visibility of repositories.
- Create at most one small, meaningful PR per run. Do not make empty commits.
- Use an existing open issue; create one only for a concrete next vertical slice when no open issue exists.
- Use a feature branch, label issues and PRs `AI-assisted`, run `./mvnw --batch-mode --no-transfer-progress verify`, and open a PR only on success.
- Do not merge the PR; the local scheduler merges only after remote CI succeeds.
- Use only free/local tooling. Do not install software, access unrelated files, use paid APIs, or alter machine/account settings.
