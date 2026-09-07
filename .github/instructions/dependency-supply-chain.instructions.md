---
applyTo: "pom.xml,**/pom.xml,Dockerfile,.github/workflows/**,.github/dependabot.yml,devbox.json,devbox.lock,trekktabell.yaml"
---

# Dependency update review: supply chain threat model

These instructions apply to dependency bumps (Dependabot or manual). Treat every
dependency change as a potential supply chain attack until the evidence says
otherwise. Version numbers alone prove nothing.

## Always report

Flag and explain any of the following. Prefer a false positive over a silent miss.

### 1. Provenance of the new version

- New version published very recently (days) relative to the previous release cadence,
  or a burst of releases after a long dormant period.
- Maintainer, organisation, or repository ownership changed between the two versions.
- The artifact's `groupId`/namespace, repository URL, or homepage changed.
- Release is not reproducible from a public tag, or the tag does not exist upstream.
- Package renamed, re-published under a new coordinate, or resurrected from a deprecated
  or archived project.

### 2. Typosquatting and dependency confusion

- Coordinate is a near-miss of a well-known package (character swaps, hyphen/underscore
  changes, added prefixes/suffixes such as `-core`, `-js`, `2`).
- A dependency that was previously internal/private now resolves from a public registry.
- A new registry, mirror, or `<repository>` entry is added to `pom.xml`, or an existing
  one is repointed to a non-canonical host.

### 3. Version and integrity pinning

- An immutable pin is replaced by a mutable reference:
  - GitHub Actions: full commit SHA replaced by a tag or branch (`@v4`, `@main`).
    Actions in `.github/workflows/**` that are already SHA-pinned must stay SHA-pinned,
    and third-party actions (anything outside `actions/`, `docker/`, `github/`) should be
    SHA-pinned.
  - Docker: `image@sha256:...` digest replaced by a floating tag such as `latest`.
    `Dockerfile` and `trekktabell.yaml` images are expected to stay digest-pinned.
  - Maven: fixed version replaced by a range, `LATEST`, `RELEASE`, or a `-SNAPSHOT`.
- A checksum, digest, or lock entry changed without a corresponding version change.

### 4. Blast radius of the change

- The diff does more than change a version: added plugins, new `<executions>`,
  `maven-antrun-plugin`, `exec-maven-plugin`, new build/test hooks, new
  `.github/workflows` steps, or changes to `Dockerfile` `RUN`/`ENTRYPOINT`/`CMD`.
- Any step that fetches and executes remote content (`curl … | sh`, `wget`, arbitrary
  script downloads) or writes outside the build directory.
- New or widened workflow `permissions`, new use of `pull_request_target`, or a secret
  being exposed to a job that runs untrusted code.
- Transitive dependency count jumps sharply, or a new transitive dependency appears that
  is unrelated to the stated purpose of the update.

### 5. Runtime and network behaviour

- New network egress, telemetry, analytics, or "update check" behaviour.
- Obfuscated, minified, or base64/hex-encoded blobs introduced in source or config.
- Post-install / build-time code execution added by the dependency.
- Native binaries or pre-built artifacts checked into the repository.

### 6. Known-bad signals

- The version is affected by a published advisory (GHSA/CVE), or the update claims to
  fix a vulnerability but the referenced advisory does not exist.
- Release notes or changelog are missing, empty, auto-generated with no substance, or
  do not match the diff.
- The upstream project has been reported as compromised.

## Repository-specific expectations

- Java 17, Maven build; runtime dependencies are Undertow and Jackson. A new *direct*
  dependency in `pom.xml` on a Dependabot PR is unexpected — call it out.
- `Dockerfile` uses Chainguard base images; a base image swap to a different publisher is
  a finding, not a nit.
- `trekktabell.yaml` deployment images must remain pinned to `sha256:` digests.
- Do not comment on formatting, changelog wording, or version-number style.

## Output format

Structure the review as:

1. **Verdict** — one of `Looks routine`, `Needs a human look`, `Do not merge`.
2. **Findings** — bullet list, each with the concrete evidence from the diff.
3. **Checks the reviewer should run manually** — only if something cannot be verified
   from the diff (e.g. "compare the upstream release tag against the published artifact").

If the diff is a clean, single-line version bump of a well-known artifact with no other
changes, say so briefly and do not manufacture concerns.
