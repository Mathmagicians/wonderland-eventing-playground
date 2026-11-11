# Automation

The tasks of each work package in the [catalog](release-0.0.8.md), each with its owner and what it turns green.
One go per change. The evidence of Spec green, the green run and the ADRs a package touches, is linked from the
package's row in the catalog. The first task is the purse's architecture feature green.

## The purse's architecture feature passes

| Task | Owner | Turns green |
|---|---|---|
| The hexagon's steps: the architecture of the build, a module, its port, what it depends on, every arrow naming a scenario, nothing else annotated | pair | done 2026-10-29: [HexagonSteps](../../../platform/src/testFixtures/java/dk/mathmagicians/wonderland/architecture/bdd/HexagonSteps.java) over [ArchitectureModel](../../../platform/src/testFixtures/java/dk/mathmagicians/wonderland/architecture/bdd/ArchitectureModel.java), the platform's test fixtures, package `architecture.bdd`, reading `model/<story>.textproto` into the library's generated classes; the library ships the model, the platform the steps |
| Wonderland's steps: `subscribesTo`, `publishes … through PublishMessage`, `publishes nothing` | pair | done 2026-10-29: [WonderlandSteps](../../../platform/src/testFixtures/java/dk/mathmagicians/wonderland/architecture/bdd/WonderlandSteps.java) beside them |
| The suite: `@architecture-discovery` played by `make arch-bdd` without containers, in the root build and in a client's | pair | done 2026-10-29: [ArchBddTest](../../../examples/purse/src/test/java/dk/mathmagicians/wonderland/examples/stories/purse/bdd/ArchBddTest.java) in the purse, glue `architecture.bdd`, the tag, its reports under `build/reports/arch`; the `bdd` task leaves the tag out; the tests are `ArchUnitTest` and `ArchModulithTest` under `make arch-verify` |

Spec green, to sign: `make arch-bdd` in `examples/purse` on 2026-10-30, the five scenarios of
`the_purse_has_hexagonal_architecture.feature` green against the `purse.textproto` that `make arch-gen` wrote from
the code the same day, `subscribes` from `Story.subscribesTo()`, the application ring's module on its default
label `Application`, and the payloads driven by the scenarios of the last `bdd` run's report. On 2026-10-29 the
feature was green against the file as wished and red against the first file the build wrote, on the package, the
dependency names, and `hears`, since then `subscribes`.

## architecture-discovery extracts the purse's .textproto

