# Formulation

Each feature's scenarios and architecture rules, by link, and the step skeletons, for the features of the
[catalog](release-0.0.7.md) that passed Examples agreed. The evidence of Spec signed, the red run, is linked from the
feature's row in the catalog.

## Messaging capability: cli, kafka, confluent

| Task | Owner | Done when |
|---|---|---|
| The map's scenarios in `platform/src/test/resources/features/i_can_pick_where_a_story_plays.feature` | themathmagician | each example of the map is a scenario |
| An architecture rule: the Kafka adapters carry `@Profile("kafka")`, the console and log adapters `@Profile("cli")` | themathmagician | it fails on today's `@Profile("confluent")` |
| Step skeletons for the new steps, each throwing `PendingException` | pair | `make bdd-snippets` finds every step defined |

## The catalog follows the board

| Task | Owner | Done when |
|---|---|---|
| The map's scenarios in [the_catalog_follows_the_board.feature](../../../src/test/resources/features/the_catalog_follows_the_board.feature), `@manual` | pair | each example of the map is a scenario |

## A release ships what is ready

| Task | Owner | Done when |
|---|---|---|
| The map's scenarios in [a_release_ships_what_is_ready.feature](../../../src/test/resources/features/a_release_ships_what_is_ready.feature), `@manual` | pair | each example of the map is a scenario |

## Stories describe themselves

| Task | Owner | Done when |
|---|---|---|
| An architecture rule: every `Story` carries `@PrimaryPort` with a description | themathmagician | it fails on today's stories |
| Unit tests for each story's `publishes()` | themathmagician | they fail on today's `Story` |

## Architecture discovery: one model, three levels

| Task | Owner | Done when |
|---|---|---|
| A test of the `model.dsl` written from a fixture, in architecture-discovery | themathmagician | it fails on today's writer |
| `make arch-check`, exact | pair | it fails on a reordered line |

## The skill generate-platform-client

| Task | Owner | Done when |
|---|---|---|
| The client's features and architecture classes, the acceptance it turns green | themathmagician | they run red on an empty client |

## Load on demand: the transport and its environment

| Task | Owner | Done when |
|---|---|---|
| The spec: the Git and CI bullet on `load-run.yaml` in the README, and `.github/environment-test.json` and `environment-prod.json`, applied by `make gh-main-protection` | pair | themathmagician signs it |
| The red run: a dispatch from `eventing-without-confluent` with `transport=kafka`, in the empty `kafka-test` | pair | the run shown, red at startup |

Spec signed 2026-10-21. The run,
[39508585975](https://github.com/Mathmagicians/wonderland-eventing-playground/actions/runs/39508585975), from
`eventing-without-confluent` with `transport=kafka`, deployed to `kafka-test` with `ENV=test` and skipped the load:
the registry holds no `latest` under the new image name until `main` builds it.

## CI plays each transport

Spec signed 2026-10-22: `make bdd` on cli, red before `Transport`.

| Task | Owner | Done when |
|---|---|---|
| `@confluent` on the cluster's and the Flink table's features | pair | done 2026-10-22: `make bdd-snippets TRANSPORT=cli` lists none of them |
| The generator's, the tea party's, and the purse's scenarios play on cli as written, the drivers following the transport | pair | done 2026-10-22: `make bdd-snippets TRANSPORT=cli` lists them, every step defined |

## Wonderland's lines: what a feature reuses from the platform

**Spec:** [wonderlands_lines.feature](../../../src/test/resources/features/wonderlands_lines.feature), written
2026-10-27. Spec signed 2026-10-28: `make bdd-snippets SCENARIOS=@manual` lists every step of its 19 scenarios
undefined, the red run.

| Task | Owner | Done when |
|---|---|---|
| The map's scenarios in the feature file: the tea party's summary row, every line glued where its opening says, a line glued against its opening named | themathmagician | done 2026-10-27 |
| The root's `bdd` plays the features of the repository: `RunCucumberTest` at the root, the platform's fixtures on its test classpath without `cucumber-spring`, played after the subprojects' | pair | done 2026-10-28: the red run above |
| The five features rewritten to ADR-002, one file at a time: the purse's, the tea party's, the generator's, the cluster's, the Flink table's | themathmagician | `make bdd-snippets` lists every Wonderland step as undefined, the red run |
| An architecture rule: none; ADR-002 is enforced by `make bdd-reuse` and review | | |
