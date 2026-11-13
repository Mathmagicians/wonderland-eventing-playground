# Automation

What carries each work package of the [catalog](release-0.0.8.md), and its gate. How the pieces work together is
the library's document,
[architecture-discoverability.md](../../../../architecture-discovery/architecture-discoverability.md).

## The purse's architecture feature passes

Owner: pair.

- The steps, in the platform's test fixtures, package `architecture.bdd`:
  [HexagonSteps](../../../../platform/src/testFixtures/java/dk/mathmagicians/wonderland/architecture/bdd/HexagonSteps.java),
  [WonderlandSteps](../../../../platform/src/testFixtures/java/dk/mathmagicians/wonderland/architecture/bdd/WonderlandSteps.java),
  over [ArchitectureModel](../../../../platform/src/testFixtures/java/dk/mathmagicians/wonderland/architecture/bdd/ArchitectureModel.java).
- The suite: [ArchBddTest](../../../../examples/purse/src/test/java/dk/mathmagicians/wonderland/examples/stories/purse/bdd/ArchBddTest.java),
  played by `make arch-bdd`.
- Spec green 2026-10-30: `make arch-bdd` in `examples/purse`, the five scenarios green against the
  `purse.textproto` `make arch-gen` wrote from the code the same day.

## architecture-discovery extracts the purse's .textproto

Owner: pair; `Story.subscribesTo()` and `publishes()` themathmagician's.

- The model: [hexagon.proto](../../../../architecture-discovery/src/main/proto/hexagon.proto),
  [ModelWriter](../../../../architecture-discovery/src/main/java/dk/mathmagicians/playground/architecture/model/ModelWriter.java),
  `Stories`, `TextProtoWriter`, each with its test.
- The views: `HexagonDocumenter`,
  [ForestDocumenter](../../../../architecture-discovery/src/main/java/dk/mathmagicians/playground/architecture/discovery/ForestDocumenter.java),
  [StreamsDocumenter](../../../../architecture-discovery/src/main/java/dk/mathmagicians/playground/architecture/discovery/StreamsDocumenter.java),
  each with its test; the look in
  [hexagon.style.puml](../../../../architecture-discovery/src/main/resources/hexagon.style.puml).
- The specifications: [app/src/test/resources/architecture](../../../../app/src/test/resources/architecture),
  `platform`, `load`, `tea-party`.
- `HexagonReader` retired 2026-10-30.
- Spec green 2026-10-30: `make arch-check` green at the root.
- Finding, closed 2026-11-12: a `ci` that resolved the library before `architecture-discovery.yaml` published it
  failed `diagramStyle`. The task is gone: the library renders its own diagrams, `DiagramRenderer`; 0.0.9,
  architecture-discovery renders its own diagrams.

## make arch-bdd and make arch-check check the DSL

Owner: pair.

- [ModelCheck](../../../../architecture-discovery/src/main/java/dk/mathmagicians/playground/architecture/model/ModelCheck.java)
  behind `archCheck` in [platform.gradle](../../../../platform.gradle); `arch-check`, `arch-bdd`, `arch-forest`,
  `arch-streams`, `arch-hexagon` in [platform.mk](../../../../platform.mk).
- The purse's specification:
  [purse.textproto](../../../../examples/purse/src/test/resources/architecture/purse.textproto).
- Spec green 2026-10-30: `make check` carries `arch-verify` and `arch-bdd`, in `cicd.yaml` and
  `cicd-client-example.yaml`.

## The White Rabbit and the skill: the round trip

Owner: themathmagician; the build's scaffold and the brief pair.

- The specification:
  [white-rabbit.textproto](../../../../examples/white-rabbit-1/src/test/resources/architecture/white-rabbit.textproto);
  the red runs of 2026-10-30 in `examples/white-rabbit-1`.
- The skill: [wonderland-generate-story](../../../../.claude/skills/wonderland-generate-story/SKILL.md), written
  2026-10-30.
- Closed 2026-11-12: a demo example, played for the talk and done; the story, the offline run, and the rehearsal
  close with it.

## Slides alongside

Owner: pair, [#11](https://github.com/Mathmagicians/wonderland-eventing-playground/issues/11). The deck is
[slides/slides.md](../../../../slides/slides.md), played by `make slidev-present`; its sections Three levels and
The agent round trip are in.
