# Discovery

The example map of each work package in the [catalog](release-0.0.8.md): its story, its rules with examples, its
questions, and the decisions it raises for [docs/adr.md](../../adr.md). Examples agreed 2026-10-29 on all four.

## The purse's architecture feature passes

**Story:** as the purse, a story playing in Wonderland, my architecture is a feature, and it passes.

- The feature,
  [the_purse_has_hexagonal_architecture.feature](../../../examples/purse/src/test/resources/features/the_purse_has_hexagonal_architecture.feature), and
  the DSL as wished, [purse.textproto](../../../examples/purse/docs/generated/architecture/model/purse.textproto),
  written 2026-10-29. `make bdd-snippets` in `examples/purse` lists its 11 steps undefined.
- The platform ships its behaviour steps in its test fixtures and the purse reuses 7 of its 13 lines; the
  architecture steps ship the same way.

### Rule: a story's architecture feature states every architecture annotation of the story, in the code's own names, on steps the library and the platform offer; a client writes the facts and no step
- `Then its @PrimaryPort is KnowWhatIsLeft, which implements Story`, `And KnowWhatIsLeft subscribesTo Transaction`,
  `And its @Bean purse builds KnowWhatIsLeft from PurseProperties`.
- the hexagon's steps, the library's: the architecture of the build, a module, its port, its wiring, what it
  depends on, every payload driven by a scenario, no Hexagonal Architecture stereotype and no Spring wiring the
  feature does not state, one step each, decided 2026-10-30; Wonderland's, the platform's: `subscribesTo`,
  `publishes … through PublishMessage`, `publishes nothing`.

### Rule: an annotation the generated diagram carries that the feature does not state is red
- `Scenario: Nothing else is annotated`: the step reads the feature it runs in through Gherkin's parser, collects
  every `@Annotation` its steps name, and compares with the DSL.

### Rule: a behaviour feature stays in domain language; the hexagon's words live in the architecture feature and the DSL
- `When the purse reads the stream of transactions` in the behaviour feature; `subscribesTo Transaction` in the
  architecture feature.

### Rule: an @architecture-discovery feature plays without containers
- `make arch-bdd` in `examples/purse`: the five scenarios, no image, no broker.

### Rule: a behaviour feature belongs to the story whose build it lives in; one that lives elsewhere says which story it drives, @story:<name>
- the purse's and the rabbit's features: no tag, the build is the story's.
- the generator's two in the platform, `i_can_generate_events.feature` and the Flink table's: `@story:load`.
- `arch-bdd` reads the scenarios that drove a story from the last run's report by that rule.

## architecture-discovery extracts the purse's .textproto

**Story:** as the build, I write a story's architecture as a `.textproto` from the code, and a reader sees what
the story subscribes to and publishes, its ports, and how Spring wires it.

- `ModelWriter` in [architecture-discovery/](../../../architecture-discovery/architecture-discoverability.md)
  reads the classes once into the model, ArchUnit and Modulith; `HexagonDocumenter`, `ForestDocumenter`, and
  `StreamsDocumenter` draw it; Modulith's `Documenter` writes the component diagrams; `renderDiagrams` in
  [platform.gradle](../../../platform.gradle) renders them with the PlantUML jar. Decided 2026-10-29, the model
  from the code, the PlantUML a view of it.
- `Story.subscribesTo()` and `publishes()` answer at runtime; the writer reads the class objects they name
  without starting the story.

### Rule: the DSL is Protobuf text by hexagon.proto in architecture-discovery, the hexagon's words as its fields
- `name`, `base_package`, `feature`, `primary_port`, `secondary_port`, `subscribes`, `publishes`, `through`,
  `depends_on`, `type`, and Spring apart under `wiring`: `configuration`, `settings`, `bean`; decided 2026-10-30.
  `TextFormat` parses it, the writer prints it in the specifications' layout; the generated classes are the
  model's Java form, `architecture.model`.

### Rule: one .textproto per story and one for the platform, under docs/generated/architecture/model, named as --story names the story
- `examples/purse/docs/generated/architecture/model/purse.textproto`; the root build writes `platform`, `load`,
  `tea-party`; the White Rabbit's build writes `white-rabbit.textproto`.

### Rule: the PlantUML is a view of the model, content only, its look in a style file
- `hexagon.puml`: a ring a `package`, a module a `hexagon`, a type a `card` with its role as the stereotype, the
  legend a `frame`; no `skinparam`, no `<style>`, no colour, no font size; `hexagon.style.puml` handed to
  PlantUML by `renderDiagrams`. Decided 2026-10-29; the model from the code since 2026-10-30, the PlantUML drawn
  from it.

