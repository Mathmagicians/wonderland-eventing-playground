---
name: code-comments
description: What a comment in the code says and what it never carries. Use when writing or reviewing a comment in Java, Gradle, make, compose, a workflow, a properties file, a proto, Terraform, SQL, or a feature file's description, and when a review finds process, people, or fluff in one.
---

# Code comments

A comment states what the code does, or the fact the code cannot say: the reason for a value, the contract a
caller relies on, the behaviour of the thing on the other side of a call. It reads as the team's, once, in the
present tense, see `/voice`.

## What a comment never carries

- **Process.** No release, phase, gate, issue number, date, sign-off, decision, or plan. No "for now", "until",
  "yet", "later", "currently", "temporary", "workaround", no `TODO`. The plan in `docs/releases/` and the ADRs in
  `docs/adr.md` hold the process; a comment names what is, not how it got there or what comes next.
- **People.** No me, you, we, the human, Claude, an agent, the pair, a name. Who wrote or owns a line lives in
  `.claude/` and `CLAUDE.md`, nowhere else.
- **Fluff.** No "simple", "just", "easy", "nice", "clean", "robust", "proper", "obvious", "note that",
  "of course", "important", no exclamation marks, no smileys. An adjective stays when it carries information:
  `idempotent`, `unchecked`, `bounded`, `package-private`.
- **A restated rule.** The README holds the rules and `kafka.properties` holds the values; a comment names the
  rule or the value it applies, it does not repeat it.

## What a comment carries, per file

- Java: a `///` doc comment on a type says what it is and what it owns; on a member, what it does and answers,
  the exception it throws. A `//` inside a method says why a line is the way it is, where the code cannot.
- Gradle: a `//` above a task or a dependency says what it is for; a hard-coded version carries the reason and
  the date, the one dated comment, the README's rule.
- make: a `##` comment on every target, the text of `make help`; a `##@` line per section; a `#` above a
  variable says what it holds and where its value comes from.
- compose, workflows: a `#` above a service, a job, or a step says what it does and what it needs.
- properties: a `#` per tuning value says why that value, the README's rule for `kafka.properties`.
- proto, Terraform, SQL: a comment says what the message, resource, or table is and what reads or writes it.
- A feature file's description says what the capability is, in domain language.

## The check

A review of a comment greps for the words above. A comment that explains what the code plainly does is removed.
A comment that carries process moves to the plan; one that carries a person is removed.
