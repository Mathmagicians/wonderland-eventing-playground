# Formulation

Each work package's scenarios and architecture rules, by link, and the step skeletons, for the packages of the
[catalog](release-0.0.8.md) that passed Examples agreed. The evidence of Spec signed, the red run, is linked from
the package's row in the catalog.

## The purse's architecture feature passes

| Task | Owner | Done when |
|---|---|---|
| `the_purses_hexagon.feature` and `purse.textproto`, the DSL as wished | themathmagician, pair secretary | done 2026-10-29: `make bdd-snippets` in `examples/purse`, 11 steps undefined, the red run |
| Step skeletons for the hexagon's steps in the library's test fixtures and Wonderland's in the platform's, each throwing `PendingException` | pair | `make arch-verify` finds every step defined |

## architecture-discovery extracts the purse's .textproto

| Task | Owner | Done when |
|---|---|---|
| `hexagon.proto`, the grammar of the DSL, in `architecture-discovery/src/main/proto` | pair | `purse.textproto` parses through `TextFormat` |
| A test of the writer over a fixture PlantUML in architecture-discovery | pair | it fails on today's library |
| A test of `HexagonDocumenter` asserting no styling line | pair | it fails on today's writer |

## make arch-verify and make arch-check check the DSL

| Task | Owner | Done when |
|---|---|---|
| `make arch-check` on `docs/generated/architecture/model` | pair | red on a `.textproto` one line apart |
| `make arch-verify` playing `@architecture` | pair | red on the purse's undefined steps |

## The White Rabbit and the skill: the round trip

| Task | Owner | Done when |
|---|---|---|
| `the_white_rabbit_has_no_time_to_haggle.feature`, `the_white_rabbits_hexagon.feature`, and `white-rabbit.textproto`, written before the code | themathmagician, pair secretary | done 2026-10-29; they run red once the folder has a build |
| The skill's brief, from the spec | themathmagician | the brief names the rules, the steps, the features, the `.textproto`, the platform |
