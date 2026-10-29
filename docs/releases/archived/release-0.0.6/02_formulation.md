# Formulation

Each feature's scenarios and architecture rules, by link, and the step skeletons, for the features of the
[catalog](release-spec-0.0.6.md) that passed Examples agreed. The evidence of Spec signed, the red run, is linked from
the feature's row in the catalog.

## Project sync: the board follows the catalog

Spec signed 2026-10-21. The spec is
[the_board_follows_the_catalog.feature](../../../src/test/resources/features/the_board_follows_the_catalog.feature),
`@manual`, checked on the runs of `make gh-project-sync`.

| Task | Owner | Done when |
|---|---|---|
| The map's scenarios in `the_board_follows_the_catalog.feature`, `@manual` | pair | each example of the map is a scenario |
| The first sync with the Issue cells empty | pair | the run shows the issues created and the links written into the rows |
| A gate signed and synced | pair | the card is in the row's new state |
