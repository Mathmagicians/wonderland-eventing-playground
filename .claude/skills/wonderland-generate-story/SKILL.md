---
name: wonderland-generate-story
description: Build a story of Wonderland from its specification, a client of the eventing platform as examples/purse is one. Use when a story's folder holds its specification, two features and a .textproto, and nothing else; a part of the specification that is missing is written first, with the human, as step 0, and the build comes from examples/purse. The run ends when make build, arch-gen, arch-check, arch-verify, bdd, and arch-bdd are green in that folder. The README holds the standards, this skill the order of work and the gates.
---

# Generate a platform client

A story is specified before its code: its behaviour feature in domain language, its architecture-discovery feature
in the code's own names, and its `.textproto`, the hexagon it must have. The build writes the same `.textproto`
from the code it finds, and the checks compare the two. This skill turns the specification into the code the
checks accept, nothing more.

## Input

- The story's folder, the current directory, holding the specification, the three files below, and nothing else;
  the build is written into it from `examples/purse`, see The build.
- `src/test/resources/features/<story>.feature`, the behaviour, on the platform's steps and the story's own.
- `src/test/resources/features/<story>_hexagon.feature`, tagged `@architecture-discovery`, the architecture in the
  code's names: `@Module`, `@PrimaryPort`, `subscribesTo`, `publishes … through PublishMessage`,
  `@AutoConfiguration`, `@EnableConfigurationProperties`, `@ConfigurationProperties`, `@Bean`.
- `src/test/resources/architecture/<story>.textproto`, the specification the build must write back: the module's
  name and package, its primary port and what it implements, `subscribes`, `publishes`, `depends_on`, `wiring`.
- The platform in the local Maven repository, `make platform-install` at the root of the checkout, so the run
  works offline.
- The README, Hexagon under Architecture and the Coding standards; `examples/purse` as the worked example, read in
  full before writing.

## Step 0, when a part of the specification is missing

Before any code: the three files above exist, or the run stops and writes them with the human, one at a time,
each after their yes. Ask what is not there, in one line each, and draft from the answers:

- No behaviour feature: ask what the story does, in the domain's words, what it reads from which stream and what
  it puts where; draft the feature on the platform's steps, `make bdd-snippets` shows which lines are the story's
  own.
- No architecture-discovery feature: draft it from the behaviour feature and the purse's, the same scenarios, the
  story's names in them: the module, the port, what it subscribes to and publishes, the wiring, the dependencies,
  every payload driven, nothing else annotated.
- No `.textproto`: draft it from the two features in the DSL's shape, the purse's as the example; the names are the
  features'.

A draft is shown in full, written after the yes, and the run goes on to the specification as written.

## Read the specification

From the `.textproto`: `base_package` is the story's package, `name` the `@Module` name, `primary_port` the class
that implements `Story`, `subscribes` and `publishes` the payload classes the port names, `wiring` the
auto-configuration, its settings record with its prefix, and its bean with what it builds and from what,
`depends_on` the platform's modules the story may reach, `Application` and `Domain` and nothing at the rim.

From the behaviour feature: what the story does with what it subscribes to, and the lines that are the story's
own, the ones no platform step defines, `make bdd-snippets` lists them.

## The build, from the purse

Before the code, the story's build: every file of `examples/purse` outside `src/main`, `src/test/java`,
`src/test/resources`, `docs`, `README.md`, `build`, and `.gradle`, copied into the folder, the purse's names
replaced by the story's and nothing else changed. The Gradle wrapper, `gradlew`, `gradlew.bat`, `gradle/wrapper`,
is copied as it is, `gradlew` executable. `<story>` below is the folder's name, `white-rabbit`.

| File | What changes |
|---|---|
| `settings.gradle` | `rootProject.name` is `<story>` |
| `build.gradle` | the `description`, one line in the story's words; the head comment names the story |
| `gradle.properties` | nothing; `platformGradle` and `platformVersion` stay |
| `Makefile` | `IMAGE` is `<story>` |
| `compose.yaml` | `name` and the service are `<story>`, the image `<story>:${VERSION…}`, the `command` lines the story's settings, `--<prefix>.<setting>` per setting of the record with the feature's values as defaults |
| `.gitignore` | nothing |
| `src/main/resources/application.properties` | `spring.application.name` and `story` are `<story>`, the comment's example settings the story's |
| `src/test/java/<root of base_package>/ArchUnitTest.java`, `ArchModulithTest.java` | the package is the parent of `base_package` without `.stories.<story>`, as `examples` is for the purse; the body stays |
| `src/test/java/…/<Story>Tests.java` | the wiring test, `PurseTests` renamed, the `properties` the story's ttl, `<prefix>.ttl=` |
| `src/test/java/<base_package>/bdd/ArchBddTest.java` | the package is `<base_package>.bdd`; the body stays |
| `src/test/java/<base_package>/bdd/RunCucumberTest.java` | the package is `<base_package>.bdd`, and the glue's second package too |

The purse's steps and drivers, `PurseSteps`, `PurseTable`, and its unit tests are not copied: the story's own
come in Write, step 6 and 7.

## Write, in this order

1. `src/main/java/<base_package>/package-info.java`: `@Module(name = "<name>")`, one line saying what the story is.
2. The port, `<primary_port>` implementing `Story`: `name()` as `--story` names it, `subscribesTo()` returning the
   payload classes literally, `Set.of(Offer.class)`, `publishes()` the same way, `on(Envelope)` the behaviour,
   `group()` the consumer group, `playsFor()` the ttl. `@PrimaryPort` on the class. The payloads it publishes go
   through `PublishMessage`, never through an adapter.
3. The settings record, `@ConfigurationProperties("<prefix>")`, validated in its compact constructor with the value
   in the message, every value with a default.
4. The auto-configuration, `@AutoConfiguration` with `@EnableConfigurationProperties(<settings>.class)` and the
   `@Bean` method named as the spec names it, building the port from the settings and the ports it needs.
5. `src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`, the
   auto-configuration's full name, one line.
6. Unit tests, one `<Class>Test` per class, AssertJ, data from `*Fixtures`.
7. The steps for the feature's own lines, `public` step classes under `<base_package>.bdd`, one line each
   delegating to a driver, the driver holding the assertions, as `PurseSteps` and `PurseTable` do.

Nothing outside the story's package. No new dependency. The README's Java 25 and Spring Boot standards. The names
are the specification's, never a synonym.

## Gates, in this order

| Gate | Red says |
|---|---|
| `make build` | a compile error, a failing unit test, a format violation, `spotlessApply` fixes the last |
| `make arch-gen`, then `make arch-check` | the lines specified and not built, or built and not specified |
| `make arch-verify` | a type reaching past its ring, a module boundary crossed |
| `make bdd` | a scenario red on the image, a step undefined |
| `make arch-bdd` | an architecture scenario red on what the build wrote, a payload no scenario drives |

A red gate names what to change; change the code, never the specification or the features, and run the gates
again from the first. Done when every gate is green. `make bdd` starts containers, which the human runs when the
run is unattended.

## What the run reports

Outcome first, then the gates' outputs, then the files written, one line each, then what was left out and why.
Nothing to stage: the human stages and commits.
