# Discovery

The example map of each feature in the [catalog](release-spec-0.0.6.md): its story, its rules with examples, its
questions, and the decisions it raises for [docs/adr.md](../../adr.md).

## One name: wonderland-eventing-playground

**Story:** the project carries one name, without a vendor: Confluent is one of the transports it serves.

Examples agreed 2026-10-21.

### Rule: every spelling of the project's name is wonderland-eventing-playground
- the repository `Mathmagicians/wonderland-eventing-playground`, its badges, the GitHub Packages Maven URL, and
  `rootProject.name`.
- the image `ghcr.io/mathmagicians/wonderland-eventing-playground` and the compose project.
- `spring.application.name`, on the wire as `ce_source`.
- the Java package `dk.mathmagicians.playground.confluent` stays.

## Project sync: the board follows the catalog

**Story:** I see each feature of a release on the GitHub project board, in its phase, with what the phase files say
about it, and `make gh-project-sync` after a signed gate moves its card.

Examples agreed 2026-10-21.

- The project, [Wonderland-eventing-playground-spec-driven](https://github.com/orgs/Mathmagicians/projects/5),
  number 5, id `PVT_kwDOAd2-Xs4Bn-nR`, belongs to the Mathmagicians organization. The organization's issue field
  `phase` has the options `discovery`, `formulation`, `automation`, `verification`, `parking`, and `archive`. The
  board has a swimlane per milestone, one milestone per release.
- GitHub links commits to issues by `#12` and closes them by keyword, and has no keywords that set a field.
- `GITHUB_TOKEN` writes issues and contents and leaves an organization's project alone; a push made with it starts
  no run. The project's auto-add workflow, filter `is:issue`, puts each issue of the repository on the board.

### Rule: each feature in a release's catalog has an issue, linked in its row
- a row with an empty Issue cell, `make gh-project-sync` on its branch: the sync creates the issue, titled as the
  feature, and writes `[#N](url)` into the cell.
- a row with its issue: the sync creates none.
- `main` receives every row linked, through the pull request.

- every row of every release plan has a card; a release is a swimlane, its milestone, and has no card of its own.
  Decided 2026-10-21.

### Rule: an issue's body is the feature's sections of the phase files
- the card of Load on demand: its sections of `01_discovery.md`, `02_formulation.md`, and `03_automation.md`, under
  Discovery, Formulation, and Automation, with the links absolute into the synced branch.
- One name has no section in `02_formulation.md`: its card has Discovery and Automation.
- the body edited on GitHub: the next sync writes it from the files.

### Rule: an issue's Phase is its row's state, its milestone the version in its overview's file name
- Spec green signed, the row from Automation to Verification, synced: the card moves to Verification.
- every overview under `docs/releases/` but `archived/`: `this-pr/` and each planned `release-<version>/`; a
  feature of `release-0.0.7/` lies in the swimlane 0.0.7.
- a version without its milestone: the sync creates the milestone, without a due date.
- a state matches its Phase option in any case, `Automation` the option `automation`; a state without an option
  stops the sync with the state's name.

**Questions**
1. The release-wide parts, Context, README, and `04_verification.md`: they stay in the repository; a card is a row.
   Decided 2026-10-21.
