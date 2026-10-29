# Verification

The release gate, **In production**: every feature the [catalog](release-0.0.9.md) ships Spec green, the release
checks passed, and Wonderland playing in each of its environments.

## Release checks

| Check | Evidence | Signed |
|---|---|---|
| `make check` green | | |
| the PR's checks green | | |
| Review findings above Nit resolved, or deferred with a reason | | |
| README, `make help`, and `docs/adr.md` say what the code does | | |
| The `bdd` job's summary: the reuse table of `make bdd-reuse` under the scenarios | | |

## ADR compliance

| ADR | Checked by | Evidence |
|---|---|---|
| The board writes a feature's phase to the catalog through the issue field Phase | the `@manual` scenarios of the_catalog_follows_the_board.feature | |
| A release ships the features that are Spec green | the `@manual` scenarios of a_release_ships_what_is_ready.feature | |
| [ADR-002 A platform step opens with Wonderland and names its hexagon element](../../adr.md#adr-002-a-platform-step-opens-with-wonderland-and-names-its-hexagon-element) | `make bdd-reuse` and review | |

## In production

| Environment | Tag | Image | Wonderland plays | Signed |
|---|---|---|---|---|
| `test` | | | | |
| `prod` | | | | |
