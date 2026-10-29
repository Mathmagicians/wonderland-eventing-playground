# Verification

The release gate, **In production**: every feature the [catalog](release-0.0.8.md) ships Spec green, the release
checks passed, and Wonderland playing in each of its environments.

## Release checks

| Check | Evidence | Signed |
|---|---|---|
| `make check` green | | |
| `ci`, `cd`, `iac`, `architecture-discovery`, `cicd-client-example` green on the PR | | |
| Review findings above Nit resolved, or deferred with a reason | | |
| README, `make help`, and `docs/adr.md` say what the code does | | |
| `make arch-verify` green in the root build and in `examples/purse`; `make arch-check` exact on every `.textproto` | | |

## ADR compliance

| ADR | Checked by | Evidence |
|---|---|---|
| [ADR-001 CI plays each transport in a matrix](../../adr.md#adr-001-ci-plays-each-transport-in-a-matrix) | `make ci-transports`, and review of `cicd.yaml` and `cicd-client-example.yaml` | |
| The DSL is Protobuf text written from the generated PlantUML, verified by Gherkin, proposed | `make arch-verify` and `make arch-check` | |

## In production

| Environment | Tag | Image | Wonderland plays | Signed |
|---|---|---|---|---|
| `test` | | | | |
| `prod` | | | | |
