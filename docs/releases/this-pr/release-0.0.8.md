# Release 0.0.8

**Date:** 2026-10-29 | **Updated:** 2026-10-29 | **Branch:** `dsl`

Four work packages, Discovery done 2026-10-29: the purse's architecture feature passes; architecture-discovery
extracts the purse's `.textproto`; `make arch-bdd` and `make arch-check` check the DSL; the White Rabbit and the
skill, the round trip. The spec's results: the branch's latest `cicd` run, its job summary with the scenarios, and
its `cucumber-report-<transport>` artifact; locally `build/reports/cucumber/index.html` in each project with
features, after `make bdd`. The release ends In production, [verification.md](verification.md).

## Catalog

| Feature | Issue | State | Examples agreed | Spec signed | Spec green | Map |
|---|---|---|---|---|---|---|
| The purse's architecture feature passes | [#22](https://github.com/Mathmagicians/wonderland-eventing-playground/issues/22) | Formulation | [2026-10-29](discovery.md#the-purses-architecture-feature-passes) | | | [discovery.md](discovery.md#the-purses-architecture-feature-passes) |
| architecture-discovery extracts the purse's .textproto | [#9](https://github.com/Mathmagicians/wonderland-eventing-playground/issues/9) | Formulation | [2026-10-29](discovery.md#architecture-discovery-extracts-the-purses-textproto) | | | [discovery.md](discovery.md#architecture-discovery-extracts-the-purses-textproto) |
| make arch-bdd and make arch-check check the DSL | [#23](https://github.com/Mathmagicians/wonderland-eventing-playground/issues/23) | Formulation | [2026-10-29](discovery.md#make-arch-bdd-and-make-arch-check-check-the-dsl) | | | [discovery.md](discovery.md#make-arch-bdd-and-make-arch-check-check-the-dsl) |
| The White Rabbit and the skill: the round trip | [#10](https://github.com/Mathmagicians/wonderland-eventing-playground/issues/10) | Formulation | [2026-10-29](discovery.md#the-white-rabbit-and-the-skill-the-round-trip) | | | [discovery.md](discovery.md#the-white-rabbit-and-the-skill-the-round-trip) |

A row without an issue gets one from `make gh-project-sync`. Slides alongside,
[#11](https://github.com/Mathmagicians/wonderland-eventing-playground/issues/11), is the pair's across the four:
each package fills its section of the deck as it lands, [automation.md](automation.md#slides-alongside). From
0.0.7: #9 and #10 renamed to their package; #8, Stories describe themselves, folded into the extraction as the
static `listensTo` and `publishes`; [release 0.0.7](../archived/release-0.0.7/release-spec-0.0.7.md).

## Stories

The stories of Wonderland in this release, each a hexagon beside the platform's, played by `--story=<name>`.

| Story | Port | Subscribes to | Publishes | Plays on | State |
|---|---|---|---|---|---|
| `load`, the generator, `stories/load` | `GenerateLoad` | nothing | products, offers, orders, one type per instance | `cli`, `kafka`; `confluent` once the cluster answers | playing, `bdd (cli)` and `bdd (kafka)` green 2026-10-29 |
| `tea-party`, `stories/tea-party` | `SettleAtTheTeaParty` | offers, orders | transactions | `cli`, `kafka` | playing, the same runs |
| `purse`, `examples/purse`, a client's story | `KnowWhatIsLeft` | transactions | nothing | `cli` in `cicd-client-example` | playing; its architecture feature and `purse.textproto` written 2026-10-29, red |
| `white-rabbit`, `examples/white-rabbit`, the demo's client | `NoTimeToHaggle` | offers | orders | | specified 2026-10-29: its two features and `white-rabbit.textproto`, no code yet |
