# Formulation

Each feature's scenarios and architecture rules, by link, and the step skeletons, for the features of the
[catalog](release-spec-0.0.7.md) that passed Examples agreed. The evidence of Spec signed, the red run, is linked from the
feature's row in the catalog.

## Messaging capability: cli, kafka, confluent

| Task | Owner | Done when |
|---|---|---|
| The map's scenarios in `platform/src/test/resources/features/i_can_pick_where_a_story_plays.feature` | themathmagician | each example of the map is a scenario |
| An architecture rule: the Kafka adapters carry `@Profile("kafka")`, the console and log adapters `@Profile("cli")` | themathmagician | it fails on today's `@Profile("confluent")` |
| Step skeletons for the new steps, each throwing `PendingException` | pair | `make bdd-snippets` finds every step defined |

Spec signed 2026-10-29: the features play on `kafka` through the tag filter of the `bdd` task in
[platform.gradle](../../../platform.gradle), `@kafka` on
[the_cluster_has_my_topics_and_schemas.feature](../../../platform/src/test/resources/features/the_cluster_has_my_topics_and_schemas.feature)
and `@confluent` on its Flink outline. The red run:
[40804413757](https://github.com/Mathmagicians/wonderland-eventing-playground/actions/runs/40804413757), `bdd (kafka)`
with `bootstrap.servers` unset, the environment's empty variables.

## Load on demand: the transport and its environment

Spec signed 2026-10-21: the Git and CI bullet on `load-run.yaml` in the README, `.github/environment-test.json`
and `environment-prod.json`, applied by `make gh-main-protection`. The red run,
[39508585975](https://github.com/Mathmagicians/wonderland-eventing-playground/actions/runs/39508585975), from
`eventing-without-confluent` with `transport=kafka`, deployed to `kafka-test` with `ENV=test` and skipped the load:
the registry holds no `latest` under the new image name until `main` builds it.

## CI plays each transport

Spec signed 2026-10-22: `make bdd` on cli, red before `Transport`; the features tagged by the transport they need,
the rest playing on cli as written.
