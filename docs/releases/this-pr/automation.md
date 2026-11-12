# Automation

The tasks of each feature in the [catalog](release-0.0.9.md), each with its owner and the scenario it turns green.

## The catalog follows the board

`project.yaml` plays [Project sync](../archived/release-0.0.6/03_automation.md#project-sync-the-board-follows-the-catalog) on every
push, and the board's moves back into the catalog.

| Task | Owner | Turns green |
|---|---|---|
| `make project-links`: the catalogs committed and pushed when the sync changed them | pair | the links on the branch |
| `.github/workflows/project.yaml`: on `docs/releases/**` but `archived/**`, with `GITHUB_TOKEN`, issues and contents write | pair | the run green |

## A release ships what is ready

Owner: pair.

- `@wip` and `@manual` stay out of the run, 2026-10-21: the `bdd` task at
  [platform.gradle:126](../../../platform.gradle#L126), `SCENARIOS` in [platform.mk](../../../platform.mk), and the
  README's Testing. `make bdd-snippets` played the 17 scenarios, `SCENARIOS=@nothing` none.
- Open: the root `bdd`, which parses `src/test/resources/features/` and plays none of its `@manual` scenarios.

## Wonderland's lines: what a feature reuses from the platform

Owner: themathmagician for the features, pair for `make bdd-reuse` and the job summary. No task written yet.

## architecture-discovery renders its own diagrams

Owner: pair.

- The renderer, 2026-11-12:
  [DiagramRenderer](../../../architecture-discovery/src/main/java/dk/mathmagicians/playground/architecture/discovery/DiagramRenderer.java)
  with its test; PlantUML in [architecture-discovery/build.gradle](../../../architecture-discovery/build.gradle);
  `renderDiagrams` in [platform.gradle](../../../platform.gradle) runs it, the `plantuml` configuration and
  `diagramStyle` gone.
- The version and the release, 2026-11-12: the tool's version from the `v*` tags in
  [architecture-discovery/build.gradle](../../../architecture-discovery/build.gradle), `-Prelease` for the next
  version; `discovery-release` and `discovery-version` in the [Makefile](../../../Makefile);
  [architecture-discovery.yaml](../../../.github/workflows/architecture-discovery.yaml) runs `discovery-release`
  from `main` and `discovery-publish` from anywhere else. The workflow is checked by its run on the pushed
  branch, not played yet.
- The documents, 2026-11-12: the README's Repository layout and Git and CI,
  [architecture-discoverability.md](../../../architecture-discovery/architecture-discoverability.md), step 6 and
  its dependencies.
- The look, 2026-11-12, approved on a prototype the same day: a module coloured by its ring, a card by its role,
  the legend a card per role, in
  [hexagon.style.puml](../../../architecture-discovery/src/main/resources/hexagon.style.puml); the hexagon
  without the dependencies on the domain, the arrows inside it, and `publishes` and `subscribes`; its direction
  an option, left to right by default, `make arch-gen DIRECTION=tb` for top to bottom, through
  `-ParchitectureDirection` in [platform.gradle](../../../platform.gradle) and `ModuleDocumentation`; the
  stories and the platform drawn as shapes in the forest and the streams, the streams left to right.
  `HexagonDocumenterTest`, `ForestDocumenterTest`, `StreamsDocumenterTest`.
- Spec green, to sign: `make discovery-build`, 58 tests, then `make discovery-install` and `make build`, `make
  arch-verify`, `make arch-bdd`, and `make arch-check` green on 2026-11-12 against a local Maven repository
  holding the tool at `0.0.9-SNAPSHOT` alone; the renderer alone left
  `forest.svg`, `streams.svg`, `hexagon.svg`, and six module diagrams byte for byte as before, and the look then
  redrew the three levels. `make check` on
  `cli` and `kafka`, themathmagician's run, not played yet.
- Open: `examples/purse` draws with the tool it finds published or in the local Maven repository, the look of
  before, until `architecture-discovery.yaml` publishes; decided 2026-11-12, question 4 of the
  [map](discovery.md#architecture-discovery-renders-its-own-diagrams): it waits for the published tool.
- Open, themathmagician's: `0.1.0-SNAPSHOT` of the tool, version id 66480721 in GitHub Packages and a folder in
  the local Maven repository, deleted, so `latest.integration` takes `0.0.9-SNAPSHOT`; until then the root build
  resolves the tool of 2026-09-07 and does not compile.
- Open: the tool's version block is a second copy of [platform.gradle:16](../../../platform.gradle#L16); one
  home would be a script both apply.
- Open: a `cicd` on `main` that fails after the tool's workflow released leaves the tool at a version the
  repository never tagged, and the next release of the tool answers 409.
- Finding: `plantuml-mit` 1.2026.10 is published, the pin is 1.2026.8.

## Eventing over Cloudflare

| Task | Owner | Turns green |
|---|---|---|
| The publisher and the pull listener over the Queues HTTP API, `adapter/cloudflare` | themathmagician | the map's scenarios |
| The queues and their dead-letter queues in `iac/`, provider `cloudflare/cloudflare` | pair | the queues exist per environment |
| `TRANSPORT=cloudflare` in `platform.mk`, the account's token in `.env.cloudflare-<ENV>.private` and a GitHub environment | pair | `make run-tiny TRANSPORT=cloudflare` |
| The deck, "Same stories, three transports" with the fourth | pair | the slide shows four |
