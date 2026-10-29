# Discovery

The example map of each feature in the [catalog](release-0.0.8.md): its story, its rules with examples, its
questions, and the decisions it raises for [docs/adr.md](../../adr.md).

## Architecture discovery: one model, three levels

**Story:** as a reader of the architecture, I read each story's hexagon as the build wrote it from the code, what
it hears and publishes and where its annotations go, and I see the platform at three levels drawn from the same
files.

- `HexagonDocumenter` in [architecture-discovery/](../../../architecture-discovery/architecture-discoverability.md)
  writes PlantUML under `docs/generated/architecture` from the annotations; `make arch-check` compares its sources
  line-sorted, [platform.mk:107](../../../platform.mk#L107).
- A behaviour feature stays in domain language, the README's rule; the hexagon's words live in the DSL and in the
  architecture discovery features.
- The purse's architecture discovery feature,
  [the_purses_hexagon.feature](../../../examples/purse/src/test/resources/features/the_purses_hexagon.feature), and
  its DSL as wished, [purse.textproto](../../../examples/purse/docs/generated/architecture/model/purse.textproto),
  written 2026-10-29.

### Rule: the DSL is written by the tool from the generated PlantUML, in the hexagon's words: one .textproto per story and one for the platform, by hexagon.proto in architecture-discovery, the drawing content only, its look in a style file
### Rule: every arrow of the DSL names the scenario that drives it, from the last bdd run's cucumber.json
### Rule: a story's architecture discovery feature states every architecture annotation of the story, on steps the library and the platform offer, and a client writes the facts and no step; an annotation the feature does not state is red
### Rule: level 1, the forest: the landscape, the platform with its stories and the purse
### Rule: level 2, the streams: the stories with what they hear and publish
### Rule: level 3, the hexagon of each story and of the platform: the rings and the adapters, drawn from the DSL
### Rule: make arch-check compares the DSL exactly, the PlantUML line-sorted

**Questions**
1. The DSL: PlantUML, a hexagonal DSL of our own, Structurizr DSL, Mermaid, JSON, Gherkin, and Protobuf text were
   compared on the three questions of the README's Purpose. Decided 2026-10-29: Protobuf text, its grammar
   `hexagon.proto` in the tool, its parser and printer `TextFormat`; Gherkin drives the verification; the feature
   stays in domain language; nothing external enters.

## The skill generate-platform-client

**Story:** as a client of the platform, I write a story's features and its hexagon before its code, and the build
tells whoever builds it whether what was built is what was specified.

- The purse extends `HexagonalArchitecture` and `ModuleDocumentation` with one-line classes, and 7 of the 13 step
  lines of its feature are platform steps.
- `make platform-install` puts the platform, its test fixtures, and the stories in the local Maven repository,
  [Makefile:116](../../../Makefile#L116).
- The White Rabbit's spec, written 2026-10-29 before its code:
  [the_white_rabbit_has_no_time_to_haggle.feature](../../../examples/white-rabbit/src/test/resources/features/the_white_rabbit_has_no_time_to_haggle.feature),
  [the_white_rabbits_hexagon.feature](../../../examples/white-rabbit/src/test/resources/features/the_white_rabbits_hexagon.feature),
  and [white-rabbit.textproto](../../../examples/white-rabbit/docs/generated/architecture/model/white-rabbit.textproto).

### Rule: the skill's input is the architecture rules, the offered steps, the story's features and its .textproto, and the published platform
### Rule: the client is done when its make check is green: the rules, its features, the platform's steps, and make arch-verify, its .textproto matching what the build writes
### Rule: the run works offline, from make platform-install

**Questions**
1. The client for the demo: a new story, generated from its features and its `.textproto`; the rehearsal's code
   kept in `<story>-backup` as the fallback. Decided 2026-10-29: the White Rabbit Story, `stories.whiterabbit`,
   its port `NoTimeToHaggle`, hears `Offer`, publishes `Order`, in `examples/white-rabbit`.

## Stories describe themselves

**Story:** as the platform, I read from each story what it hears, what it publishes, and on which key.

- [Story.java](../../../platform/src/main/java/dk/mathmagicians/playground/confluent/eventing/application/Story.java)
  names what a story reads, `listensTo()` and `hears()`; what it publishes lives in its body.
- jMolecules 2.0.1's `@PrimaryPort` carries `name` and `description`.
- The key per topic is the README's rule under Purpose, and `Envelope.key()` in the code.

### Rule: a story names the payload types it publishes, Story.publishes(), beside listensTo()
### Rule: a story's port carries its name and its description, @PrimaryPort(name, description)
### Rule: each topic's key strategy carries a name: by product, by region and product, by region, by customer

**Questions**
1. The home of a key strategy's name: the payload record, or `Topics`?
2. The static home of what a story hears and publishes: the writer of the DSL reads the code without starting
   it, and `listensTo()` answers at runtime. An annotation on the story, or a constant.
