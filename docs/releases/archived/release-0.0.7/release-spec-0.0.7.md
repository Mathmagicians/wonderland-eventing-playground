# Release 0.0.7

**Date:** 2026-10-21 | **Updated:** 2026-10-29 | **Branch:** `eventing-with-kafka`, merged as
[#21](https://github.com/Mathmagicians/wonderland-eventing-playground/pull/21) on 2026-10-29

The kafka transport and its universe, a transport's keys in its environment, and CI playing each transport. In
production 2026-10-29, [verification.md](04_verification.md): `v0.0.7` on the merge, its run
[40914163544](https://github.com/Mathmagicians/wonderland-eventing-playground/actions/runs/40914163544) with `ci`,
`bdd (cli)`, `bdd (kafka)`, `cd`, and `tag` green, the scenarios in each `bdd` job's summary and its
`cucumber-report-<transport>` artifact.

## Catalog

| Feature | Issue | State | Examples agreed | Spec signed | Spec green | Map |
|---|---|---|---|---|---|---|
| Messaging capability: cli, kafka, confluent | [#7](https://github.com/Mathmagicians/wonderland-eventing-playground/issues/7) | Archive | [2026-10-29](01_discovery.md#messaging-capability-cli-kafka-confluent) | [2026-10-29](02_formulation.md#messaging-capability-cli-kafka-confluent) | [2026-10-29](03_automation.md#messaging-capability-cli-kafka-confluent) | [discovery.md](01_discovery.md#messaging-capability-cli-kafka-confluent) |
| Load on demand: the transport and its environment | [#13](https://github.com/Mathmagicians/wonderland-eventing-playground/issues/13) | Archive | [2026-10-20](01_discovery.md#load-on-demand-the-transport-and-its-environment) | [2026-10-21](02_formulation.md#load-on-demand-the-transport-and-its-environment) | [2026-10-29](03_automation.md#load-on-demand-the-transport-and-its-environment) | [discovery.md](01_discovery.md#load-on-demand-the-transport-and-its-environment) |
| CI plays each transport | [#19](https://github.com/Mathmagicians/wonderland-eventing-playground/issues/19) | Archive | [2026-10-22](01_discovery.md#ci-plays-each-transport) | [2026-10-22](02_formulation.md#ci-plays-each-transport) | [2026-10-29](03_automation.md#ci-plays-each-transport) | [discovery.md](01_discovery.md#ci-plays-each-transport) |

The rows the release did not ship moved on 2026-10-29 in their states: Stories describe themselves, #8,
Architecture discovery, #9, The skill generate-platform-client, #10, and Slides alongside, #11, to 0.0.8,
`this-pr/`; The catalog follows the board, #17, A release ships what is ready, #18, and Wonderland's lines, #20,
to 0.0.9.
