# Verification

The release gate, **In production**: every feature the [catalog](release-0.0.8.md) ships Spec green, the release
checks passed, and Wonderland playing in each of its environments.

## Release checks

| Check | Evidence | Signed |
|---|---|---|
| `make check` green | not recorded | |
| `ci`, `cd`, `iac`, `architecture-discovery`, `cicd-client-example` green on the PR | [#24](https://github.com/Mathmagicians/wonderland-eventing-playground/pull/24): `ci`, `bdd (cli)`, `bdd (kafka)`, `cd` in run [41245135474](https://github.com/Mathmagicians/wonderland-eventing-playground/actions/runs/41245135474), `iac` [41245135481](https://github.com/Mathmagicians/wonderland-eventing-playground/actions/runs/41245135481), `architecture-discovery` [41245135340](https://github.com/Mathmagicians/wonderland-eventing-playground/actions/runs/41245135340), `cicd-client-example` [41245135392](https://github.com/Mathmagicians/wonderland-eventing-playground/actions/runs/41245135392) | 2026-11-12 |
| Review findings above Nit resolved, or deferred with a reason | #24 carries no review and no comment | |
| README, `make help`, and `docs/adr.md` say what the code does | every `make` target the README, the purse's README, and the library's document name is in `make help`, checked 2026-11-12; `docs/adr.md` holds ADR-001 and ADR-002, the DSL's ADR not written | |
| `make arch-verify` and `make arch-bdd` green in the root build and in `examples/purse`; `make arch-check` exact on every `.textproto` | the `ci` and `bdd` jobs of run 41245135474, and `cicd-client-example` 41245135392 | 2026-11-12 |

## ADR compliance

| ADR | Checked by | Evidence |
|---|---|---|
| [ADR-001 CI plays each transport in a matrix](../../../adr.md#adr-001-ci-plays-each-transport-in-a-matrix) | `make ci-transports`, and review of `cicd.yaml` and `cicd-client-example.yaml` | |
| The DSL is Protobuf text written from the generated PlantUML, verified by Gherkin, proposed | `make arch-bdd` and `make arch-check` | |

## In production

| Environment | Tag | Image | Wonderland plays | Signed |
|---|---|---|---|---|
| `test` | `v0.0.8` on `0af1868` | `0.0.8` and `latest` on one version in the registry, the `tag` job of run [43123727120](https://github.com/Mathmagicians/wonderland-eventing-playground/actions/runs/43123727120) | `bdd (cli)` and `bdd (kafka)` of that run, the deployments `cli-test` 7724374873 and `kafka-test` 7724374859 | 2026-11-12 |
| `prod` | `v0.0.8` on `0af1868` | the same | no `load-run.yaml` dispatch from `main` on this version | |
