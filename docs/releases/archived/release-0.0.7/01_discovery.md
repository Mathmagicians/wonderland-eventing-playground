# Discovery

The example map of each feature in the [catalog](release-0.0.7.md): its story, its rules with examples, its
questions, and the decisions it raises for [docs/adr.md](../../adr.md).

## Messaging capability: cli, kafka, confluent

**Goal:** all three start locally with easy make targets: the log works, `kafka` starts a local container, and
`confluent` fails gracefully about its credentials.

- Confluent Cloud answers the keys of `confluent-test` and `confluent-prod` with `SaslAuthenticationException:
  Authentication failed`, in every `load-run` since 2026-09-29 12:08 UTC. The cluster stays out of service.
- `cli` publishes to the log, `confluent` to Confluent Cloud through `kafka.properties`, imported at
  [application-confluent.properties:2](../../../platform/src/main/resources/application-confluent.properties#L2);
  the Kafka adapters carry `@Profile("confluent")`, and `kafka` has no profile yet.
- 0.0.6 ships the transports `cli` and `confluent`, with
  [CI plays each transport](#ci-plays-each-transport), its matrix played on `cli`; 0.0.7
  adds `kafka`. Decided 2026-10-22.
- A transport is a profile, `cli`, `kafka`, `confluent`; an environment is the GitHub environment a run plays
  against or deploys to, `test` or `prod`, and a profile of its own naming the topics. A process runs
  `<transport>,<env>`. Decided 2026-10-21.

**Story:** I pick where a story plays, the log, a local broker, or Confluent Cloud, with one make variable.

### Rule: on cli the console feeds the story and the log receives what it publishes
- `make run-tiny`: a line `Published <id> to log-0 at offset <offset>, key <key>, region <region>: <payload>` at
  INFO, the payload as the console reads it, then the process ends.

### Rule: on kafka a broker and a schema registry run on the machine, with the topics and schemas iac/ gives Confluent Cloud
- `make run-tiny TRANSPORT=kafka` with nothing running: the broker and the registry start, the schemas register,
  and the generator's message lands on `test.offers`.
- `make run-tiny TRANSPORT=kafka` a second time: the broker is up already, and the offsets continue from the first
  run.
- `make kafka-universe-down`: the broker, the registry, and the viewer stop, and what they held is gone.

### Rule: on confluent the keys come from .env.confluent-ENV.private, and startup checks them
- `make run-tiny TRANSPORT=confluent` without `.env.confluent-test.private`: `APPLICATION FAILED TO START`, naming each
  missing variable, exit code 1.
- with `KAFKA_API_SECRET` alone missing: the message names `KAFKA_API_SECRET`.
- with the keys `.env.confluent-test.private` holds, which the cluster rejects: see question 2.

**Questions**
1. A process that names an environment alone, `SPRING_PROFILES_ACTIVE=test`: Spring's own failure for the missing
   `Publisher` bean, or a message naming `cli`, `kafka`, and `confluent`?
2. The keys the cluster rejects: startup fails on the first authentication error, or the story runs and logs each
   failed send at ERROR, as [KafkaPublisher.java:47](../../../platform/src/main/java/dk/mathmagicians/playground/confluent/eventing/adapter/kafka/publisher/KafkaPublisher.java#L47) does?
3. The Background `Given I have the API keys to the cluster`, in all five features: kept in all five as
   `Given I can connect to the eventing system`,
   [TransportSteps.java:14](../../../platform/src/testFixtures/java/dk/mathmagicians/playground/confluent/eventing/bdd/TransportSteps.java#L14);
   the step asks the transport played: on `confluent` the keys open the cluster, on `cli` the Docker host answers.
   Decided 2026-10-22.
4. Production is the environments Wonderland plays in, `test` and `prod`: which transport each plays on, with
   Confluent Cloud out of service?
5. The purse on `kafka`: the platform's `kafka-up` through `platform.mk`, or broker services in its own
   [compose.yaml](../../../examples/purse/compose.yaml)? The platform's, `kafka-universe-up`: the universe is a
   compose file of its own, `compose.kafka.yaml`, the broker, the registry, the viewer, and the one-shot services
   that create the topics and register the schemas, what `iac/` is for Confluent Cloud. Decided 2026-10-29.
6. The schema registry image: `confluentinc/cp-schema-registry` (Confluent, Confluent Community License), or
   Karapace, `ghcr.io/aiven-open/karapace` (Aiven, Apache 2.0)? Confluent's. Decided 2026-10-29.
7. `make run-tiny`, the quick start of the Definition of done: on `TRANSPORT`, default `cli`. Decided 2026-10-21.

**Decisions for docs/adr.md, by title**
- The transport is a profile beside the environment

Examples agreed 2026-10-29.

## Load on demand: the transport and its environment

**Story:** I play the load when I choose, on the transport I pick: in test from a branch, in prod from `main`.

Examples agreed 2026-10-20. In Automation at the archive of 0.0.6, moved here 2026-10-24.

### Rule: load-run.yaml runs on dispatch, with the transport confluent, kafka, or cloudflare, and the load arguments
- a dispatch with `transport=kafka` and no arguments: `make docker-smoke`, the minimum.
- a dispatch with arguments: `make docker-run` with them.
- the hour turns: nothing runs.

### Rule: from main the load plays in <transport>-prod, from any other branch in <transport>-test
- a dispatch from `eventing-without-confluent` with `transport=kafka`: the job runs in `kafka-test`, with `ENV=test`.
- a dispatch from `main` with `transport=confluent`: the job runs in `confluent-prod`, with `ENV=prod`.
- `<transport>-prod` takes deployments from `main` only; `make gh-main-protection` sets each transport's two
  environments.
- the `kafka-*` and `cloudflare-*` environments start empty.

## CI plays each transport

**Goal:** a matrix build for each of the transports, confluent excluded for now, with a comment about pricing.

- `cd` and `cicd-client-example` play the features against Confluent Cloud, which answers the keys with
  `SaslAuthenticationException` since 2026-09-29, see
  [Messaging capability](#messaging-capability-cli-kafka-confluent).
  `main` takes a merge with both green, admins included, [branch-protection.json](../../../.github/branch-protection.json).
- The suite's drivers start the image with the suite's profiles, `<transport>,<env>`, and the keys the environment
  has, [GeneratorContainer.java:78](../../../platform/src/testFixtures/java/dk/mathmagicians/playground/confluent/eventing/bdd/GeneratorContainer.java#L78)
  and [StoryContainer.java:62](../../../platform/src/testFixtures/java/dk/mathmagicians/playground/confluent/eventing/bdd/StoryContainer.java#L62).
  Under `cli` the image publishes to its log and its console feeds it.
- A job with a matrix reports a check per entry, `bdd (cli)`. The matrix is a `bdd` job of its own, so `ci` and
  `cd` keep the names branch protection requires, and protection stays as it is. Decided 2026-10-21.
- CI plays each transport in a matrix: confluent sits out for Confluent Cloud's pricing, kafka until its broker and
  schema registry run on the runner, and the five features move to cli. Decided 2026-10-21.
- What CI runs is driven by the Makefile: the transports CI plays are `CI_TRANSPORTS` in `platform.mk`, with the
  reason each other transport sits out, and the matrix reads them from `make ci-transports`. Decided 2026-10-21.

**Story:** CI plays the spec on each transport we run, and a transport that sits out says why.

Examples agreed 2026-10-22. In Automation at the archive of 0.0.6, moved here 2026-10-24.

### Rule: the bdd job plays a matrix of the transports; a transport that sits out carries its reason
- a pull request: `bdd (cli)` plays, then `cd` publishes the image, and `ci`, `cd`, and `cicd-client-example` are
  green.
- `make ci-transports`: `["cli"]`.
- confluent sits out: the comment at `CI_TRANSPORTS` names Confluent Cloud's pricing.
- kafka sits out: the same comment names the broker and the schema registry on the runner.

### Rule: each transport plays the scenarios it carries
- `make bdd`: plays as `TRANSPORT=cli`.
- `make bdd TRANSPORT=cli`: every scenario but `@kafka`, `@confluent`, `@wip`, and `@manual`.
- `make bdd TRANSPORT=kafka`: every scenario but `@confluent`, `@wip`, and `@manual`.
- `make bdd TRANSPORT=confluent`: every scenario but `@wip` and `@manual`.
- the Background `Given I can connect to the eventing system`: in every feature, checked on the transport played.

### Rule: on cli the console feeds the story and the log receives what it publishes
- the generator's feature on cli: the envelope's id and region in the image's log.
- the tea party and the purse on cli: the offers and the orders typed on the console, the transactions in the log.
- cli plays the image's `cli` profile, see [Messaging capability](#messaging-capability-cli-kafka-confluent).

**Questions**
1. The default of `TRANSPORT`: `cli`, which plays without a broker and without keys. Decided 2026-10-21.

**Decisions for docs/adr.md, by title**
- [ADR-001 CI plays each transport in a matrix](../../adr.md#adr-001-ci-plays-each-transport-in-a-matrix)
