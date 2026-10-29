# Automation

The tasks of each work package in the [catalog](release-0.0.8.md), each with its owner and what it turns green.
One go per change. The evidence of Spec green, the green run and the ADRs a package touches, is linked from the
package's row in the catalog. The first task is the purse's architecture feature green.

## The purse's architecture feature passes

| Task | Owner | Turns green |
|---|---|---|
| The hexagon's steps: the architecture of the build, a module, its port, what it depends on, every arrow naming a scenario, nothing else annotated | pair | done 2026-10-29: [HexagonSteps](../../../platform/src/testFixtures/java/dk/mathmagicians/wonderland/architecture/bdd/HexagonSteps.java) over [ArchitectureModel](../../../platform/src/testFixtures/java/dk/mathmagicians/wonderland/architecture/bdd/ArchitectureModel.java), the platform's test fixtures, package `architecture.bdd`, reading `model/<story>.textproto` into the library's generated classes; the library ships the model, the platform the steps |
| Wonderland's steps: `listensTo`, `publishes … through PublishMessage`, `publishes nothing` | pair | done 2026-10-29: [WonderlandSteps](../../../platform/src/testFixtures/java/dk/mathmagicians/wonderland/architecture/bdd/WonderlandSteps.java) beside them |
| The suite: `@architecture-discovery` played by `make arch-bdd` without containers, in the root build and in a client's | pair | done 2026-10-29: [ArchBddTest](../../../examples/purse/src/test/java/dk/mathmagicians/wonderland/examples/stories/purse/bdd/ArchBddTest.java) in the purse, glue `architecture.bdd`, the tag, its reports under `build/reports/arch`; the `bdd` task leaves the tag out; the tests are `ArchUnitTest` and `ArchModulithTest` under `make arch-verify` |

Spec green, to sign: `make arch-bdd` in `examples/purse` on 2026-10-30, the five scenarios of
`the_purse_has_hexagonal_architecture.feature` green against the `purse.textproto` that `make arch-gen` wrote from
the code the same day, `subscribes` from `Story.subscribesTo()`, the application ring's module on its default
label `Application`, and the payloads driven by the scenarios of the last `bdd` run's report. On 2026-10-29 the
feature was green against the file as wished and red against the first file the build wrote, on the package, the
dependency names, and `hears`.

## architecture-discovery extracts the purse's .textproto

