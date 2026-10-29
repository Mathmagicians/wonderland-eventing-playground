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

## Eventing over Cloudflare

| Task | Owner | Turns green |
|---|---|---|
| The publisher and the pull listener over the Queues HTTP API, `adapter/cloudflare` | themathmagician | the map's scenarios |
| The queues and their dead-letter queues in `iac/`, provider `cloudflare/cloudflare` | pair | the queues exist per environment |
| `TRANSPORT=cloudflare` in `platform.mk`, the account's token in `.env.cloudflare-<ENV>.private` and a GitHub environment | pair | `make run-tiny TRANSPORT=cloudflare` |
| The deck, "Same stories, three transports" with the fourth | pair | the slide shows four |
