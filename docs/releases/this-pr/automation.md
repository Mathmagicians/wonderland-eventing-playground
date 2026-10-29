# Automation

The tasks of each feature in the [catalog](release-0.0.7.md), each with its owner and the scenario it turns green,
and the core snippets. One go per change. The evidence of Spec green, the green run and the ADRs a feature touches,
is linked from the feature's row in the catalog.

## Messaging capability: cli, kafka, confluent

### Profiles: cli, kafka, confluent

Owner: pair for the properties, themathmagician for the annotations. Turns green: every scenario of the map.

The properties, done 2026-10-29: the group `spring.profiles.group.confluent=kafka` in
[platform.properties](../../../platform/src/main/resources/platform.properties);
[application-kafka.properties](../../../platform/src/main/resources/application-kafka.properties) imports
[kafka.properties](../../../platform/src/main/resources/kafka.properties), whose broker and registry default to the
local ones; [application-confluent.properties](../../../platform/src/main/resources/application-confluent.properties)
holds SASL, the registry's basic auth, and the `confluent.*` values `ConfluentKeys` checks.
`application-test.properties` and `application-prod.properties` hold the topic names.

The console and log adapters carry `@Profile("cli")`. The Kafka adapters' annotations:

| File | Current | Replacement |
|---|---|---|
| [KafkaPublisher.java:26](../../../platform/src/main/java/dk/mathmagicians/playground/confluent/eventing/adapter/kafka/publisher/KafkaPublisher.java#L26), [StoryListener.java:26](../../../platform/src/main/java/dk/mathmagicians/playground/confluent/eventing/adapter/kafka/consumer/StoryListener.java#L26), [Reader.java:18](../../../platform/src/main/java/dk/mathmagicians/playground/confluent/eventing/adapter/kafka/consumer/Reader.java#L18), [DeadLetters.java:23](../../../platform/src/main/java/dk/mathmagicians/playground/confluent/eventing/adapter/kafka/consumer/DeadLetters.java#L23), [Topics.java:14](../../../platform/src/main/java/dk/mathmagicians/playground/confluent/eventing/adapter/kafka/Topics.java#L14) | `@Profile("confluent")` | `@Profile("kafka")` |

### ConfluentKeys, checked at startup

Owner: themathmagician, with `ConfluentKeysTest`. Turns green: on `confluent` without keys, startup names each
missing variable.

`platform/src/main/java/.../eventing/adapter/kafka/ConfluentKeys.java`, bound by the `@ConfigurationPropertiesScan`
at [EventingPlatform.java:12](../../../platform/src/main/java/dk/mathmagicians/playground/confluent/EventingPlatform.java#L12)
as `Topics` is. Spring Boot reports the failed binding as `APPLICATION FAILED TO START` with the message:

```java
/// The Confluent Cloud keys, by the names of `.env.confluent-<ENV>.private` and the GitHub environments. Startup fails naming
/// every variable that is missing.
@ConfigurationProperties("confluent")
@Profile("confluent")
public record ConfluentKeys(String bootstrapServers, String apiKey, String apiSecret,
                            String registryEndpoint, String registryApiKey, String registryApiSecret) {

    public ConfluentKeys {
        var missing = Stream.of(
                        new Variable("KAFKA_BOOTSTRAP_SERVERS", bootstrapServers),
                        new Variable("KAFKA_API_KEY", apiKey),
                        new Variable("KAFKA_API_SECRET", apiSecret),
                        new Variable("SCHEMA_REGISTRY_REST_ENDPOINT", registryEndpoint),
                        new Variable("SCHEMA_REGISTRY_API_KEY", registryApiKey),
                        new Variable("SCHEMA_REGISTRY_API_SECRET", registryApiSecret))
                .filter(Variable::isMissing)
                .map(Variable::name)
                .toList();
        if (!missing.isEmpty()) {
            throw new IllegalArgumentException("Confluent Cloud needs " + String.join(", ", missing));
        }
    }

    private record Variable(String name, @Nullable String value) {
        boolean isMissing() {
            return value == null || value.isBlank();
        }
    }
}
```

### The local broker, registry, and topic viewer

Owner: pair. Turns green: the `kafka` examples. Done 2026-10-29: [compose.kafka.yaml](../../../compose.kafka.yaml),
a compose file of its own under the flock's project name, so both share one network: `apache/kafka:4.2.1` (Apache
Software Foundation, Apache 2.0), one KRaft node with topic auto-creation off; `confluentinc/cp-schema-registry:8.3.1`
(Confluent, Confluent Community License); `ghcr.io/kafbat/kafka-ui:v1.5.0` (Kafbat, Apache 2.0), the topic viewer on
localhost:8082. The host reaches the broker on localhost:9092 and the registry on localhost:8081, the flock on
`kafka:19092` and `registry:8081`.

### TRANSPORT in platform.mk

Owner: pair. Turns green: the make examples of the map. Done 2026-10-29 in [platform.mk](../../../platform.mk):
`KAFKA_COMPOSE`, `KAFKA_NETWORK`, `FLOCK_KAFKA`, `ON_KAFKA`, and `NEEDS`; `run`, `up`, `docker-run`, `bdd`, and
`bdd-published` take `$(NEEDS)`, `docker-run` joins the broker's network, `ON_TRANSPORT` hands it to the `bdd` task;
the Kafka universe section, `kafka-universe-up` and `kafka-universe-down`. A story's build gets them through its
include, the compose file found beside `platform.mk`.

### The topics and schemas, the universe's

Owner: pair. Turns green: the schema step of
[i_can_generate_events.feature](../../../platform/src/test/resources/features/i_can_generate_events.feature) on
`kafka`. Done 2026-10-29: compose holds the registration, as `iac/` does for Confluent Cloud. The one-shot services
`topics` and `schemas` of [compose.kafka.yaml](../../../compose.kafka.yaml), run by `kafka-universe-up` after the
broker and the registry are healthy: the topics of ENV and their dead-letter twins, their names and partition count
from [iac/locals.tf](../../../iac/locals.tf) through make, created with `kafka-topics.sh --if-not-exists`; the
schemas as [iac/schemas.tf](../../../iac/schemas.tf) registers them, `<payload>.proto` under
`<env>.<payload>s-value`, the transaction with its imports as references, posted from the mounted `src/main/proto`.
A second run creates and registers nothing new.

### The spec on a transport

Owner: pair for the Gradle wiring, themathmagician for the drivers. Turns green: every feature on `kafka`.

Done 2026-10-29. The `bdd` task in [platform.gradle](../../../platform.gradle) takes the transport and the profiles
from `make bdd`, and on `kafka` the universe's network and the addresses on it, `bdd.network` and `bdd.flock`.
`GeneratorContainer.onTransport()` in
[GeneratorContainer.java](../../../platform/src/testFixtures/java/dk/mathmagicians/playground/confluent/eventing/bdd/GeneratorContainer.java)
puts every container of the image on the suite's transport, the story's in
[StoryContainer.java](../../../platform/src/testFixtures/java/dk/mathmagicians/playground/confluent/eventing/bdd/StoryContainer.java)
too; `ClusterTransport` plays on `kafka`, Confluent Cloud through the group. A transport's keys are its
environment's, `<transport>-<env>`: on GitHub the environment, locally `.env.<transport>-<env>.private`,
`WITH_KEYS` in [platform.mk](../../../platform.mk), which drops a variable the environment exports empty; `cli`
and `kafka` have neither. The `bdd` job plays every transport of `CI_TRANSPORTS` to the end, `fail-fast: false`
in [cicd.yaml](../../../.github/workflows/cicd.yaml), each with its summary and report.

- `CI_TRANSPORTS := cli kafka` in [platform.mk](../../../platform.mk), the universe on the runner for the `bdd`
  job; `confluent` stays beside it, commented out, out of trial credits. Done 2026-10-29. The purse's cicd plays
  `cli`.

### The console hears its story's region

| Task | Owner | Turns green |
|---|---|---|
| [ConsoleListener.java:258](../../../platform/src/main/java/dk/mathmagicians/playground/confluent/eventing/adapter/cli/console/ConsoleListener.java#L258) hands the story what `story.hears`, as [StoryListener.java:64](../../../platform/src/main/java/dk/mathmagicians/playground/confluent/eventing/adapter/kafka/consumer/StoryListener.java#L64) does | themathmagician | `ConsoleListenerTest`: an envelope of another region is not heard |
| [ConsoleListener.java:186](../../../platform/src/main/java/dk/mathmagicians/playground/confluent/eventing/adapter/cli/console/ConsoleListener.java#L186) keeps the region a `region:` line names as typed, without `toUpperCase` | themathmagician | `ConsoleListenerTest`: `region: Apac` stamps `Apac` |

Spec green 2026-10-29: `make bdd TRANSPORT=kafka` green on the platform's and the tea party's features, and run
[40851714017](https://github.com/Mathmagicians/wonderland-eventing-playground/actions/runs/40851714017), `bdd (cli)`
and `bdd (kafka)` green in `cli-test` and `kafka-test`, each with its summary and its `cucumber-report-<transport>`.

- Parked: the JDK's restricted-method warning on lz4-java's native load, once per JVM that publishes;
  `--enable-native-access=ALL-UNNAMED` on the `Test` tasks, `bootRun`, and the image's `JAVA_TOOL_OPTIONS`, in
  `platform.gradle`.

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

## Stories describe themselves

| Task | Owner | Turns green |
|---|---|---|
| `Story.publishes()` in each story | themathmagician | the unit tests of `publishes()` |
| `@PrimaryPort(name, description)` on each story | themathmagician | the architecture rule |
| The named key strategies | themathmagician | the map's key rule |

## Architecture discovery: one model, three levels

| Task | Owner | Turns green |
|---|---|---|
| The `model.dsl` writer, through `ModuleDocumentation` | themathmagician | the fixture test |
| `workspace.dsl` and `styles.dsl`: the forest, the trees, the hexagon | themathmagician | the three levels |
| The rendering in the build, `make arch-gen` | pair | `make arch-check`, exact |

## The skill generate-platform-client

| Task | Owner | Turns green |
|---|---|---|
| The skill, from the architecture spec | themathmagician | the client's acceptance |
| The rehearsal's generated code kept in a folder beside the live run's, `<story>-backup`, the fallback of the live demo | themathmagician | its `make check` green |
| The offline run from `make platform-install`, [Makefile:116](../../../Makefile#L116) | pair | the client builds without the network |

## Slides alongside

Owner: pair. The deck in [slides/](../../../slides/slides.md), with `make slides-present` and `make slides-pdf`. Each
feature fills its section of the deck as it lands. The talk plays from compose,
[Slides spike](../archived/release-0.0.6/03_automation.md#slides-spike-the-talk-plays-from-compose).

- The PDF in `docs/generated/slides` is the export of 2026-10-20.

## README

Owner: themathmagician, pair secretary.

- Configuration, Environments: `kafka` in the transports' table.
- Spring Boot: secrets fail fast at startup, named by `ConfluentKeys`.
- Play: `make run TRANSPORT=kafka`, `make kafka-universe-up`, and `make kafka-universe-down`.
- Tech Stack: Apache Kafka and the schema registry for the local and the CI runs.
- Git and CI: `bdd (kafka)` and `cicd-client-example` on the local broker.
- Dependencies and build: `./gradlew spotlessApply`, a task the build does not have.

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
- Open: a dispatch on `kafka`, its environments empty by design, the universe on the runner, see
  [Messaging capability](#messaging-capability-cli-kafka-confluent); the `cloudflare-*` environments filled when it
  plays, 0.0.8, Eventing over Cloudflare. Spec green on a dispatch per transport that plays.

## CI plays each transport

| Task | Owner | Turns green |
|---|---|---|
| `TRANSPORT` in `platform.mk`, default `cli`, passed to the `bdd` task with the profiles `<transport>,<env>`; the task leaves out what the transport does not carry | pair | done 2026-10-22: `make bdd-snippets` plays 3 + 7 scenarios, `TRANSPORT=confluent` 10 + 7 |
| `TRANSPORTS` and `CI_TRANSPORTS` in `platform.mk`, the reasons beside them, and `make ci-transports` | pair | done 2026-10-21: `make ci-transports` prints `["cli"]` |
| The drivers' port [Transport](../../../platform/src/testFixtures/java/dk/mathmagicians/playground/confluent/eventing/bdd/Transport.java), one bean per transport: `ClusterTransport` on `confluent`, the cluster and registry calls the drivers made before; `ConsoleTransport` on `cli`, the story's console typed into, the image's log read | pair | done 2026-10-22 on cli: `bdd (cli)` green on the pull request; open: `make bdd TRANSPORT=confluent` green once the cluster answers, [Messaging capability](#messaging-capability-cli-kafka-confluent) |
| `LoggingPublisher` logs the receipt, the key, the region, and the payload as the console reads it, `Converter.text` | pair | done 2026-10-22: `LoggingPublisherTest`, `ConverterTest`; `make run-tiny` logs `Published ENVL-… to log-0 at offset 0, key EMEA/P-DRIN, region EMEA: Offer offer_id: "OFF-…" product_id: "P-DRIN" …` |
| `cli` in `load-run.yaml`'s choice, and its environments `cli-test` and `cli-prod` | pair | done 2026-10-21: the deployments to `cli-test` and `cli-prod` of 2026-10-21; `make gh-main-protection` deploys `cli-prod` from `main` alone |
| The `bdd` job of `cicd.yaml`, a matrix over `make ci-transports`, before `cd`, which publishes the image; `cicd-client-example.yaml` plays the purse in `cli-test` | pair | done 2026-10-21: [39691461043](https://github.com/Mathmagicians/wonderland-eventing-playground/actions/runs/39691461043) with `bdd (cli)` and `cd`, and `cicd-client-example` [39691460970](https://github.com/Mathmagicians/wonderland-eventing-playground/actions/runs/39691460970), green on the pull request |

- The merge of 0.0.6, run
  [39692749878](https://github.com/Mathmagicians/wonderland-eventing-playground/actions/runs/39692749878) on `main`,
  2026-10-21: `ci`, `bdd (cli)` in `cli-test`, `cd`, and `tag` green, `make ci-transports` printing `["cli"]`.
- Spec green 2026-10-29: run
  [40851714017](https://github.com/Mathmagicians/wonderland-eventing-playground/actions/runs/40851714017), `bdd (cli)`
  and `bdd (kafka)` green, `make ci-transports` printing `["cli","kafka"]`; `confluent` joins `CI_TRANSPORTS` when
  the cluster answers.
