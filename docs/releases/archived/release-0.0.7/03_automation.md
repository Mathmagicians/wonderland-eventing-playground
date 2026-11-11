# Automation

The tasks of each feature in the [catalog](release-spec-0.0.7.md), each with its owner and the scenario it turns green,
and the core snippets. One go per change. The evidence of Spec green, the green run and the ADRs a feature touches,
is linked from the feature's row in the catalog.

## Messaging capability: cli, kafka, confluent

Owner: pair for the properties and the plumbing, themathmagician for the annotations and the drivers. Turns green:
every scenario of the map on `kafka`. Done 2026-10-29:

- The profiles: the group `spring.profiles.group.confluent=kafka` in
  [platform.properties](../../../platform/src/main/resources/platform.properties);
  [application-kafka.properties](../../../platform/src/main/resources/application-kafka.properties) imports
  [kafka.properties](../../../platform/src/main/resources/kafka.properties), the local broker and registry by
  default; [application-confluent.properties](../../../platform/src/main/resources/application-confluent.properties)
  holds SASL and the registry's basic auth. The Kafka adapters carry `@Profile("kafka")`, the console and log
  adapters `@Profile("cli")`.
- The universe, [compose.kafka.yaml](../../../compose.kafka.yaml): `apache/kafka:4.2.1`,
  `confluentinc/cp-schema-registry:8.3.1`, `ghcr.io/kafbat/kafka-ui:v1.5.0`; its one-shot services `topics` and
  `schemas` create the topics of ENV with their dead-letter twins from [iac/locals.tf](../../../iac/locals.tf) and
  register the schemas as [iac/schemas.tf](../../../iac/schemas.tf) does, a second run adding nothing.
- `TRANSPORT` in [platform.mk](../../../platform.mk): `NEEDS` brings the universe to `run`, `up`, `docker-run`,
  `bdd`, and `bdd-published` on `kafka`; `kafka-universe-up` and `kafka-universe-down`; a story's build gets them
  through its include.
- The spec on a transport: the `bdd` task in [platform.gradle](../../../platform.gradle) takes the transport, the
  profiles, and on `kafka` the universe's network; `GeneratorContainer.onTransport()` and `StoryContainer` put every
  container on it; `ClusterTransport` plays on `kafka`. A transport's keys are its environment's,
  `<transport>-<env>`, `WITH_KEYS` dropping what the environment exports empty. `CI_TRANSPORTS := cli kafka`, the
  `bdd` job playing each to the end, `fail-fast: false` in [cicd.yaml](../../../.github/workflows/cicd.yaml);
  `confluent` joins `CI_TRANSPORTS` when the cluster answers.

### Open

| Task | Owner | Turns green |
|---|---|---|
| `ConfluentKeys`, the `confluent.*` values bound by the `@ConfigurationPropertiesScan` at [EventingPlatform.java:12](../../../platform/src/main/java/dk/mathmagicians/playground/confluent/EventingPlatform.java#L12) as `Topics` is, with `ConfluentKeysTest` | themathmagician | on `confluent` without keys, startup names each missing variable |
| [ConsoleListener.java:258](../../../platform/src/main/java/dk/mathmagicians/playground/confluent/eventing/adapter/cli/console/ConsoleListener.java#L258) hands the story what `story.hears`, as [StoryListener.java:64](../../../platform/src/main/java/dk/mathmagicians/playground/confluent/eventing/adapter/kafka/consumer/StoryListener.java#L64) does | themathmagician | `ConsoleListenerTest`: an envelope of another region is not heard |
| [ConsoleListener.java:186](../../../platform/src/main/java/dk/mathmagicians/playground/confluent/eventing/adapter/cli/console/ConsoleListener.java#L186) keeps the region a `region:` line names as typed, without `toUpperCase` | themathmagician | `ConsoleListenerTest`: `region: Apac` stamps `Apac` |

Spec green 2026-10-29: `make bdd TRANSPORT=kafka` green on the platform's and the tea party's features, and run
[40851714017](https://github.com/Mathmagicians/wonderland-eventing-playground/actions/runs/40851714017), `bdd (cli)`
and `bdd (kafka)` green in `cli-test` and `kafka-test`, each with its summary and its `cucumber-report-<transport>`.

- The JDK's restricted-method warning on lz4-java's native load, once per JVM that publishes, is silenced by
  `--enable-native-access=ALL-UNNAMED` in `platform.gradle`: on the `Test` tasks, on `bootRun`, and appended to the
  image's `JAVA_TOOL_OPTIONS` through `bootBuildImage`.

## Load on demand: the transport and its environment

Owner: pair. Turns green: the dispatches of the map.

- [load-run.yaml](../../../.github/workflows/load-run.yaml): `workflow_dispatch` only; `transport` and the branch
  pick `<transport>-<test|prod>`, prod from `main`, 2026-10-20.
- The environments: `TRANSPORTS` in the [Makefile](../../../Makefile), `.github/environment-test.json` and
  `environment-prod.json`, applied by `make gh-main-protection`, 2026-10-20.
- `TRANSPORT` reaches make, [load-run.yaml:28](../../../.github/workflows/load-run.yaml#L28), and `platform.mk`
  plays it, 2026-10-22.
- The dispatch
  [39694484209](https://github.com/Mathmagicians/wonderland-eventing-playground/actions/runs/39694484209) from `main`
  on 2026-10-21, `make docker-smoke` with `latest` in `cli-prod`, green; from the branch,
  [39508585975](https://github.com/Mathmagicians/wonderland-eventing-playground/actions/runs/39508585975) deployed to
  `kafka-test` with `ENV=test` and skipped the load, the registry holding no `latest` yet.
- Spec green 2026-10-29 on the transports that play: `cli`, the dispatch from `main` above. A dispatch on `kafka`
  waits for the universe on the runner; the `cloudflare-*` environments fill when 0.0.8 plays.

## CI plays each transport

Owner: pair. Done 2026-10-22:

- `TRANSPORT` in [platform.mk](../../../platform.mk), default `cli`, the profiles `<transport>,<env>` to the `bdd`
  task, which leaves out what the transport does not carry; `TRANSPORTS`, `CI_TRANSPORTS`, and `make ci-transports`.
- The drivers' port
  [Transport](../../../platform/src/testFixtures/java/dk/mathmagicians/playground/confluent/eventing/bdd/Transport.java),
  one bean per transport: `ClusterTransport` on `kafka` and `confluent`, `ConsoleTransport` on `cli`;
  `LoggingPublisher` logs the receipt, the key, the region, and the payload as the console reads it,
  `LoggingPublisherTest`, `ConverterTest`.
- `cli` in `load-run.yaml`'s choice with `cli-test` and `cli-prod`; the `bdd` job of
  [cicd.yaml](../../../.github/workflows/cicd.yaml), a matrix over `make ci-transports` before `cd`;
  `cicd-client-example.yaml` plays the purse in `cli-test`.
- The merge of 0.0.6, run
  [39692749878](https://github.com/Mathmagicians/wonderland-eventing-playground/actions/runs/39692749878) on `main`,
  2026-10-21: `ci`, `bdd (cli)` in `cli-test`, `cd`, and `tag` green, `make ci-transports` printing `["cli"]`.
- Spec green 2026-10-29: run
  [40851714017](https://github.com/Mathmagicians/wonderland-eventing-playground/actions/runs/40851714017), `bdd (cli)`
  and `bdd (kafka)` green, `make ci-transports` printing `["cli","kafka"]`; `confluent` joins `CI_TRANSPORTS` when
  the cluster answers.
