# Architecture decisions

The decisions that shape the architecture, one entry each, numbered in the order they are written. An entry keeps its
text; a decision that replaces it is a new entry, and the earlier entry's status names it.

## Format

```
### ADR-NNN Title

- **Date:** the day of its status
- **Status:** Proposed, Accepted, or Superseded by ADR-NNN
- **Context:** the facts that call for the decision
- **Decision:** what we do
- **Consequences:** what follows from it
- **Enforced by:** the architecture rule or test that checks it, or review
```

## Decisions

### ADR-001 CI plays each transport in a matrix

- **Date:** 2026-10-22
- **Status:** Accepted
- **Context:** A process plays a transport beside its environment, `<transport>,<env>`. Confluent Cloud answers the
  keys of `confluent-test` and `confluent-prod` with `SaslAuthenticationException` since 2026-09-29, and a run on it
  is billed. Branch protection requires `ci`, `cd`, `iac`, `architecture-discovery`, and `cicd-client-example` by
  name, and a job with a matrix reports a check per entry, `bdd (cli)`.
- **Decision:** The `bdd` job of `cicd.yaml` plays the features against the candidate once per transport of
  `CI_TRANSPORTS` in `platform.mk`, read through `make ci-transports`, each entry in its `<transport>-test`
  environment. `cd` follows `ci` and `bdd` and publishes the tested candidate. A transport that sits out carries its
  reason beside `CI_TRANSPORTS`: `confluent` for Confluent Cloud's pricing, `kafka` until its broker and schema
  registry run on the runner, `cloudflare` until it plays. Each transport plays the scenarios it carries; a feature
  that only one transport carries is tagged with it, `@confluent`.
- **Consequences:** `ci` and `cd` keep the names branch protection requires. A transport enters CI by its name in
  `CI_TRANSPORTS`. On `cli` the features of the cluster and the Flink table sit out; they play on `confluent` from a
  developer machine. `cicd-client-example.yaml` plays the purse on `cli` in `cli-test`.
- **Enforced by:** `make ci-transports`, and review of `cicd.yaml` and `cicd-client-example.yaml`.

### ADR-002 A platform step opens with Wonderland and names its hexagon element

- **Date:** 2026-10-24
- **Status:** Accepted
- **Context:** The platform ships the shared steps of the BDD suite, and a client's feature mixes them with its
  own. A reader cannot tell which lines the client defines, and nothing measures how much of a feature the platform
  carries.
- **Decision:** A step the platform ships opens with `Wonderland` and names the element of the hexagon it drives,
  in the present tense: `Wonderland is reachable`, the rim; `Wonderland hears on the stream of <payloads>:`, what
  the story hears; `Wonderland's story <name> reads the stream of <payloads>`, the driving port; `Wonderland
  publishes <payloads> to the topic`, the driven port; `Wonderland has the topic`, the schema, the table, Confluent
  Cloud. A client's step never opens with `Wonderland`. `make bdd-reuse` counts the lines per element from
  `cucumber.json`, and the `bdd` job prints the table in its summary.
- **Consequences:** Every feature in the repository and the purse's are rewritten, the features first, then the
  step patterns. A line whose opening and glue disagree fails `make bdd`.
- **Enforced by:** `make bdd-reuse`, the check of opening against glue; review of the feature files.
