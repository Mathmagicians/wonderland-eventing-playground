# Release 0.0.6

**Date:** 2026-10-20 | **Updated:** 2026-10-23 | **Branch:** `eventing-without-confluent`, merged as
[#6](https://github.com/Mathmagicians/wonderland-eventing-playground/pull/6) on 2026-10-21

The plumbing, shipped as soon as it was ready. In production 2026-10-23, [verification.md](04_verification.md):
`v0.0.6` on the merge, its run
[39692749878](https://github.com/Mathmagicians/wonderland-eventing-playground/actions/runs/39692749878) with `ci`,
`bdd (cli)`, `cd`, and `tag` green, the scenarios in the `bdd (cli)` job summary and its `cucumber-report-cli`
artifact.

## Catalog

| Feature | Issue | State | Examples agreed | Spec signed | Spec green | Map |
|---|---|---|---|---|---|---|
| Slides spike: the talk plays from compose | [#14](https://github.com/Mathmagicians/wonderland-eventing-playground/issues/14) | Archive | | | [2026-10-20](03_automation.md#slides-spike-the-talk-plays-from-compose) | [automation.md](03_automation.md#slides-spike-the-talk-plays-from-compose) |
| One name: wonderland-eventing-playground | [#15](https://github.com/Mathmagicians/wonderland-eventing-playground/issues/15) | Archive | [2026-10-21](01_discovery.md#one-name-wonderland-eventing-playground) | | [2026-10-21](03_automation.md#one-name-wonderland-eventing-playground) | [discovery.md](01_discovery.md#one-name-wonderland-eventing-playground) |
| Project sync: the board follows the catalog | [#16](https://github.com/Mathmagicians/wonderland-eventing-playground/issues/16) | Archive | [2026-10-21](01_discovery.md#project-sync-the-board-follows-the-catalog) | [2026-10-21](02_formulation.md#project-sync-the-board-follows-the-catalog) | [2026-10-22](03_automation.md#project-sync-the-board-follows-the-catalog) | [discovery.md](01_discovery.md#project-sync-the-board-follows-the-catalog) |

Load on demand, #13, and CI plays each transport, #19, were in Automation at the archive and moved to 0.0.7 in that
state, with their gate dates and their sections.