| Task | Owner | Turns green |
|---|---|---|
| Clean the generation: `hexagon.puml` content only; the look in `hexagon.style.puml`, handed to PlantUML by `renderDiagrams`; Modulith's diagrams in its UML style; [the steps](#clean-the-generation) | pair | the `.puml` holds no styling line, `make arch-gen` draws the same hexagon |
| Every architecture annotation of a module read by the build | pair | done 2026-10-29 into `hexagon.puml`, superseded 2026-10-30: the annotations live in the model, `Platform`'s modules and types with `stereotype` and `wiring` apart, and a story's `wiring`, the row below; the hexagon shows roles alone |
| `hexagon.proto`, the package `architecture.model`, `proto-gen` covering the library | pair | done 2026-10-29: [hexagon.proto](../../../architecture-discovery/src/main/proto/hexagon.proto), `Story` and `Platform`, the generated classes under the library's `src/generated`, the protobuf plugin in its `build.gradle`; `Stories` and `TextProtoWriter` with their tests, `make discovery-build` green |
| The model from the code, decided 2026-10-29, [the pipeline](#one-model-the-pipeline): `ModelWriter` in the library reads the classes once, ArchUnit and Modulith, into `Platform`: the modules with their ring, package, and stereotypes, the types with their role and every architecture annotation, the arrows; `Stories.of` and `TextProtoWriter` print `model/<story>.textproto` and `platform.textproto` in `make arch-gen`. `HexagonDocumenter` draws from `Platform`, the rings, the modules, the roles, the arrows, no wiring. `HexagonReader` retires | pair | done 2026-10-29: [ModelWriter.java](../../../architecture-discovery/src/main/java/dk/mathmagicians/playground/architecture/model/ModelWriter.java) with `ModelWriterTest`, `HexagonDocumenter` over `Platform`, `ModuleDocumentation` writing `model/` and `plantuml/`; the purse writes `purse.textproto` and the root build `platform`, `load`, `tea-party`, no Spring stereotype in `hexagon.puml`, `make discovery-install` and `make platform-install` done; `generateArchitecture` leaves `RunArchitectureTest` out. `HexagonReader`, its test, and the puml fixtures retired 2026-10-30. The DSL's shape, decided 2026-10-30: `primary_port` and `secondary_port` one line each, the role in the field's name; `subscribes` and `publishes`; `depends_on` and `feature` as lists; `type` for any other stereotyped type; Spring under `wiring { configuration  settings  bean }`; in `Platform`, a module's and a type's `stereotype` and `wiring` kept apart, jMolecules' and Spring's names bare. `TextProtoWriter` prints the layout the specifications are written in, a scalar per line, a repeated one as a list, a message on one line when it fits in 150 columns |
| `subscribes` and `publishes` of a story, the eventing pair decided 2026-10-29, `publish` the code's word already: the writer reads what `Story.subscribesTo()` references and what the port hands `PublishMessage`, so the DSL carries them and the hexagon draws them; `feature` on the story | pair for the writer, themathmagician for the names in `Story` and the step | done 2026-10-30: `ModelWriter` draws `SUBSCRIBES` from the payload class objects `subscribesTo()` references and `PUBLISHES` for the payloads a port that reaches another driving port depends on and does not subscribe to; `Story.subscribesTo()` and the step `subscribesTo` themathmagician's; the tea party's file says subscribes Offer and Order, publishes Transaction through PublishMessage. A story's `feature` follows the map's rule: a behaviour feature belongs to the story whose build it lives in, the project named as the story or the one story of a client's build, and one that lives elsewhere says `@story:<name>`, `@story:load` on the generator's two in the platform; `generateArchitecture` hands the writer the features of every project in the build. The platform's specifications, [app/src/test/resources/architecture](../../../app/src/test/resources/architecture), `platform`, `load`, `tea-party`, written from the build and `make arch-check` green at the root. Decided 2026-10-30: `Story.publishes()`, the twin of `subscribesTo()`, in `Story`, the tea party, and the load story, themathmagician's three lines, open; the writer reads its class objects as it reads `subscribesTo()`'s, beside the port's reach, done 2026-10-30, `ModelWriterTest` over the fixture's `Squarer`, which declares, and `Shaper`, which does not |
| The scenarios drive, they are not specified, decided 2026-10-29: `make arch-bdd` reads the last `bdd` run's `cucumber.json` and fails a story whose `subscribes` or `publishes` no scenario of its `feature` drives, naming the payload; no scenario name in any spec, a name or a line being an address, not architecture. The step `every payload it subscribes to or publishes is driven by a scenario of its feature` in `HexagonSteps` reads the report through `ArchitectureModel.namesReportedFor`: a payload is driven when a scenario or a step of the story's features names it or its stream; the story's features are the ones it lists, else every feature of the run, the build's own, [the rule](discovery.md#rule-a-behaviour-feature-belongs-to-the-story-whose-build-it-lives-in-one-that-lives-elsewhere-says-which-story-it-drives-storyname) | pair | done 2026-10-30 in the purse: `Transaction` driven by `reads the stream of transactions`; an undriven payload fails naming it. The writer lists a story's `feature` by place and the `@story:<name>` tag, done 2026-10-30, `StoriesTest` on both halves of the rule |
| The three levels from the DSL: the forest, the streams, the hexagon of each; the deck's section 4 | pair | the drawings done 2026-10-30: [ForestDocumenter](../../../architecture-discovery/src/main/java/dk/mathmagicians/playground/architecture/discovery/ForestDocumenter.java), the platform a hexagon and the stories hexagons around it, `plays on`, and [StreamsDocumenter](../../../architecture-discovery/src/main/java/dk/mathmagicians/playground/architecture/discovery/StreamsDocumenter.java), a queue per payload with the stories publishing and subscribing, each a view of `Platform` with its test; `ModuleDocumentation` writes `forest.puml` and `streams.puml` beside `hexagon.puml`; the README's Architecture section shows the three levels after the handmade diagram, each captioned with `make arch-gen`; the load story stands alone in the streams until `GenerateLoad.publishes()` says Product, Offer, Order, themathmagician's line. The wiring done 2026-10-30: `arch-gen` renders every `.puml`, `arch-forest`, `arch-streams`, `arch-hexagon` each check their SVG and name it when missing, the deck's three level slides show `/generated/forest.svg`, `streams.svg`, `hexagon.svg` |

### One model, the pipeline

Decided 2026-10-29: the code is read once into the model, and every view and every step reads the model. The
hexagon shows what a picture needs; the wiring stays in the model, where the features state it. The pipeline as
built, `arch-gen`, `arch-verify`, `arch-bdd`, `arch-check`, is the library's document,
[architecture-discoverability.md](../../../architecture-discovery/architecture-discoverability.md), steps 5 to 7,
and the README's Architecture section.

### Clean the generation

Done 2026-10-29, pointers: `HexagonDocumenter` writes content only, a ring a `package`, a module a `hexagon`, a
type a `card` with its role as the stereotype, the legend a `frame`, `HexagonDocumenterTest.carriesNoStyling`;
the look is [hexagon.style.puml](../../../architecture-discovery/src/main/resources/hexagon.style.puml), taken
out of the library jar by `diagramStyle` in [platform.gradle](../../../platform.gradle) and handed to PlantUML as
`-config`; Modulith's diagrams in `DiagramStyle.UML`, Structurizr's writer with its own skinparams; the system
named `Wonderland` by `@Modulithic(systemName)`, the application module `Application`, a bare `@Module`. The
question on the rule's words against the code's closed with the DSL of 2026-10-30.

Finding, open: `ci` resolves the library as `latest.integration` from GitHub Packages, published by
`architecture-discovery.yaml` on the same push; a `ci` that resolved the jar before the publish fails
`diagramStyle` with a `NullPointerException` on the missing entry, a `gh run rerun`,
[github-actions.md](../../../.claude/skills/architect-pair/references/github-actions.md); the task should name
the entry and `make discovery-install`.

## make arch-bdd and make arch-check check the DSL

| Task | Owner | Turns green |
|---|---|---|
| `make arch-check` holds the build to the specification under `SPEC` | pair | done 2026-10-30: the specification against the build. [ModelCheck.java](../../../architecture-discovery/src/main/java/dk/mathmagicians/playground/architecture/model/ModelCheck.java) parses the `.textproto` specified by hand and the one the build wrote as messages and compares them, layout aside, the lines one side has alone as the finding; `archCheck` in [platform.gradle](../../../platform.gradle), `make arch-check` with `SPEC`, default `src/test/resources/architecture`, in [platform.mk](../../../platform.mk). The purse's specification moved there, [purse.textproto](../../../examples/purse/src/test/resources/architecture/purse.textproto), its package and module names as they are today. The PlantUML and the SVG are artifacts drawn from the model, not compared |
| `make arch-bdd` in `platform.mk` and the Gradle task `archBdd` behind it; `bdd` leaves `@architecture-discovery` out; the levels as targets, `arch-forest`, `arch-streams`, `arch-hexagon`, under `arch-gen` | pair | done 2026-10-29: the purse's feature green, `make bdd` unchanged; `arch-forest`, `arch-streams`, and `arch-hexagon` each check their SVG since 2026-10-30, the levels drawn |
| `make check` carries both, in `cicd.yaml` and `cicd-client-example.yaml` | pair | done 2026-10-30: `check` is `arch-verify build docker-image bdd arch-bdd`, the architecture features after the features, on their run's report; in `cicd.yaml` the `bdd` job runs `make arch-bdd` after `make bdd-published`, the `ci` job `make arch-verify`; the purse's `cicd` plays `make check` as before |

## The White Rabbit and the skill: the round trip

| Task | Owner | Turns green |
|---|---|---|
| The White Rabbit's build as the purse's: the wrapper, `build.gradle`, `settings.gradle`, `gradle.properties`, `Makefile`, `compose.yaml`, the tests `ArchUnitTest`, `ArchModulithTest`, `ArchBddTest`, `RunCucumberTest`; its specification at [white-rabbit.textproto](../../../examples/white-rabbit-1/src/test/resources/architecture/white-rabbit.textproto) in the DSL's shape | pair | done 2026-10-30, the red runs: `make arch-bdd` fails naming the missing module, "the stories the build wrote" without `White Rabbit Story`; `make bdd-snippets` lists the feature's five undefined steps; `make arch-verify` red on `ArchModulithTest`, Modulith refusing the root package's reach into `eventing.application` while the build has no `@Module` of its own, green once the story's package exists |
| The White Rabbit: the story, `NoTimeToHaggle`, its auto-configuration and properties, its steps | themathmagician | its `make check`, `make arch-verify`, and `make arch-bdd` |
| The offline run from `make platform-install`, [Makefile:116](../../../Makefile#L116) | pair | the client builds without the network |
| The skill `wonderland-generate-story` in `.claude/skills/` | themathmagician, pair secretary | written 2026-10-30, [SKILL.md](../../../.claude/skills/wonderland-generate-story/SKILL.md): step 0 writes a missing feature or `.textproto` with the human, then the specification read, the code in order, the gates in order, the report; its first run is the rehearsal on the White Rabbit |
| The rehearsal: the code deleted, the features and the DSL kept, the agent on an approved brief; the code kept as `<story>-backup` | themathmagician, the brief pair | the regenerated build's `make check` and `make arch-verify`; the deck's section 5 |

## Slides alongside

Owner: pair, [#11](https://github.com/Mathmagicians/wonderland-eventing-playground/issues/11). The deck in
[slides/](../../../slides/slides.md), with `make slidev-present` and `make slidev-export`. Each package fills its
section of the deck as it lands. The talk plays from compose,
[Slides spike](../archived/release-0.0.6/03_automation.md#slides-spike-the-talk-plays-from-compose).

- The CI screenshot on the slide "The spec on both transports: test + CI", 2026-10-29.
- Sections 4 and 5, headers only, filled by the extraction and the round trip.
- The PDF in `docs/generated/slides` is the export of 2026-10-29.
