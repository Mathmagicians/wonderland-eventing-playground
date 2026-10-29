# Automation

The tasks of each feature in the [catalog](release-spec-0.0.6.md), each with its owner and the scenario it turns
green. One go per change. The evidence of Spec green, the green run or what themathmagician saw, and the ADRs a
feature touches, is linked from the feature's row in the catalog.

## Slides spike: the talk plays from compose

Owner: pair.

- The talk plays in the `slides` service of [compose.yaml](../../../compose.yaml), from
  [slides/Dockerfile](../../../slides/Dockerfile); Node and Chromium live in the image. `make slides-present` opens
  the browser, `make slides-pdf` exports.
- One compose plays Wonderland and the talk, signed 2026-10-20.
- Spec green, signed 2026-10-20: themathmagician played the talk, "y it works nicely", and `make slides-pdf` wrote
  `docs/generated/slides/architecture-discoverability.pdf` from the container.

## One name: wonderland-eventing-playground

Owner: pair; the repository renamed by themathmagician. Turns green: the rule of the map.

- The repository `Mathmagicians/wonderland-eventing-playground`, renamed 2026-10-21.
- The spellings, 2026-10-21: the badges in the [README](../../../README.md) and
  [examples/purse/README.md](../../../examples/purse/README.md), `IMAGE` in the [Makefile](../../../Makefile),
  [compose.yaml](../../../compose.yaml), [platform.gradle:45](../../../platform.gradle#L45),
  [architecture-discovery/build.gradle:57](../../../architecture-discovery/build.gradle#L57),
  [settings.gradle:1](../../../settings.gradle#L1),
  [app/.../application.properties:1](../../../app/src/main/resources/application.properties#L1), and
  `EventFixtures.APP`.
- Spec green, signed 2026-10-21: `grep -rIi confluent-eventing-playground` finds no file outside the build output,
  and `make build` is green.
- Open on 2026-10-23, themathmagician's: `origin` at the new name, and the Terraform Cloud workspace's VCS
  connection. The ghcr package is private after the first push, 3 versions, where the README's `docker run` of
  `ghcr.io/mathmagicians/wonderland-eventing-playground:latest` needs it public.

## Project sync: the board follows the catalog

| Task | Owner | Turns green |
|---|---|---|
| The Issue column in each catalog, after Feature | pair | done 2026-10-21: every row of 0.0.6, 0.0.7, and 0.0.8 linked |
| `make gh-project-sync`: the missing issues and milestones, each body from the phase files, the issue's milestone from the overview's file name, the issue's Phase | pair | done 2026-10-21: the first run created issues #7 to #19 and the milestones 0.0.6, 0.0.7, and 0.0.8 |
| The issue field `phase`, 2026-10-21; the board's columns by phase, its swimlanes by milestone, and its auto-add workflow on `is:issue` | themathmagician | done 2026-10-21: the board shows a column per phase and a swimlane per release |
| `plan-and-supervise`: the overview's Issue column and the board | pair | done 2026-10-22: the skill says what the overview holds and when the board is synced |

Spec green 2026-10-22: `make gh-project-sync`.
