# Discovery

The example map of each work package in the [catalog](release-0.0.8.md): its story, its rules with examples, its
questions, and the decisions it raises for [docs/adr.md](../../adr.md). Examples agreed 2026-10-29 on all four.

## The purse's architecture feature passes

**Story:** as the purse, a story playing in Wonderland, my architecture is a feature, and it passes.

- The feature,
  [the_purses_hexagon.feature](../../../examples/purse/src/test/resources/features/the_purses_hexagon.feature), and
  the DSL as wished, [purse.textproto](../../../examples/purse/docs/generated/architecture/model/purse.textproto),
  written 2026-10-29. `make bdd-snippets` in `examples/purse` lists its 11 steps undefined.
- The platform ships its behaviour steps in its test fixtures and the purse reuses 7 of its 13 lines; the
  architecture steps ship the same way.

### Rule: a story's architecture feature states every architecture annotation of the story, in the code's own names, on steps the library and the platform offer; a client writes the facts and no step
- `Then its @PrimaryPort is KnowWhatIsLeft, which implements Story`, `And KnowWhatIsLeft listensTo Transaction`,
  `And its @Bean purse builds KnowWhatIsLeft from PurseProperties`.
- the hexagon's steps, the library's: the architecture of the build, a module, its port, what it depends on, every
  arrow naming a scenario, nothing else annotated; Wonderland's, the platform's: `listensTo`, `publishes … through
  PublishMessage`, `publishes nothing`.

### Rule: an annotation the generated diagram carries that the feature does not state is red
- `Scenario: Nothing else is annotated`: the step reads the feature it runs in through Gherkin's parser, collects
  every `@Annotation` its steps name, and compares with the DSL.

### Rule: a behaviour feature stays in domain language; the hexagon's words live in the architecture feature and the DSL
- `When the purse reads the stream of transactions` in the behaviour feature; `listensTo Transaction` in the
  architecture feature.

### Rule: an @architecture feature plays without containers
- `make arch-verify` in `examples/purse`: the five scenarios, no image, no broker.

## architecture-discovery extracts the purse's .textproto

**Story:** as the build, I write a story's hexagon as a `.textproto`, from the PlantUML the code draws, and a
reader sees what the story hears and publishes and where its annotations go.

- `HexagonDocumenter` in [architecture-discovery/](../../../architecture-discovery/architecture-discoverability.md)
  writes `hexagon.puml` from the modules and their jMolecules stereotypes; Modulith's `Documenter` writes the
  component diagrams; `renderDiagrams` in [platform.gradle](../../../platform.gradle) renders them with the
  PlantUML jar.
- `Story.listensTo()` answers at runtime; the writer reads the code without starting it.

### Rule: the DSL is Protobuf text by hexagon.proto in architecture-discovery, the hexagon's words as its fields
- `story`, `port`, `hears`, `publishes`, `through`, `depends_on`, `annotated`; `TextFormat` prints and parses it;
  the generated classes are the model's Java form, `architecture.model`.

### Rule: one .textproto per story and one for the platform, under docs/generated/architecture/model, named as --story names the story
- `examples/purse/docs/generated/architecture/model/purse.textproto`; the root build writes `platform`, `load`,
  `tea-party`; the White Rabbit's build writes `white-rabbit.textproto`.

### Rule: the DSL is written from the generated PlantUML, which is content only, its look in a style file
- `hexagon.puml`: packages for the rings and the modules, components with their stereotypes, arrows with their
  labels; no `skinparam`, no `<style>`, no colour, no font size; `hexagon.style.puml` handed to PlantUML by
  `renderDiagrams`.

### Rule: every architecture annotation of a module is a stereotype in the PlantUML, and a line in the DSL
- `@Module`, `@PrimaryPort`, `@AutoConfiguration`, `@EnableConfigurationProperties` with its class,
  `@ConfigurationProperties` with its prefix, `@Bean` with what it builds: the purse's six.

### Rule: every arrow of the DSL names the scenario that drives it, from the last bdd run's cucumber.json
- `hears { payload: "Transaction" scenario: "alices_purse_knows_what_is_left.feature:9" }`.

### Rule: the three levels are drawn from the DSL: the forest, the streams, the hexagon of each
- the landscape of the platform and its stories; the stories with what they hear and publish; the hexagon of each
  story and of the platform.

**Questions**
1. The DSL: PlantUML, a hexagonal DSL of our own, Structurizr DSL, Mermaid, JSON, Gherkin, and Protobuf text were
   compared on the three questions of the README's Purpose. Decided 2026-10-29: Protobuf text, its grammar in the
   tool, Gherkin driving the verification, the behaviour feature in domain language, nothing external entering.
2. The static home of what a story hears and publishes, so the writer reads it without starting the story: an
   annotation on the story, or a constant beside `listensTo()`. themathmagician's.

## make arch-verify and make arch-check check the DSL

**Story:** as CI, I am red when the DSL and the code disagree, or the DSL and the feature.

- `make arch-check` compares `docs/generated/architecture/plantuml` line-sorted,
  [platform.mk:161](../../../platform.mk#L161); `make arch-verify` runs the architecture tests,
  [platform.mk:153](../../../platform.mk#L153); `make check` is the CI gate.

### Rule: make arch-check compares the DSL exactly, the PlantUML line-sorted
- `purse.textproto` as committed against what `make arch-gen` writes: one line apart, red, naming the file.

### Rule: make arch-verify plays the @architecture features, and the bdd targets leave them out
- `make arch-verify`: the architecture tests and the `@architecture` features, no containers.
- `make bdd`: every scenario but `@architecture`, `@wip`, `@manual`, and the transport's tags.

### Rule: the ci job and a client's cicd run both
- `make check` in `cicd.yaml` and in `cicd-client-example.yaml`; the purse's `cicd` red on a `.textproto` that
  lies.

## The White Rabbit and the skill: the round trip

**Story:** as a client of the platform, I write the White Rabbit's features and its hexagon before its code, the
build says whether what was built is what was specified, and the skill does it again.

- The spec, written 2026-10-29 before its code:
  [the_white_rabbit_has_no_time_to_haggle.feature](../../../examples/white-rabbit/src/test/resources/features/the_white_rabbit_has_no_time_to_haggle.feature),
  [the_white_rabbits_hexagon.feature](../../../examples/white-rabbit/src/test/resources/features/the_white_rabbits_hexagon.feature),
  [white-rabbit.textproto](../../../examples/white-rabbit/docs/generated/architecture/model/white-rabbit.textproto).
- `make platform-install` puts the platform, its test fixtures, and the stories in the local Maven repository,
  [Makefile:116](../../../Makefile#L116).

### Rule: the client is done when its make check and make arch-verify are green, its .textproto matching what the build writes
### Rule: the skill's input is the architecture rules, the offered steps, the story's features and its .textproto, and the published platform
### Rule: the run works offline, from make platform-install
### Rule: the rehearsal's code is kept as <story>-backup, the fallback of the live demo

**Questions**
1. The client for the demo. Decided 2026-10-29: the White Rabbit Story, `stories.whiterabbit`, its port
   `NoTimeToHaggle`, hears `Offer`, publishes `Order`, in `examples/white-rabbit`.
