# Formulation

Each feature's scenarios and architecture rules, by link, and the step skeletons, for the features of the
[catalog](release-0.0.9.md) that passed Examples agreed.

## The catalog follows the board

| Task | Owner | Done when |
|---|---|---|
| The map's scenarios in [the_catalog_follows_the_board.feature](../../../src/test/resources/features/the_catalog_follows_the_board.feature), `@manual` | pair | each example of the map is a scenario |

## A release ships what is ready

| Task | Owner | Done when |
|---|---|---|
| The map's scenarios in [a_release_ships_what_is_ready.feature](../../../src/test/resources/features/a_release_ships_what_is_ready.feature), `@manual` | pair | each example of the map is a scenario |

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

## Eventing over Cloudflare

| Task | Owner | Done when |
|---|---|---|
| The map's scenarios, tagged `@cloudflare` | themathmagician | each example of the map is a scenario |
| An architecture rule: the Cloudflare adapters carry `@Profile("cloudflare")` | themathmagician | it fails on an adapter without it |