| Task | Owner | Turns green |
|---|---|---|
| Clean the generation: `hexagon.puml` content only; the look in `hexagon.style.puml`, handed to PlantUML by `renderDiagrams`; Modulith's diagrams in its UML style; [the steps](#clean-the-generation) | pair | the `.puml` holds no styling line, `make arch-gen` draws the same hexagon |
| Every architecture annotation of a module as a stereotype in `hexagon.puml`: Spring's and jMolecules', on a type, its methods and constructors, or its package; a value where there is one, `Profile cli`, a bean with its method and what it builds, `Bean purse: KnowWhatIsLeft`; `HexagonDocumenter.stereotypes`, `HexagonDocumenterTest.listsEveryArchitectureAnnotationAsAStereotype`, the fixture's `Wiring` and `Settings` | pair | done 2026-10-29: the purse's `hexagon.puml` names its six, `make discovery-install` done; `make arch-gen` at the root and in `examples/purse`, then `make arch-check`, themathmagician |
| `hexagon.proto`, the package `architecture.model`, `proto-gen` covering the library | pair | done 2026-10-29: [hexagon.proto](../../../architecture-discovery/src/main/proto/hexagon.proto), `Story` and `Platform`, the generated classes under the library's `src/generated`, the protobuf plugin in its `build.gradle`; `Stories` and `TextProtoWriter` with their tests, `make discovery-build` green |
| The model from the code, decided 2026-10-29, [the pipeline](#one-model-the-pipeline): `ModelWriter` in the library reads the classes once, ArchUnit and Modulith, into `Platform`: the modules with their ring, package, and stereotypes, the types with their role and every architecture annotation, the reading `HexagonDocumenter` does today, the arrows; `Stories.of` and `TextProtoWriter` print `model/<story>.textproto` and `platform.textproto` in `make arch-gen`. `HexagonDocumenter` draws from `Platform`, the rings, the modules, the roles, the arrows, no wiring. `HexagonReader`, the PlantUML read into the model, retires with its test and the puml fixtures: themathmagician's yes | pair | done 2026-10-29: [ModelWriter.java](../../../architecture-discovery/src/main/java/dk/mathmagicians/playground/architecture/model/ModelWriter.java) with `ModelWriterTest`, `HexagonDocumenter` over `Platform`, `ModuleDocumentation` writing `model/` and `plantuml/`; the purse writes `purse.textproto` and the root build `platform`, `load`, `tea-party`, no Spring stereotype in `hexagon.puml`, `make discovery-install` and `make platform-install` done; `generateArchitecture` leaves `RunArchitectureTest` out. `HexagonReader`, its test, and the puml fixtures retired 2026-10-30. The DSL's shape, decided 2026-10-30: `primary_port` and `secondary_port` one line each, the role in the field's name; `subscribes` and `publishes`; `depends_on` and `feature` as lists; `type` for any other stereotyped type; Spring under `wiring { configuration  settings  bean }`; in `Platform`, a module's and a type's `stereotype` and `wiring` kept apart, jMolecules' and Spring's names bare. `TextProtoWriter` prints the layout the specifications are written in, a scalar per line, a repeated one as a list, a message on one line when it fits in 150 columns |
| `subscribes` and `publishes` of a story, the eventing pair decided 2026-10-29, `publish` the code's word already: the writer reads what `Story.subscribesTo()` references and what the port hands `PublishMessage`, so the DSL carries them and the hexagon draws them; `feature` on the story | pair for the writer, themathmagician for the names in `Story` and the step | done 2026-10-30: `ModelWriter` draws `SUBSCRIBES` from the payload class objects `subscribesTo()` references and `PUBLISHES` for the payloads a port that reaches another driving port depends on and does not subscribe to; `Story.subscribesTo()` and the step `subscribesTo` themathmagician's; the tea party's file says subscribes Offer and Order, publishes Transaction through PublishMessage. A story's `feature` follows the map's rule: a behaviour feature belongs to the story whose build it lives in, the project named as the story or the one story of a client's build, and one that lives elsewhere says `@story:<name>`, `@story:load` on the generator's two in the platform; `generateArchitecture` hands the writer the features of every project in the build. The platform's specifications, [app/src/test/resources/architecture](../../../app/src/test/resources/architecture), `platform`, `load`, `tea-party`, written from the build and `make arch-check` green at the root |
| The scenarios drive, they are not specified, decided 2026-10-29: `make arch-bdd` reads the last `bdd` run's `cucumber.json` and fails a story whose `subscribes` or `publishes` no scenario of its `feature` drives, naming the payload; no scenario name in any spec, a name or a line being an address, not architecture. The step `every payload it subscribes to or publishes is driven by a scenario of its feature` in `HexagonSteps` reads the report through `ArchitectureModel.namesReportedFor`: a payload is driven when a scenario or a step of the story's features names it or its stream; the story's features are the ones it lists, else every feature of the run, the build's own, [the rule](discovery.md#rule-a-behaviour-feature-belongs-to-the-story-whose-build-it-lives-in-one-that-lives-elsewhere-says-which-story-it-drives-storyname) | pair | done 2026-10-30 in the purse: `Transaction` driven by `reads the stream of transactions`; an undriven payload fails naming it. Open: the writer lists a story's `feature` by place and the `@story:<name>` tag, not `@purse` |
| The three levels from the DSL: the forest, the streams, the hexagon of each; the deck's section 4 | pair | three SVGs per build |

### One model, the pipeline

Decided 2026-10-29: the code is read once into the model, and every view and every step reads the model. The
hexagon shows what a picture needs; the wiring stays in the model, where the features state it.

```
 the human writes   the feature            the_purse_has_hexagonal_architecture.feature     read by make arch-verify
                    the code               @Module @PrimaryPort @AutoConfiguration @Bean
                                           @ConfigurationProperties @Component @Profile

 make arch-gen      ArchUnit + Modulith read the classes once
                    ModelWriter        ->  model/<story>.textproto, platform.textproto       the steps, arch-check
                                           feature, primary_port, secondary_port, subscribes,
                                           publishes, depends_on, type, wiring; the platform's
                                           modules with stereotype and wiring kept apart;
                                           no scenario, those drive, see arch-bdd
                    HexagonDocumenter  ->  plantuml/hexagon.puml                             renderDiagrams
                    (from the model)       rings, modules, roles, arrows; no wiring
                    Modulith Documenter -> plantuml/components.puml, module-*.puml, *.adoc   renderDiagrams
                    diagramStyle       ->  build/plantuml/hexagon.style.puml                 renderDiagrams
                    renderDiagrams     ->  svg/*.svg; later landscape.svg, streams.svg       README, the deck

 make arch-verify   ArchUnitTest, ArchModulithTest: the rules and the module boundaries
 make arch-bdd      ArchBddTest: the @architecture-discovery features, each step reading model/<story>.textproto
                    and the platform's module: what the feature states is in it; no Hexagonal Architecture
                    stereotype and no Spring wiring the feature does not state, one step each

 make arch-check    arch-gen, then ModelCheck: the specification by hand under SPEC, src/test/resources/architecture,
                    against the model built, parsed and compared, layout aside; plantuml and svg not compared
```

### Clean the generation

Rule: [the DSL is written from the generated PlantUML, which is content only](discovery.md#rule-the-dsl-is-written-from-the-generated-plantuml-which-is-content-only-its-look-in-a-style-file).
The writer parses `hexagon.puml`, so the file carries the elements, their stereotypes, their nesting, and the
arrows, and nothing about their look; the look rides with the library and reaches PlantUML when the diagrams are
rendered.

Done 2026-10-29:

- [HexagonDocumenter.java:79](../../../architecture-discovery/src/main/java/dk/mathmagicians/playground/architecture/discovery/HexagonDocumenter.java#L79)
  writes a ring as `package`, a module as `hexagon` titled with its name above its package, a type as
  `card "Type" as alias <<Role>>`, the legend as `frame`; the padding inside a hexagon, which keeps its slanted
  sides clear of the cards, is a `rectangle " "` the style file draws without a line, `padsEveryModule`; an arrow
  to a module without a card ends on the hexagon.
- [HexagonDocumenterTest.java:22](../../../architecture-discovery/src/test/java/dk/mathmagicians/playground/architecture/discovery/HexagonDocumenterTest.java#L22):
  `carriesNoStyling`, no line matching `STYLING`, `skinparam|<style>|#[0-9A-Fa-f]{6}|#line:|<size:|<b>`, and
  the other tests on the new elements; `make discovery-build` green.
- [hexagon.style.puml](../../../architecture-discovery/src/main/resources/hexagon.style.puml), the library's
  resource: a ring's package seen by its title alone, a hexagon's title plain, a card's stereotype small, the
  legend's frame and the arrows' labels grey and small, the spacing.
- [platform.gradle:218](../../../platform.gradle#L218): `diagramStyle` takes the file out of the
  architecture-discovery jar on the test runtime class path, `renderDiagrams` hands it to PlantUML as `-config`,
  read before every diagram of the run, Modulith's too.

Open, in order:

| Step | Owner | Turns green |
|---|---|---|
| [ModuleDocumentation.java:36](../../../platform/src/testFixtures/java/dk/mathmagicians/wonderland/architecture/ModuleDocumentation.java#L36) writes Modulith's diagrams in `DiagramStyle.UML`, `DiagramOptions` with `withStyle`, through `writeDocumentation(DiagramOptions, CanvasOptions)` | pair | done 2026-10-29: `components.puml` without `!include <C4/...>`, Structurizr's UML writer with its own skinparams |
| `make discovery-install`, the jar with the style file taken first, then `make arch-gen`: `hexagon.puml`, Modulith's `.puml`, and the SVGs regenerated | pair | done 2026-10-29 at the root and in `examples/purse`: `hexagon.puml` has no line `STYLING` matches; `hexagon.svg` the same hexagon, the cards clear of the slopes |
| The names, by the practice of `@Module(name = …)` on every module: `@Modulithic(systemName = "Wonderland")` on [EventingPlatform.java:10](../../../platform/src/main/java/dk/mathmagicians/wonderland/EventingPlatform.java#L10), done 2026-10-29; the application module keeps `Application`, a bare `@Module` on [eventing/application/package-info.java](../../../platform/src/main/java/dk/mathmagicians/wonderland/eventing/application/package-info.java), decided 2026-10-29, Cockburn's name for the ring | themathmagician | done 2026-10-29: the legend says Wonderland, the column reads Application, Load Story, Tea Party Story |
| `make arch-check` on the documents staged | themathmagician | green |
| [architecture-discoverability.md:35](../../../architecture-discovery/architecture-discoverability.md#L35): step 5 names the elements and the style file, step 6 the config, Output lists `components.puml` as UML; the text pair's, placed by themathmagician | themathmagician | done 2026-10-29 |

Facts:

- Modulith's UML writer is Structurizr's `StructurizrPlantUMLExporter`, which writes its own `skinparam` lines;
  its diagrams stay as Modulith writes them, the DSL reads `hexagon.puml` alone. Today each carries four styling
  lines, the `<style>` block and the title's size.
- `ci` resolves the library as `latest.integration` from GitHub Packages, published by
  `architecture-discovery.yaml` on the same push; a `ci` that resolved the jar before the publish fails
  `diagramStyle` on the missing entry, a `gh run rerun`,
  [github-actions.md](../../../.claude/skills/architect-pair/references/github-actions.md). Finding:
  `diagramStyle` names the missing entry and `make discovery-install`, instead of a `NullPointerException`.

Questions:

1. The rule's words and the code's: the rule says packages for the rings and the modules, components with their
   stereotypes, arrows with their labels; the code writes the modules as hexagons, the types as cards, and the
   arrows unlabelled, the legend naming the relations, `drawsTheArrowsWithTheLegendAndNoLabels`. The parser needs
   one vocabulary. a) the rule takes the code's words, as drawn; b) the code takes the rule's: components,
   labelled arrows, no legend.

## make arch-bdd and make arch-check check the DSL

| Task | Owner | Turns green |
|---|---|---|
| `make arch-check` exact on `docs/generated/architecture/model`, line-sorted on the PlantUML as today | pair | done 2026-10-29: the specification against the build. [ModelCheck.java](../../../architecture-discovery/src/main/java/dk/mathmagicians/playground/architecture/model/ModelCheck.java) parses the `.textproto` specified by hand and the one the build wrote as messages and compares them, layout aside, the lines one side has alone as the finding; `archCheck` in [platform.gradle](../../../platform.gradle), `make arch-check` with `SPEC`, default `src/test/resources/architecture`, in [platform.mk](../../../platform.mk). The purse's specification moved there, [purse.textproto](../../../examples/purse/src/test/resources/architecture/purse.textproto), its package and module names as they are today. The PlantUML and the SVG are artifacts drawn from the model, not compared |
| `make arch-bdd` in `platform.mk` and the Gradle task `archBdd` behind it; `bdd` leaves `@architecture-discovery` out; the levels as targets, `arch-forest`, `arch-streams`, `arch-hexagon`, under `arch-gen` | pair | done 2026-10-29: the purse's feature green, `make bdd` unchanged; `arch-forest` and `arch-streams` say "not drawn yet" until the levels task lands |
| `make check` carries both, in `cicd.yaml` and `cicd-client-example.yaml` | pair | done 2026-10-30: `check` is `arch-verify build docker-image bdd arch-bdd`, the architecture features after the features, on their run's report; in `cicd.yaml` the `bdd` job runs `make arch-bdd` after `make bdd-published`, the `ci` job `make arch-verify`; the purse's `cicd` plays `make check` as before |

## The White Rabbit and the skill: the round trip

| Task | Owner | Turns green |
|---|---|---|
| The White Rabbit's build as the purse's: the wrapper, `build.gradle`, `settings.gradle`, `gradle.properties`, `Makefile`, `compose.yaml`, the tests `ArchUnitTest`, `ArchModulithTest`, `ArchBddTest`, `RunCucumberTest`; its specification at [white-rabbit.textproto](../../../examples/white-rabbit/src/test/resources/architecture/white-rabbit.textproto) in the DSL's shape | pair | done 2026-10-30, the red runs: `make arch-bdd` fails naming the missing module, "the stories the build wrote" without `White Rabbit Story`; `make bdd-snippets` lists the feature's five undefined steps; `make arch-verify` red on `ArchModulithTest`, Modulith refusing the root package's reach into `eventing.application` while the build has no `@Module` of its own, green once the story's package exists |
| The White Rabbit: the story, `NoTimeToHaggle`, its auto-configuration and properties, its steps | themathmagician | its `make check`, `make arch-verify`, and `make arch-bdd` |
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
