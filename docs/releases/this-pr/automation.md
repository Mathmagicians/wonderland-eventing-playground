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
- The composite, 2026-11-12: `includeBuild` in [settings.gradle](../../../settings.gradle); `discovery-install`
  gone from the [Makefile](../../../Makefile).
- The documents, 2026-11-12: the README's Repository layout and Git and CI,
  [architecture-discoverability.md](../../../architecture-discovery/architecture-discoverability.md), step 6 and
  its dependencies.
- Spec green, to sign: `make discovery-build`, `make build` with an empty local Maven repository, `make
  arch-verify`, `make arch-bdd`, and `make arch-check` green on 2026-11-12; after `make arch-gen`, `forest.svg`,
  `streams.svg`, `hexagon.svg`, and six module diagrams byte for byte as before, at the root and in
  `examples/purse`; a change to a class of the tool compiled by the next `make arch-gen`. `make check` on `cli`
  and `kafka`, themathmagician's run, not played yet.
- Open: the tool's release. Proposed: its own tags, `architecture-discovery-v<version>`, read as
  [platform.gradle:16](../../../platform.gradle#L16) reads `v*`; `architecture-discovery.yaml` tags the next patch
  on `main` and publishes it, a pull request publishes the snapshot, and the first release is a dispatch with
  `0.1.0`.
- Open: the `bdd` job's token comment in [cicd.yaml](../../../.github/workflows/cicd.yaml), which names the tool
  from GitHub Packages; the two `env` lines may go, checked by a pushed run.
- Finding: `plantuml-mit` 1.2026.10 is published, the pin is 1.2026.8.

## Eventing over Cloudflare

| Task | Owner | Turns green |
|---|---|---|
| The publisher and the pull listener over the Queues HTTP API, `adapter/cloudflare` | themathmagician | the map's scenarios |
| The queues and their dead-letter queues in `iac/`, provider `cloudflare/cloudflare` | pair | the queues exist per environment |
| `TRANSPORT=cloudflare` in `platform.mk`, the account's token in `.env.cloudflare-<ENV>.private` and a GitHub environment | pair | `make run-tiny TRANSPORT=cloudflare` |
| The deck, "Same stories, three transports" with the fourth | pair | the slide shows four |
