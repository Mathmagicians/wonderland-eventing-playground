# Verification

The release gate, **In production**: every feature the [catalog](release-spec-0.0.7.md) ships Spec green, the
release checks passed, and Wonderland playing in each of its environments.

## Release checks

| Check | Evidence | Signed |
|---|---|---|
| `make check` green | `make build` and `make bdd TRANSPORT=kafka` green locally on 2026-10-29, [Messaging capability](03_automation.md#messaging-capability-cli-kafka-confluent) | 2026-10-29 |
| `ci`, `cd`, `iac`, `architecture-discovery`, `cicd-client-example` green on the PR | [#21](https://github.com/Mathmagicians/wonderland-eventing-playground/pull/21), merged into the protected `main` on 2026-10-29; `iac` green with the plan disregarded, the Confluent environment gone | 2026-10-29 |
| Review findings above Nit resolved, or deferred with a reason | the leftovers of #7 listed under Open in [03_automation.md](03_automation.md#messaging-capability-cli-kafka-confluent), deferred | 2026-10-29 |
| README, `make help`, and `docs/adr.md` say what the code does | the README's Environments, Variables and secrets, Transports, Git and CI, and Roadmap of 2026-10-29 | 2026-10-29 |

## ADR compliance

| ADR | Checked by | Evidence |
|---|---|---|
| [ADR-001 CI plays each transport in a matrix](../../../adr.md#adr-001-ci-plays-each-transport-in-a-matrix) | `make ci-transports`, and review of `cicd.yaml` and `cicd-client-example.yaml` | `["cli","kafka"]`, `bdd (cli)` and `bdd (kafka)` on run 40914163544 |
| The transport is a profile beside the environment | the architecture rule of Formulation | the profiles `<transport>,<env>`, `kafka` with `confluent` as a group over it; the rule itself not written, [02_formulation.md](02_formulation.md#messaging-capability-cli-kafka-confluent) |

## In production

| Environment | Tag | Image | Wonderland plays | Signed |
|---|---|---|---|---|
| `test` | `v0.0.7` | `ghcr.io/mathmagicians/wonderland-eventing-playground:0.0.7`, the same bytes as `latest` | the `bdd (cli)` and `bdd (kafka)` jobs of run [40914163544](https://github.com/Mathmagicians/wonderland-eventing-playground/actions/runs/40914163544) in `cli-test` and `kafka-test` | 2026-10-29 |
| `prod` | `v0.0.7` | the same | `cli-prod`, the dispatch of 2026-10-21; `kafka-prod` waits for the universe on a runner, `confluent-prod` for credits | 2026-10-29 |