### Rule: the Hexagonal Architecture stereotypes and the Spring wiring are kept apart, in the model and in the steps
- the stereotypes as jMolecules names them, `PrimaryPort`, `Module`; the wiring as Spring's, `AutoConfiguration`,
  `EnableConfigurationProperties` with its class, `ConfigurationProperties` with its prefix, `Bean` with what it
  builds and builds from; the purse's six across the two. Decided 2026-10-30.

### Rule: the scenarios drive, they are not specified
- no scenario name in a spec, a name or a line being an address, not architecture; `arch-bdd` reads the last
  run's `cucumber.json` and fails a story whose `subscribes` or `publishes` no scenario of its features drives.
  Decided 2026-10-30.

### Rule: the three levels are drawn from the DSL: the forest, the streams, the hexagon of each
- the landscape of the platform and its stories; the stories with what they subscribe to and publish, a queue per
  payload; the hexagon of each story and of the platform.

**Questions**
1. The DSL: PlantUML, a hexagonal DSL of our own, Structurizr DSL, Mermaid, JSON, Gherkin, and Protobuf text were
   compared on the three questions of the README's Purpose. Decided 2026-10-29: Protobuf text, its grammar in the
   tool, Gherkin driving the verification, the behaviour feature in domain language, nothing external entering.
2. The static home of what a story subscribes to and publishes, so the writer reads it without starting the
   story. Decided 2026-10-30: the class objects the port's methods name literally, `subscribesTo()` returning
   `Set.of(Transaction.class)` and its twin `publishes()`, a default of `Set.of()` in `Story`; the compiled class
   keeps them as constants and the writer reads them with ArchUnit as the methods' referenced class objects. The
   load story answers every payload `--load.type` can pick, the streams showing what a story can put on a topic.

## make arch-bdd and make arch-check check the DSL

**Story:** as CI, I am red when the DSL and the code disagree, or the DSL and the feature.

- `make arch-check` compares the specification with the build, [platform.mk](../../../platform.mk); `make
  arch-verify` runs the architecture tests, `ArchUnitTest` and `ArchModulithTest`; `make arch-bdd` the features,
  `ArchBddTest`; `make check` is the CI gate.

### Rule: make arch-check compares the DSL as specified with the DSL as built, layout aside; the PlantUML and the SVG are artifacts, not compared
- `src/test/resources/architecture/purse.textproto`, written by hand, against what `make arch-gen` writes under
  `docs/generated/architecture/model`: both parsed as messages, a value apart red, naming the file and the lines
  each side has alone. Decided 2026-10-30.

### Rule: make arch-bdd plays the @architecture-discovery features, and the bdd targets leave them out
- `make arch-bdd`: the `@architecture-discovery` features, no containers, reports under `build/reports/arch`.
- `make bdd`: every scenario but `@architecture-discovery`, `@wip`, `@manual`, and the transport's tags.

### Rule: the three levels are targets of their own under arch-gen
- `make arch-forest`, `make arch-streams`, `make arch-hexagon`: one drawing each; `make arch-gen` the model and
  all three.

### Rule: the ci job and a client's cicd run both
- `make check` in `cicd.yaml` and in `cicd-client-example.yaml`; the purse's `cicd` red on a `.textproto` that
  lies.

## The White Rabbit and the skill: the round trip

**Story:** as a client of the platform, I write the White Rabbit's features and its hexagon before its code, the
build says whether what was built is what was specified, and the skill does it again.

- The spec, written 2026-10-29 before its code:
  [the_white_rabbit_has_no_time_to_haggle.feature](../../../examples/white-rabbit-1/src/test/resources/features/the_white_rabbit_has_no_time_to_haggle.feature),
  [the_white_rabbits_hexagon.feature](../../../examples/white-rabbit-1/src/test/resources/features/the_white_rabbits_hexagon.feature),
  [white-rabbit.textproto](../../../examples/white-rabbit-1/src/test/resources/architecture/white-rabbit.textproto).
- `make platform-install` puts the platform, its test fixtures, and the stories in the local Maven repository,
  [Makefile:116](../../../Makefile#L116).

### Rule: the client is done when its make check is green, make arch-verify and make arch-bdd in it, its .textproto matching what the build writes
### Rule: the skill's input is the architecture rules, the offered steps, the story's features and its .textproto, and the published platform
### Rule: the run works offline, from make platform-install
### Rule: the rehearsal's code is kept as <story>-backup, the fallback of the live demo

**Questions**
1. The client for the demo. Decided 2026-10-29: the White Rabbit Story, `stories.whiterabbit`, its port
   `NoTimeToHaggle`, subscribes to `Offer`, publishes `Order`, in `examples/white-rabbit`.
