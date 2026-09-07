# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Norwegian tax withholding table calculator (trekktabell) for 2026. Fork of Skatteetaten/trekktabell that provides a REST API for calculating tax withholding amounts. Calculates "forskuddstrekk" (advance tax deductions) based on tax tables, income amounts, and payment periods.

## Build Commands

```bash
mvn package          # Build JAR and run tests
mvn test             # Run tests only
mvn clean            # Clean build artifacts
```

Output artifacts:
- `target/trekkrutine-2026-0-SNAPSHOT.jar` - Main application
- `target/libs/` - Dependencies
- `target/bom.json` - SBOM

## Running the Application

```bash
java -jar target/trekkrutine-2026-0-SNAPSHOT.jar
```

REST API runs on port 8080. Example query:
```bash
curl 'http://localhost:8080/2026?table=7100&amount=50000'
```

## Architecture

### Core Calculation Flow

`Trekk.java` (REST API) → `Trekkrutine.java` (calculation engine) → returns withholding amount in øre

The calculation pipeline:
1. Round income to period-specific increment
2. Annualize the amount based on payment period
3. Calculate deductions via `Fradrag.java` (standard, minimum, seafarer, Finnmark)
4. Calculate taxes via `Skatteberegning.java` (municipal, state, progressive tiers, employer contributions)
5. Distribute back to payment period

### Key Classes

- **`Trekkrutine.java`** - Main calculation engine, entry point is `beregnTrekk()`
- **`Konstanter.java`** - 2026 tax rates, thresholds, and deduction limits (updated annually)
- **`Tabellnummer.java`** - Enum defining 100+ tax tables with their deduction percentages
- **`Periode.java`** - Enum for 7 payment periods (MAANED, UKE, DAG, etc.)
- **`Tabelltype.java`** - Enum for table types (VANLIG, STANDARDFRADRAG, SJO, FINNMARK, SPESIAL)
- **`Trekk.java`** - Undertow HTTP server exposing the REST API

### Package Structure

```
no.skatteetaten.fastsetting.formueinntekt.forskudd.trekkrutine2026/
  - Tax calculation logic and constants
no.twingine/
  - REST API server
```

## Testing

JUnit 4 tests in `TrekkrutineTest.java`. Key test patterns:
- Zero income returns zero withholding
- Withholding never exceeds gross income
- More deductions result in lower withholding
- Full table generation tests (`kontrollerHeleTabellenAlle`)

Run single test class:
```bash
mvn test -Dtest=TrekkrutineTest
```

## Annual Updates

When updating for a new tax year:
1. Update `Konstanter.java` with new tax rates and thresholds
2. Rename package from `trekkrutine2026` to new year
3. Update `pom.xml` artifact name
4. Update REST API endpoint in `Trekk.java`
5. Update `Dockerfile` CMD to reference the new JAR name (e.g., `trekkrutine-2027-0-SNAPSHOT.jar`)
6. Add new Deployment, Service, and HTTPRoute rule to `trekktabell.yaml` for the new year

## Deployment

Docker multi-stage build using Chainguard JRE runtime. Published to `packages.buildkite.com/asgeirn/registry/trekktabell`. Kubernetes manifests in `trekktabell.yaml`.

### CI/CD

Buildkite pipeline (`.buildkite/pipeline.yml`) runs:
1. Maven build and test (in Docker)
2. Multi-arch Docker build (linux/amd64, linux/arm64) and push to Buildkite OCI registry

Authentication to the registry uses OIDC via `buildkite-agent oidc request-token`. Tags pushed: `sha-<short>`, `latest`, and the year extracted from `pom.xml`.

### Dependabot Automation

All GitHub Actions are pinned to full commit SHAs with a trailing `# vX.Y.Z` comment,
including first-party `actions/*` and `github/*`. Dependabot has no option to convert a
tag into a SHA pin, so new actions must be pinned by hand; thereafter Dependabot updates
both the SHA and the comment. The comment format matters — the version must be the last
thing on the line. Never use a branch ref (`@main`): Dependabot ignores those entirely and
they get no security alerts.

`.github/dependabot.yml` applies `cooldown` periods so releases age before adoption
(7 days generally, 30 for Maven majors). Cooldown never delays security updates.

Dependabot PRs are handled by `.github/workflows/dependabot-review-merge.yml`:

1. Requests a review from `copilot-pull-request-reviewer[bot]`.
2. Waits for Copilot's verdict, then publishes update metadata (versions, update type,
   maintainer changes, advisory state) to the job summary via `dependabot/fetch-metadata`.
3. Enables auto-merge (squash) only when *all* of these hold: Copilot approved with zero
   inline comments, the diff stays within the dependency manifests, and Dependabot reports
   no maintainer change. Otherwise it comments on the PR explaining what is being held.

**Requires the `COPILOT_REVIEW_TOKEN` Actions secret** — a fine-grained PAT owned by a
Copilot-licensed user, scoped to this repository with:

| Permission | Access | Why |
| --- | --- | --- |
| Pull requests | Read and write | Request the review, comment, enable auto-merge |
| Contents | Read and write | Perform the merge |
| Workflows | Read and write | Merge PRs that modify `.github/workflows/**` |

The default `GITHUB_TOKEN` does *not* work for either half of this. Requesting a review with
it returns success but is silently discarded, because a Copilot review must be attributed to
a licensed identity. Merging with it fails outright on any PR touching a workflow file
(`refusing to allow a GitHub App to create or update workflow … without 'workflows'
permission`), which covers most `github-actions` ecosystem updates.

The `copilot_code_review` ruleset rule has the same attribution limitation — it never fires
on `dependabot[bot]`-authored PRs, only on human-authored ones.

Copilot's review is steered by `.github/instructions/dependency-supply-chain.instructions.md`,
which targets supply chain attack indicators. Copilot reads instruction files from the PR's
head branch, so changes to it can be validated in the same PR.

The workflow uses `pull_request_target` because Dependabot-triggered `pull_request` runs get
a read-only token and no secrets. It must never check out or execute head-branch code.

Auto-merge waits for the `build` and `Analyze (java-kotlin)` checks, which are required by
the branch ruleset (repository admins bypass it for direct pushes to `master`).

### Updating Image Digests

Images in `trekktabell.yaml` are pinned to SHA256 digests. After a new image is published, get the manifest list digest from the Buildkite build log:

```bash
bk job log <job-id> | grep 'exporting manifest list'
```

The digest appears as `sha256:...` in the log output. Update the corresponding deployment in `trekktabell.yaml` with the new digest.

## Available Tools

- **`bk`** - Buildkite CLI for managing builds, viewing logs, and triggering rebuilds. Use `echo Y | bk <command>` for commands requiring confirmation. Use `jq` to parse JSON output.
- **`yq`** - YAML processor for validating and querying YAML files.
- **`skopeo`** - Container image inspection tool (requires authentication for private registries).
