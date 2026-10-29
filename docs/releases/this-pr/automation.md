# Automation

The tasks of each feature in the [catalog](release-0.0.8.md), each with its owner and the scenario it turns green,
and the core snippets. One go per change. The evidence of Spec green, the green run and the ADRs a feature touches,
is linked from the feature's row in the catalog.

## Architecture discovery: one model, three levels

The pipeline of the tool, architecture-discovery: discover, the code to PlantUML, as today; write, the PlantUML
to the DSL, one `.textproto` per story and one for the platform by `hexagon.proto`; verify, the architecture
discovery features on the steps the library and the platform offer; check, `make arch-check` exact on the DSL.
The tasks in order; the first is the purse's architecture discovery feature green, its `purse.textproto` matching
what the build writes.

| Task | Owner | Turns green |
|---|---|---|
| Clean the generation: `hexagon.puml` content only, packages for the rings and the modules, components with their stereotypes, arrows with their labels; the look in `hexagon.style.puml` in the library's resources, handed to PlantUML by `renderDiagrams`; Modulith's diagrams in its UML style | pair | `make arch-gen` draws the same hexagon, the `.puml` holds no styling line, `make arch-check` |
| Every architecture annotation of a module as a stereotype in `hexagon.puml`: `@AutoConfiguration`, `@EnableConfigurationProperties`, `@ConfigurationProperties` with its prefix, `@Bean` with what it builds | pair | the purse's `hexagon.puml` names its six annotations |
| `architecture-discovery/src/main/proto/hexagon.proto`, the package `architecture.model`, `proto-gen` covering the library | pair | `make build` |
| The writer: the PlantUML read into the model, printed as `docs/generated/architecture/model/<story>.textproto` and `platform.textproto`, in `make arch-gen`; `make arch-check` exact on them | pair | the root build writes `platform`, `load`, `tea-party`; the purse writes `purse` |
| Static `listensTo` and `publishes`, [Stories describe themselves](#stories-describe-themselves): the hexagon draws them, the DSL carries them | themathmagician | the tea party's file says hears Offer and Order, publishes Transaction |
| The links: the last `bdd` run's `cucumber.json` read by the writer, each arrow naming its scenario | pair | the tea party's arrows carry their lines; an arrow without one is listed |
| The offered Gherkin: the hexagon's steps in the library's test fixtures, Wonderland's on top in the platform's; `every_story_has_its_hexagon.feature` in the platform; `make arch-verify` plays `@architecture` without containers | steps and wiring pair, the features themathmagician | red on a story without its port, green on the platform and the purse |
| The three levels from the DSL: the forest, the streams, the hexagon of each; the slides' section 4 | pair | three SVGs per build |

## The skill generate-platform-client

The client for the demo: the White Rabbit Story, `stories.whiterabbit`, its port `NoTimeToHaggle`, hears `Offer`,
publishes `Order`; `examples/white-rabbit`, its features and its `white-rabbit.textproto` written before its code.

| Task | Owner | Turns green |
|---|---|---|
| The White Rabbit: the story, its behaviour feature `the_white_rabbit_has_no_time_to_haggle.feature`, its architecture discovery feature, `white-rabbit.textproto` | themathmagician | its `make check` and `make arch-verify` |
| The skill, from the architecture spec: the rules, the offered steps, the DSL, the published platform | themathmagician | the brief it yields |
| The offline run from `make platform-install`, [Makefile:116](../../../Makefile#L116) | pair | the client builds without the network |
| The rehearsal: the code deleted, the features and the DSL kept, the agent on an approved brief; the rehearsal's code kept as `<story>-backup`, the fallback of the live demo | themathmagician, the brief pair | the regenerated build's `make check` and `make arch-verify` |

## Stories describe themselves

| Task | Owner | Turns green |
|---|---|---|
| `Story.publishes()` in each story | themathmagician | the unit tests of `publishes()` |
| `@PrimaryPort(name, description)` on each story | themathmagician | the architecture rule |
| The named key strategies | themathmagician | the map's key rule |

## Slides alongside

Owner: pair. The deck in [slides/](../../../slides/slides.md), with `make slidev-present` and `make slidev-export`. Each
feature fills its section of the deck as it lands. The talk plays from compose,
[Slides spike](../archived/release-0.0.6/03_automation.md#slides-spike-the-talk-plays-from-compose).

- The CI screenshot on the slide "The spec on both transports: test + CI", 2026-10-29.
- Sections 4 and 5, headers only, filled as #9 and #10 land.
- The PDF in `docs/generated/slides` is the export of 2026-10-29.
