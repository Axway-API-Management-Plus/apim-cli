# AGENTS.md

## What this repo is
- Multi-module Maven Java CLI for Axway API Manager; root module graph is in `pom.xml` (`modules/*` + `distribution`).
- Core behavior is "desired state -> actual state" sync ("Everything as code") from config files to API Manager (`README.md`).

## Architecture map (read these first)
- CLI bootstrap and command dispatch: `modules/apim-cli/src/main/java/com/axway/apim/cli/APIManagerCLI.java`.
- Plugin contract for command providers: `modules/apim-cli/src/main/java/com/axway/apim/cli/APIMCLIServiceProvider.java`.
- Service registration uses Java `ServiceLoader` files at `modules/*/src/main/resources/META-INF/services/com.axway.apim.cli.APIMCLIServiceProvider`.
- API management command flow example: `modules/apis/src/main/java/com/axway/apim/APIImportApp.java`.
- Remote system abstraction (HTTP/API Manager integration) lives in `modules/apim-adapter`.
- Shared test utilities and integration framework support live in `modules/apim-cli-tests`.

## How commands are added here (project-specific pattern)
- Implement `APIMCLIServiceProvider` in the owning module (example: `APIImportApp`).
- Expose each CLI action with `@CLIServiceMethod(name = "...", description = "...")`.
- Register provider class name in the module ServiceLoader file under `META-INF/services/...APIMCLIServiceProvider`.
- Group routing is based on `getGroupId()`; usage output comes from `getGroupDescription()`.

## Build and test workflows
- Baseline build (unit tests only by default):
```bash
mvn clean verify
```
- Integration tests profile (disables unit tests, enables failsafe ITs):
```bash
mvn clean verify -Pintegration-tests
```
- Skip all tests profile:
```bash
mvn clean verify -Pno-tests
```
- Distribution artifacts are produced by module `distribution` (see `distribution/target/*.tar.gz` and `*.zip`).
- Java target is 11 at parent level (`pom.xml`), while `apim-cli-tests` compiles for Java 8 (`modules/apim-cli-tests/pom.xml`).

## Integration and runtime assumptions
- API Manager access credentials/host are supplied via environment property files (sample: `examples/conf/env.api-env.properties`).
- API/app desired state payloads are JSON/YAML config files (see `examples/basic`, `examples/complex`, `examples/orgsAndApps`).
- Adapter dependencies indicate supported API styles/formats: Swagger/OpenAPI parser, OData (v2/v4), GraphQL, Jackson YAML/XML, HTTP client (`modules/apim-adapter/pom.xml`).

## Testing + CI nuances
- Unit tests run via Surefire; integration tests via Failsafe with explicit exclusions in root `pom.xml`.
- Integration tests are Docker/API-Manager based; setup expectations and required GitHub secrets are documented in `Integration_test_setup.md`.
- For config-driven behavior changes, prefer adding/adjusting scenario-style tests in `modules/*/src/test` using patterns from existing ITs.

## Release workflow anchors
- Release process is branch-based (`develop` -> `master`) and documented in `DEVELOPMENT.md`.
- Release profile (`-Prelease`) enables sources/javadocs/GPG signing/Nexus staging plugins in root `pom.xml`.

## Existing AI guidance in repo
- No dedicated AI instruction files were found in the searched convention paths; `README.md` is the primary project-level guidance.

