# Automation

The tasks of each work package in the [catalog](release-0.0.8.md), each with its owner and what it turns green.
One go per change. The evidence of Spec green, the green run and the ADRs a package touches, is linked from the
package's row in the catalog. The first task is the purse's architecture feature green.

## The purse's architecture feature passes

| Task | Owner | Turns green |
|---|---|---|
| The hexagon's steps in the library's test fixtures: the architecture of the build, a module, its port, what it depends on, every arrow naming a scenario, nothing else annotated | pair | the purse's scenarios but the two of Wonderland's |
| Wonderland's steps in the platform's test fixtures: `listensTo`, `publishes … through PublishMessage`, `publishes nothing` | pair | the purse's two remaining scenarios |
| The suite: `@architecture` played by `make arch-verify` without containers, in the root build and in a client's | pair | `make arch-verify` green in `examples/purse` |

## architecture-discovery extracts the purse's .textproto

| Task | Owner | Turns green |
|---|---|---|
| Clean the generation: `hexagon.puml` content only; the look in `hexagon.style.puml`, handed to PlantUML by `renderDiagrams`; Modulith's diagrams in its UML style | pair | the `.puml` holds no styling line, `make arch-gen` draws the same hexagon |
| Every architecture annotation of a module as a stereotype in `hexagon.puml` | pair | the purse's `hexagon.puml` names its six |
| `hexagon.proto`, the package `architecture.model`, `proto-gen` covering the library | pair | `make build` |
| The writer: the PlantUML read into the model, printed as `docs/generated/architecture/model/<story>.textproto` and `platform.textproto`, in `make arch-gen` | pair | the root build writes `platform`, `load`, `tea-party`; the purse writes `purse` |
| Static `listensTo` and `publishes` on a story; the hexagon draws them, the DSL carries them | themathmagician | the tea party's file says hears Offer and Order, publishes Transaction |
| The links: the last `bdd` run's `cucumber.json` read by the writer, each arrow naming its scenario | pair | the tea party's arrows carry their lines; an arrow without one is listed |
| The three levels from the DSL: the forest, the streams, the hexagon of each; the deck's section 4 | pair | three SVGs per build |

## make arch-verify and make arch-check check the DSL

| Task | Owner | Turns green |
|---|---|---|
| `make arch-check` exact on `docs/generated/architecture/model`, line-sorted on the PlantUML as today | pair | red on a `.textproto` one line apart |
| `make arch-verify` in `platform.mk` and the Gradle task behind it; `bdd` leaves `@architecture` out | pair | the purse's feature green, `make bdd` unchanged |
| `make check` carries both, in `cicd.yaml` and `cicd-client-example.yaml` | pair | the purse's `cicd` red on a `.textproto` that lies |

## The White Rabbit and the skill: the round trip

| Task | Owner | Turns green |
|---|---|---|
| The White Rabbit: the story, `NoTimeToHaggle`, its auto-configuration and properties, its build as the purse's | themathmagician | its `make check` and `make arch-verify` |
| The offline run from `make platform-install`, [Makefile:116](../../../Makefile#L116) | pair | the client builds without the network |
| The skill `generate-platform-client` in `.claude/skills/` | themathmagician | the brief it yields |
| The rehearsal: the code deleted, the features and the DSL kept, the agent on an approved brief; the code kept as `<story>-backup` | themathmagician, the brief pair | the regenerated build's `make check` and `make arch-verify`; the deck's section 5 |

## Slides alongside

Owner: pair, [#11](https://github.com/Mathmagicians/wonderland-eventing-playground/issues/11). The deck in
[slides/](../../../slides/slides.md), with `make slidev-present` and `make slidev-export`. Each package fills its
section of the deck as it lands. The talk plays from compose,
[Slides spike](../archived/release-0.0.6/03_automation.md#slides-spike-the-talk-plays-from-compose).

- The CI screenshot on the slide "The spec on both transports: test + CI", 2026-10-29.
- Sections 4 and 5, headers only, filled by the extraction and the round trip.
- The PDF in `docs/generated/slides` is the export of 2026-10-29.
