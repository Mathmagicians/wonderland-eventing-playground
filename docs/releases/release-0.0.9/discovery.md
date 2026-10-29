# Discovery

The example map of each feature in the [catalog](release-0.0.8.md): its story, its rules with examples, its questions,
and the decisions it raises for [docs/adr.md](../../adr.md).

## Eventing over Cloudflare

**Goal:** a story plays on Cloudflare as a fourth transport, `cloudflare`, beside `cli`, `kafka`, and `confluent`,
through adapters of its own at the rim of the hexagon.

- 0.0.7 makes the transport a profile beside the environment.
- Queues takes a message over HTTP, `POST /accounts/{account}/queues/{queue}/messages`, a body with the content type
  `json` or `text`, under a token with `Queues Edit`
  ([publish via HTTP](https://developers.cloudflare.com/queues/examples/publish-to-a-queue-via-http/),
  [push API](https://developers.cloudflare.com/api/resources/queues/subresources/messages/methods/push/)).
- A pull consumer reads over HTTP, `messages/pull` and `messages/ack`, up to 100 messages a pull, each with `body`,
  `id`, `timestamp_ms`, `attempts`, and `lease_id`, and a visibility timeout up to 12 hours
  ([pull consumers](https://developers.cloudflare.com/queues/configuration/pull-consumers/)).
- A message is its body; the attributes a Kafka record carries as headers ride inside it.
- Queues delivers at least once, and the consumer recognises a repeat by an id the producer writes
  ([delivery guarantees](https://developers.cloudflare.com/queues/reference/delivery-guarantees/)); the order of
  delivery is best effort ([JavaScript APIs](https://developers.cloudflare.com/queues/configuration/javascript-apis/)).
- A message the consumer keeps failing goes to the queue's dead-letter queue
  ([dead-letter queues](https://developers.cloudflare.com/queues/configuration/dead-letter-queues/)).
- A Durable Object is one single-threaded instance per name, reached from a Worker
  ([Durable Objects](https://developers.cloudflare.com/durable-objects/)).
- Pipelines takes JSON over HTTP or a Worker binding, transforms it row by row in SQL, and writes it to R2 as Iceberg
  tables or Parquet and JSON files; it is in open beta, and its streams, SQL, and sinks are fixed at creation
  ([Pipelines](https://developers.cloudflare.com/pipelines/)).
- `wrangler dev` simulates Queues for Workers through Miniflare
  ([local development](https://developers.cloudflare.com/queues/configuration/local-development/)).
- On Kafka the key picks the partition, so the records of one key stay in order (README, Purpose), and the envelope
  rides as CloudEvents headers (README, Kafka and Confluent Cloud).

**Story:** I play a story on Cloudflare with `TRANSPORT=cloudflare`, and the stories stay as they are.

### Rule: a generator publishes each payload type to a queue of its own, the counterpart of its topic, from the platform over HTTP
### Rule: a listening story pulls its queues and acknowledges what it handled; a message it keeps failing goes to the dead-letter queue
### Rule: a story handles a repeated message as it handled the first, by the envelope id

**Questions**
1. Order per key: the README's rule holds through a Durable Object per key, with a Worker in front of the queues;
   or the transport delivers in best-effort order, and the features about order run on `kafka` and `confluent`?
2. The envelope in the body: CloudEvents structured mode in JSON with the payload as `data_base64`, or the
   `Envelope` message of `envelope.proto` in its JSON form?
3. The schema: the Confluent framing with its schema id, base64 in the body, against a registry the platform
   reaches; or the Protobuf JSON form, checked against the platform's own descriptors?
4. The runs: a Cloudflare account for the developer machine and for CI, its token in `.env.cloudflare-<ENV>.private` and a
   GitHub environment `cloudflare-<env>`?
5. Pipelines: the counterpart of the `@confluent` Flink feature, the generator's JSON in an R2 table, with the
   latest value snapshot as a query over it?

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
