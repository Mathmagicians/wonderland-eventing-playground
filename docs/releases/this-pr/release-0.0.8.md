# Release 0.0.8

**Date:** 2026-10-29 | **Updated:** 2026-10-29 | **Branch:** `dsl`

Architecture discoverability and verification: the purse's architecture feature passes, architecture-discovery
writes the purse's `.textproto` from the generated PlantUML, `make arch-verify` and `make arch-check` check it, the
White Rabbit is built from its spec, and the skill does it again. The spec's results: the branch's latest `cicd`
run, its job summary with the scenarios, and its `cucumber-report-<transport>` artifact; locally
`build/reports/cucumber/index.html` in each project with features, after `make bdd`. The release ends In
production, [verification.md](verification.md).

## Catalog

| Feature | Issue | State | Examples agreed | Spec signed | Spec green | Map |
|---|---|---|---|---|---|---|
| Architecture discovery: one model, three levels | [#9](https://github.com/Mathmagicians/wonderland-eventing-playground/issues/9) | Discovery | | | | [discovery.md](discovery.md#architecture-discovery-one-model-three-levels) |
| The skill generate-platform-client | [#10](https://github.com/Mathmagicians/wonderland-eventing-playground/issues/10) | Discovery | | | | [discovery.md](discovery.md#the-skill-generate-platform-client) |
| Stories describe themselves | [#8](https://github.com/Mathmagicians/wonderland-eventing-playground/issues/8) | Discovery | | | | [discovery.md](discovery.md#stories-describe-themselves) |
| Slides alongside | [#11](https://github.com/Mathmagicians/wonderland-eventing-playground/issues/11) | Automation | | | | [automation.md](automation.md#slides-alongside) |

#8, #9, #10, and #11 came from 0.0.7 on 2026-10-29 in their states, [release 0.0.7](../archived/release-0.0.7/release-spec-0.0.7.md).

## Stories

The stories of Wonderland in this release, each a hexagon beside the platform's, played by `--story=<name>`.

| Story | Port | Hears | Publishes | Plays on | State |
|---|---|---|---|---|---|
| `load`, the generator, `stories/load` | `GenerateLoad` | nothing | products, offers, orders, one type per instance | `cli`, `kafka`; `confluent` once the cluster answers | playing, `bdd (cli)` and `bdd (kafka)` green 2026-10-29 |
| `tea-party`, `stories/tea-party` | `SettleAtTheTeaParty` | offers, orders | transactions | `cli`, `kafka` | playing, the same runs |
| `purse`, `examples/purse`, a client's story | `KnowWhatIsLeft` | transactions | nothing | `cli` in `cicd-client-example` | playing; its architecture feature and `purse.textproto` written 2026-10-29, red, [#9](automation.md#architecture-discovery-one-model-three-levels) |
| `white-rabbit`, `examples/white-rabbit`, the demo's client | `NoTimeToHaggle` | offers | orders | | specified 2026-10-29: its two features and `white-rabbit.textproto`, no code yet, [#10](automation.md#the-skill-generate-platform-client) |
