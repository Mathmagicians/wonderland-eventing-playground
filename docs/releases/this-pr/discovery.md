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

## The catalog follows the board

**Goal:** a project update is a release doc update; a change on the board is for members only.

- Actions has no trigger for a project's fields. The `projects_v2_item` webhook is an organization's, in public
  preview, [webhook events](https://docs.github.com/en/webhooks/webhook-events-and-payloads#projects_v2_item).
- An issue field is an organization's single select on the issue. Setting it fires the `issues` event, `field_added`,
  with the field's name, its value, and its previous value, and a board groups its columns by it,
  [about issue fields](https://docs.github.com/en/issues/planning-and-tracking-with-projects/understanding-fields/about-issue-fields).
  Mathmagicians has issue fields: Effort, Priority, Start date, and Target date, each Organization only.
- An issue field is Organization only by default, visible to the organization's members and the repository's
  collaborators,
  [managing issue fields](https://docs.github.com/en/issues/tracking-your-work-with-issues/using-issues/managing-issue-fields-in-your-organization).
  Setting a value takes push access to the repository,
  [issue field values](https://docs.github.com/en/rest/issues/issue-field-values).
- Project 5 is private: those granted read access see it, and the base role Write lets the organization's members
  edit it,
  [managing access](https://docs.github.com/en/issues/planning-and-tracking-with-projects/managing-your-project/managing-access-to-your-projects).
- The `issues` event runs the workflow on `main`'s last commit,
  [events that trigger workflows](https://docs.github.com/en/actions/reference/workflows-and-actions/events-that-trigger-workflows#issues),
  and `main` holds no `docs/releases`.

**Story:** as a member of Mathmagicians, I move a card on the board, and the release plan on the release's branch
says so.

### Rule: a feature's phase is the issue field Phase; the board's columns are its options
- the options: `discovery`, `formulation`, `automation`, `verification`, `parking`, `archive`.

### Rule: a member of Mathmagicians moves the board's cards
- themathmagician sets a card's Phase: the release plan follows, see the next rule.
- the board opened signed out: it is not shown.
- the project is private, its base role Write, and Phase is Organization only.

### Rule: a Phase set on the board is its row's state and its gate's date, on the release's branch
- the card of Project sync set to `verification`: `project.yaml` runs on `field_added`, and
  `eventing-without-confluent` receives the commit `docs: Project sync: the board follows the catalog to
  verification`, the row reading Verification with 2026-10-21 in its Spec green cell, linked to the feature's
  section of `03_automation.md`, where our loop patches the evidence in.
- the card of One name set to `archive`: the row reads Archive; In production is the release's gate, signed in
  `04_verification.md`.
- that commit's push starts no run, and the issue's body says the new state.
- a Phase written by `make gh-project-sync`: the run finds the row in that state and commits nothing.

### Rule: a release's milestone names its branch
- `make gh-project-sync` on `eventing-without-confluent`: the milestone 0.0.6 has the description
  `eventing-without-confluent`, the overview's Branch.
- a card of 0.0.7 moved, its overview naming no branch: its Phase goes back to the row's state, and the job summary
  names the card.

**Questions**
1. The gate a move signs, Examples agreed on the move to `formulation`, Spec signed to `automation`, Spec green to
   `verification`: the pull writes its date into the feature's row, and our loop patches the evidence into the
   feature's section of the phase file the card left. In production is the release's gate. A workflow that verifies
   a gate, as `cicd.yaml` verifies a release, is a feature of its own. Decided 2026-10-21.
2. A row is a feature, and a move changes its row; a release groups the features it releases. A feature is Spec green
   on its passing BDD tests, or on a manual verification where BDD tests are not feasible. Decided 2026-10-21.
3. Phase in place of Status, one field on the board: Project sync writes Phase, and the project's Status stays
   unused. Decided 2026-10-21.
4. A shipped feature's issue is closed by `closes #N` in a commit, when the commit reaches `main`; the pull moves its
   card to `archive`. Decided 2026-10-22.

**Decisions for docs/adr.md, by title**
- The board writes a feature's phase to the catalog through the issue field Phase

## A release ships what is ready

**Goal:** a release ships the subset that is ready.

- `make bdd` plays every feature file: the `bdd` task at [platform.gradle:103](../../../platform.gradle#L103) and
  [junit-platform.properties](../../../platform/src/testFixtures/resources/junit-platform.properties) set no tag
  filter. A pending step is a red run, the evidence of Spec signed, and a red feature blocks the build.
- A card's milestone is its overview's version, see
  [Project sync](../archived/release-0.0.6/01_discovery.md#project-sync-the-board-follows-the-catalog).
- A feature we decide is not ready carries `@wip`, and a check played by hand, written as a scenario, carries
  `@manual`: `make bdd` leaves both out, and `make bdd-snippets` leaves out `@manual` alone. Decided 2026-10-21.
- The repository's own features, the tooling's, live in `src/test/resources/features/` at the root, played by a
  `bdd` of the root project. Decided 2026-10-21.

**Story:** I release the features that are ready; the others move on to the next release.

### Rule: a release ships its rows in Verification; the others move to the next release's catalog
- the merge of 0.0.6: its rows in Verification read Archive, signed In production in `04_verification.md`.
- a feature in Discovery at the merge: its row moves to the catalog of 0.0.7, and its card to the swimlane 0.0.7.

### Rule: a feature we decide is not ready carries @wip, and make bdd leaves it out
- `make bdd`: every scenario but those tagged `@wip` or `@manual`.
- a feature decided not ready for 0.0.6: its feature file carries `@wip`, and the release ships green without it.
- `make bdd-snippets`: the snippets for the undefined steps of every scenario, `@wip` included.

### Rule: a check played by hand carries @manual, and its evidence is in the phase file
- the talk of the Slides spike as a scenario tagged `@manual`: `make bdd` and `make bdd-snippets` leave it out.
- its Spec green in `03_automation.md`: what themathmagician saw, with the date.

**Decisions for docs/adr.md, by title**
- A release ships the features that are Spec green

## Stories describe themselves

**Story:** as the platform, I read from each story what it hears, what it publishes, and on which key.

- [Story.java](../../../platform/src/main/java/dk/mathmagicians/playground/confluent/eventing/application/Story.java)
  names what a story reads, `listensTo()` and `hears()`; what it publishes lives in its body.
- jMolecules 2.0.1's `@PrimaryPort` carries `name` and `description`.
- The key per topic is the README's rule under Purpose, and `Envelope.key()` in the code.

### Rule: a story names the payload types it publishes, Story.publishes(), beside listensTo()
### Rule: a story's port carries its name and its description, @PrimaryPort(name, description)
### Rule: each topic's key strategy carries a name: by product, by region and product, by region, by customer

**Questions**
1. The home of a key strategy's name: the payload record, or `Topics`?

## Architecture discovery: one model, three levels

**Story:** as a reader of the architecture, I see the platform at three levels, drawn from one model the code
writes.

- `HexagonDocumenter` in [architecture-discovery/](../../../architecture-discovery/architecture-discoverability.md)
  writes PlantUML under `docs/generated/architecture` from the annotations; `make arch-check` compares its sources
  line-sorted, [platform.mk:107](../../../platform.mk#L107).
- structurizr-core and structurizr-export 5.0.3 come on the classpath with Spring Modulith.

### Rule: model.dsl is generated from the code through ModuleDocumentation; workspace.dsl and styles.dsl are written by hand
### Rule: level 1, the forest: the landscape, the platform with its stories and the purse
### Rule: level 2, the trees: the containers, with their connections
### Rule: level 3, the hexagon of each container: the rings and the adapters
### Rule: make arch-check compares the generated files exactly

**Questions**
1. The rendering: structurizr-dsl 6.2.3 in the build, or the Structurizr CLI image? Both external, a go on the name.

## The skill generate-platform-client

**Story:** from the architecture spec, the skill builds a client of the platform that passes it.

- The purse extends `HexagonalArchitecture` and `ModuleDocumentation` with one-line classes, and 7 of the 13 step
  lines of its feature are platform steps.
- `make platform-install` puts the platform, its test fixtures, and the stories in the local Maven repository,
  [Makefile:116](../../../Makefile#L116).

### Rule: the skill's input is the architecture rules, the shared steps, model.dsl, and the published platform
### Rule: the client is done when its make check is green: the rules, its features, the platform's steps
### Rule: the run works offline, from make platform-install

**Questions**
1. The client for the demo: a new story, generated from its feature and `model.dsl`; the rehearsal's code kept in
   `<story>-backup` as the fallback. Decided 2026-10-29.

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

## Wonderland's lines: what a feature reuses from the platform

**Goal:** in a client's feature I see which lines are Wonderland's and which the client defines, and the summary
prints the degree of reuse.

- The platform ships 14 step patterns in 7 classes under `platform/src/testFixtures/.../bdd`, each class named by
  the element it drives: `TransportSteps`, `StreamSteps`, `StorySteps`, `GeneratorSteps`, `ClusterSteps`,
  `SchemaSteps`, `FlinkSteps`.
- The purse's feature has 13 step lines, 7 of them the platform's.
- `cucumber.json` records each step's glue method, `cucumber.plugin` in `junit-platform.properties`; the `bdd` job
  appends `pretty.txt` to its summary, [cicd.yaml:111](../../../.github/workflows/cicd.yaml#L111).

**Story:** I read a client's feature and see, line by line, what Wonderland does and what the client defines, and
the summary tells me how much of the feature is Wonderland's.

Examples agreed 2026-10-24.

### Rule: a platform step opens with Wonderland and names the element of the hexagon it drives, in the present tense
- the rim, the transport played: `Given Wonderland is reachable`.
- what the story hears, `StoryListener` and `ConsoleListener`: `And Wonderland hears on the stream of transactions:
  Mad Hatter paid Alice 20.25 coins for a thing`.
- the driving port `Story`, the story by its `--story` name: `When Wonderland's story purse reads the stream of
  transactions`.
- the driven port `Publisher`, `KafkaPublisher` and `LoggingPublisher`: `Then Wonderland publishes 3 offers to the
  topic offers`.
- Confluent Cloud, from `iac/`: `Then Wonderland has the topic offers`, `Wonderland has the schema offer.proto for
  the topic offers`, the table the same way.

### Rule: a client's step never opens with Wonderland
- `Given Alice's purse holds 1000 coins`, `Then Alice has 1009.75 coins left`, `And the purse makes a warning`.

### Rule: the opening and the glue agree
- a line opening with Wonderland whose glue lies outside the platform's `bdd` package: `make bdd-reuse` red, naming
  the line; the reverse the same way.

### Rule: the summary prints the reuse per feature, by element
- `make bdd-reuse` after `make bdd`: Feature | Lines | Rim | Heard | Story | Published | Cloud | Own | Share, from
  `cucumber.json`, each line of the feature once; the tea party's row `24 | 1 | 9 | 4 | 0 | 0 | 10 | 58%`, played by
  the root's `make bdd`; the purse's, `13 | 1 | 4 | 2 | 0 | 0 | 6 | 54%`, prints in its own cicd, it is its own
  build under `examples/`.
- the `bdd` job: the table under the scenarios in the job summary, a release check in `verification.md`.

**Questions**
1. Columns per element beside the one count: now, the rule above. Decided 2026-10-24.
2. The order of the rewrite: the feature files first, one at a time, each red on its undefined steps, then the 14
   step patterns in the fixtures turn them green. Decided 2026-10-24.

**Decisions for docs/adr.md, by title**
- [ADR-002 A platform step opens with Wonderland and names its hexagon element](../../adr.md#adr-002-a-platform-step-opens-with-wonderland-and-names-its-hexagon-element)
