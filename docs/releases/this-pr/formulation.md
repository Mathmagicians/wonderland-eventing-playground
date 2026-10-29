# Formulation

Each feature's scenarios and architecture rules, by link, and the step skeletons, for the features of the
[catalog](release-0.0.8.md) that passed Examples agreed. The evidence of Spec signed, the red run, is linked from the
feature's row in the catalog.

## Architecture discovery: one model, three levels

| Task | Owner | Done when |
|---|---|---|
| The purse's architecture discovery feature, `the_purses_hexagon.feature`, and its `purse.textproto`, the DSL as wished | themathmagician, pair secretary | done 2026-10-29: `make bdd-snippets` in `examples/purse`, 11 steps undefined, the red run |
| `hexagon.proto`, the grammar of the DSL, in architecture-discovery | pair | the purse's `.textproto` parses |
| Step skeletons for the hexagon's steps in the library and Wonderland's in the platform, each throwing `PendingException` | pair | `make arch-verify` finds every step defined |

## The skill generate-platform-client

| Task | Owner | Done when |
|---|---|---|
| `examples/white-rabbit`: `the_white_rabbit_has_no_time_to_haggle.feature`, `the_white_rabbits_hexagon.feature`, and `white-rabbit.textproto`, written before the code | themathmagician, pair secretary | done 2026-10-29; they run red once the folder has a build |

## Stories describe themselves

| Task | Owner | Done when |
|---|---|---|
| An architecture rule: every `Story` carries `@PrimaryPort` with a description | themathmagician | it fails on today's stories |
| Unit tests for each story's `publishes()` | themathmagician | they fail on today's `Story` |
