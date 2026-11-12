# Discovery

The example map of each work package in the [catalog](release-0.0.8.md), trimmed to its story, its rules, and
where each lives now. Examples agreed 2026-10-29 on all four.

## The purse's architecture feature passes

**Story:** as the purse, a story playing in Wonderland, my architecture is a feature, and it passes.

The examples are the scenarios of
[the_purse_has_hexagonal_architecture.feature](../../../../examples/purse/src/test/resources/features/the_purse_has_hexagonal_architecture.feature).

- Rule: a story's architecture feature states every architecture annotation of the story, in the code's own
  names, on steps the library and the platform offer; a client writes the facts and no step. Decided 2026-10-30.
- Rule: an annotation the generated diagram carries that the feature does not state is red.
- Rule: a behaviour feature stays in domain language; the hexagon's words live in the architecture feature and
  the DSL.
- Rule: an `@architecture-discovery` feature plays without containers.
- Rule: a behaviour feature belongs to the story whose build it lives in; one that lives elsewhere says which
  story it drives, `@story:<name>`.

## architecture-discovery extracts the purse's .textproto

**Story:** as the build, I write a story's architecture as a `.textproto` from the code, and a reader sees what
the story subscribes to and publishes, its ports, and how Spring wires it.

The pipeline as built is the library's document,
[architecture-discoverability.md](../../../../architecture-discovery/architecture-discoverability.md); the DSL's
fields are [hexagon.proto](../../../../architecture-discovery/src/main/proto/hexagon.proto).

- Rule: the DSL is Protobuf text by `hexagon.proto` in architecture-discovery, the hexagon's words as its fields.
  Decided 2026-10-29 over PlantUML, a DSL of our own, Structurizr DSL, Mermaid, JSON, and Gherkin; its shape
  decided 2026-10-30.
- Rule: one `.textproto` per story and one for the platform, under `docs/generated/architecture/model`, named as
  `--story` names the story.
- Rule: the PlantUML is a view of the model, content only, its look in a style file. Decided 2026-10-29.
- Rule: the Hexagonal Architecture stereotypes and the Spring wiring are kept apart, in the model and in the
  steps. Decided 2026-10-30.
- Rule: the scenarios drive, they are not specified. Decided 2026-10-30.
- Rule: the three levels are drawn from the DSL: the forest, the streams, the hexagon of each.
- Decided 2026-10-30: what a story subscribes to and publishes is read without starting it, from the class
  objects `Story.subscribesTo()` and `publishes()` name.

## make arch-bdd and make arch-check check the DSL

**Story:** as CI, I am red when the DSL and the code disagree, or the DSL and the feature.

The targets are in `make help`, Architecture, and [platform.mk](../../../../platform.mk).

- Rule: `make arch-check` compares the DSL as specified with the DSL as built, layout aside; the PlantUML and the
  SVG are artifacts, not compared. Decided 2026-10-30.
- Rule: `make arch-bdd` plays the `@architecture-discovery` features, and the bdd targets leave them out.
- Rule: the three levels are targets of their own under `arch-gen`.
- Rule: the `ci` job and a client's cicd run both.

## The White Rabbit and the skill: the round trip

**Story:** as a client of the platform, I write the White Rabbit's features and its hexagon before its code, the
build says whether what was built is what was specified, and the skill does it again.

The spec, written 2026-10-29 before its code, is under
[examples/white-rabbit-1/src/test/resources](../../../../examples/white-rabbit-1/src/test/resources); the skill is
[wonderland-generate-story](../../../../.claude/skills/wonderland-generate-story/SKILL.md).

- Rule: the client is done when its `make check` is green, `make arch-verify` and `make arch-bdd` in it, its
  `.textproto` matching what the build writes.
- Rule: the skill's input is the architecture rules, the offered steps, the story's features and its
  `.textproto`, and the published platform.
- Rule: the run works offline, from `make platform-install`.
- Rule: the rehearsal's code is kept as `<story>-backup`, the fallback of the live demo.
- Decided 2026-10-29: the client for the demo is the White Rabbit Story, port `NoTimeToHaggle`, subscribes to
  `Offer`, publishes `Order`.
