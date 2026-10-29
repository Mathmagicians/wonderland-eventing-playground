# The platform's targets, shared by every build that plays stories on it: this repository's, and a story's own.
# A story's build sets the variables, includes this file, and adds its targets below the include:
#
#   IMAGE := purse
#   PLATFORM_MK ?= ../../platform.mk
#   include $(PLATFORM_MK)
#
# Every target runs the Gradle wrapper of the directory it runs in, with platform.gradle applied, see there.

GRADLE := ./gradlew
# the image and container name
IMAGE ?= $(notdir $(CURDIR))
# the Boot project that builds the image: :app in this repository, the root in a story's build
BOOT ?=
# Gradle's version and the next one, from the root project, evaluated once, on first use
VERSION = $(eval VERSION := $(shell $(GRADLE) -q :version))$(VERSION)
RELEASE = $(eval RELEASE := $(shell $(GRADLE) -q :nextVersion))$(RELEASE)
# the GitHub repository, owner/name, from the origin remote; the registry, GHCR under the owner, lowercase
GITHUB_REPO = $(eval GITHUB_REPO := $(shell git remote -v 2>/dev/null | awk '/^origin.*\(fetch\)/ { print $$2 }' | sed -E 's|.*github.com[:/]||; s|\.git$$||'))$(GITHUB_REPO)
REGISTRY ?= ghcr.io/$(firstword $(subst /, ,$(GITHUB_REPO)))
REPO = $(shell echo $(REGISTRY)/$(IMAGE) | tr A-Z a-z)
# the tags docker-publish pushes; the registry tag docker-run, bdd-published and git-release take
TAGS ?= $(VERSION)
TAG ?= latest
# what a story reads, the names of .env.private.sample: a transport's keys, locally from .env.<TRANSPORT>-<ENV>.private,
# sourced into the command's shell only, in CI from the GitHub environment <transport>-<env>; a transport without
# keys, cli, kafka, has neither
CREDENTIALS := KAFKA_BOOTSTRAP_SERVERS KAFKA_API_KEY KAFKA_API_SECRET SCHEMA_REGISTRY_REST_ENDPOINT SCHEMA_REGISTRY_API_KEY SCHEMA_REGISTRY_API_SECRET
ENV ?= test
# where the file lives: here, or the repository root for a story's build inside it
ENV_DIR ?= .
ENV_FILE = $(ENV_DIR)/.env.$(TRANSPORT)-$(ENV).private
WITH_ENV = test ! -f $(ENV_FILE) || { set -a; . $(ENV_FILE); set +a; };
# a variable the environment exports empty, as a GitHub environment without it does, is dropped: to Spring an empty
# is a value, not the default
DROP_EMPTY := for v in $(CREDENTIALS); do eval "[ -n \"\$$$$v\" ] || unset $$v"; done;
# the transports, each with the GitHub environments <transport>-test and <transport>-prod: cli, the console and the
# log; kafka, the universe of compose.kafka.yaml; confluent, Confluent Cloud, the cluster the keys of
# .env.confluent-<ENV>.private open; cloudflare, plays nothing
TRANSPORTS := cli kafka confluent cloudflare
# the transports CI plays, the matrix of the bdd job: those whose <transport>-test environment holds what they need
CI_TRANSPORTS := cli kafka
# the transport a story plays on, one of TRANSPORTS
TRANSPORT ?= cli
# the keys of TRANSPORT and ENV into the command: the environment's, then the file's
WITH_KEYS = $(DROP_EMPTY) $(WITH_ENV)
# the profiles a process plays: its transport and its environment
PROFILES = $(TRANSPORT),$(ENV)
# the kafka universe of compose.kafka.yaml, beside this file, what iac/ is for Confluent Cloud: the broker, the
# registry, the viewer, the topics, and the schemas. The host reaches them on localhost, the flock on the compose
# network; a story on kafka brings the universe up first. The topic names and the partition count are
# iac/locals.tf's, read here and handed to compose with ENV
PLATFORM_DIR := $(dir $(lastword $(MAKEFILE_LIST)))
KAFKA_TOPICS := $(shell sed -nE 's/^ *names *= *\[(.*)\]/\1/p' $(PLATFORM_DIR)iac/locals.tf | tr -d '",')
KAFKA_PARTITIONS := $(shell sed -nE 's/^ *partitions *= *([0-9]+).*/\1/p' $(PLATFORM_DIR)iac/locals.tf)
KAFKA_COMPOSE := ENV=$(ENV) KAFKA_TOPICS="$(KAFKA_TOPICS)" KAFKA_PARTITIONS=$(KAFKA_PARTITIONS) docker compose -f $(PLATFORM_DIR)compose.kafka.yaml
KAFKA_NETWORK = $(eval KAFKA_NETWORK := $(shell $(KAFKA_COMPOSE) config --format json 2>/dev/null | jq -r .name)_default)$(KAFKA_NETWORK)
FLOCK_KAFKA := KAFKA_BOOTSTRAP_SERVERS=kafka:19092 SCHEMA_REGISTRY_REST_ENDPOINT=http://registry:8081
ON_KAFKA := $(filter kafka,$(TRANSPORT))
NEEDS := $(if $(ON_KAFKA),kafka-universe-up)
# the flock's environment: the profiles and the keys of TRANSPORT and ENV; on kafka the broker's addresses
COMPOSE := $(WITH_KEYS) $(if $(ON_KAFKA),$(FLOCK_KAFKA)) PROFILES=$(PROFILES) VERSION=$(VERSION) docker compose
# the scenarios bdd plays, a Cucumber tag expression; left empty, the bdd task's default
SCENARIOS ?=
SCENARIOS_ARG = $(if $(SCENARIOS),-Pscenarios='$(SCENARIOS)')
# what the bdd task plays on
ON_TRANSPORT = -Ptransport=$(TRANSPORT) -Pprofiles=$(PROFILES) $(if $(ON_KAFKA),-Pnetwork=$(KAFKA_NETWORK) -Pflock="$(FLOCK_KAFKA)")

.DEFAULT_GOAL := build
.PHONY: help check build test run clean version next-version docker-image docker-repo docker-publish docker-tag docker-image-exists docker-run bdd bdd-published bdd-matrix bdd-snippets ci-transports up down kafka-universe-up kafka-universe-down arch-verify arch-gen arch-check changed

# sections are the ##@ lines, targets the ## comments, this file's first
HELP_FILES := $(lastword $(MAKEFILE_LIST)) $(filter-out $(lastword $(MAKEFILE_LIST)),$(MAKEFILE_LIST))
help:      ## this list
	@printf '\n   (\\ /)\n   ( . .)   %s %s\n  c(")(")\n' "$(IMAGE)" "$(VERSION)"
	@sed -nE 's/^##@ (.*)/\n\1/p; s/^([a-z-]+):.*## *(.*)/  \1\t\2/p' $(HELP_FILES) | expand -t 24
	@echo

##@ Build, the CI gate
check: arch-verify build docker-image bdd   ## architecture, build, image, features, in this order

build:     ## compile, unit tests, jars
	@$(GRADLE) build

test:      ## unit tests
	@$(GRADLE) test

run: $(NEEDS)   ## from source on TRANSPORT and ENV; ARGS pick the story and its settings, e.g. ARGS="--story=tea-party --tea-party.region=APAC"
	@$(WITH_KEYS) SPRING_PROFILES_ACTIVE=$(PROFILES) $(GRADLE) $(BOOT):bootRun $(if $(ARGS),--args="$(ARGS)")

clean:     ## remove build output
	@$(GRADLE) clean

version:   ## from git tags: 1.2.3 at tag v1.2.3, 1.2.4-SNAPSHOT after it
	@echo $(VERSION)

next-version:   ## the SNAPSHOT without its suffix
	@echo $(RELEASE)

##@ Image, built by buildpacks, pushed to the registry, run from it
docker-image:   ## build IMAGE:VERSION; skipped while the jars and the version are unchanged
	@$(GRADLE) $(BOOT):bootBuildImage -Pimage=$(IMAGE):$(VERSION)

docker-repo:   ## the registry path, REGISTRY/IMAGE
	@echo $(REPO)

docker-publish: docker-image   ## push to the registry under each tag in TAGS
	@for tag in $(TAGS); do docker tag $(IMAGE):$(VERSION) $(REPO):$$tag && docker push $(REPO):$$tag; done

docker-tag:   ## put each tag in TAGS on the registry image TAG, the same bytes
	@for tag in $(TAGS); do docker buildx imagetools create -t $(REPO):$$tag $(REPO):$(TAG); done

docker-image-exists:   ## exit 0 when the registry has a latest image
	@docker manifest inspect $(REPO):latest > /dev/null

docker-run: $(NEEDS)   ## the registry image TAG on TRANSPORT and ENV, its console on stdin; ARGS pick the story and its settings, e.g. ARGS="--story=tea-party --tea-party.region=APAC"
	@$(WITH_KEYS) docker run --rm -i --pull always -e SPRING_PROFILES_ACTIVE=$(PROFILES) $(addprefix -e ,$(CREDENTIALS)) $(if $(ON_KAFKA),--network $(KAFKA_NETWORK) $(addprefix -e ,$(FLOCK_KAFKA))) $(REPO):$(TAG) $(ARGS)

##@ Flock, docker compose plays the image from compose.yaml on TRANSPORT and ENV; settings are environment variables, e.g. TTL=300 make up
up: docker-image $(NEEDS)   ## every player in compose.yaml, for TTL seconds, default 60, up to 300
	@$(COMPOSE) up

down:      ## stop the flock
	@$(COMPOSE) down

##@ Kafka universe, compose.kafka.yaml, what iac/ is for Confluent Cloud: the broker, the registry, the viewer, the topics, and the schemas TRANSPORT=kafka plays on
kafka-universe-up:   ## start the broker, the registry, and the viewer on localhost:8082, then create the topics and register the schemas of ENV
	@$(KAFKA_COMPOSE) up -d --wait kafka registry viewer
	@$(KAFKA_COMPOSE) run --rm topics
	@$(KAFKA_COMPOSE) run --rm schemas
	@$(KAFKA_COMPOSE) ps --format "table {{.Service}}\t{{.Status}}\t{{.Ports}}"

kafka-universe-down:   ## stop the universe; what it held is gone
	@$(KAFKA_COMPOSE) down

##@ Features, Cucumber runs the image on TRANSPORT: the platform's features and every story's
bdd: docker-image $(NEEDS)   ## play the features against the local image, built first when stale; SCENARIOS=<tag expression> and TRANSPORT pick the scenarios
	@$(WITH_KEYS) $(GRADLE) bdd -Pimage=$(IMAGE):$(VERSION) $(SCENARIOS_ARG) $(ON_TRANSPORT)

bdd-published: $(NEEDS)   ## play the features against the registry image TAG, as bdd does
	@$(WITH_KEYS) $(GRADLE) bdd -Pimage=$(REPO):$(TAG) $(SCENARIOS_ARG) $(ON_TRANSPORT)

bdd-matrix:   ## play the features once per transport of CI_TRANSPORTS, locally what the bdd job plays
	@for transport in $(CI_TRANSPORTS); do $(MAKE) --no-print-directory bdd TRANSPORT=$$transport; done

bdd-snippets:  ## print a snippet for each undefined step, playing none
	@$(GRADLE) bdd -PdryRun $(SCENARIOS_ARG) $(ON_TRANSPORT) || true

ci-transports:   ## print CI_TRANSPORTS as a JSON list, the matrix of the bdd job
	@printf '%s\n' $(CI_TRANSPORTS) | jq -Rnc '[inputs]'

##@ Architecture, the hexagon from the annotations in the code, verified by tests and drawn under docs/generated/architecture
arch-verify:   ## the hexagonal rule and the module boundaries
	@$(GRADLE) $(BOOT):test --tests '*ArchitectureTest' --tests '*ArchitectureDocumentationTest'

arch-gen:      ## write docs/generated/architecture from the code
	@$(GRADLE) $(BOOT):renderDiagrams

# the plantuml sources are compared line-sorted, the Documenter writes dependency lines in varying order; the svg
# files follow, so they must be in git but are not compared
arch-check: arch-gen   ## fail when docs/generated/architecture is not regenerated and staged
	@git status --porcelain -- docs/generated/architecture | grep '^??' && { echo "docs/generated/architecture has files not in git: git add docs/generated/architecture"; exit 1; } || true
	@for f in $$(git status --porcelain -- docs/generated/architecture/plantuml | awk '/^.[^ ]/ { print $$2 }'); do \
	  [ "$$(git show ":$$f" | sort | cksum)" = "$$(sort "$$f" | cksum)" ] || { echo "$$f is out of date: make arch-gen, then git add docs/generated/architecture"; exit 1; }; \
	done

##@ Repository
changed:   ## true when a file under PATHS changed between commit BASE and HEAD, false when not; no or unknown BASE is true
	@if [ -z "$(BASE)" ] || ! git cat-file -e "$(BASE)" 2>/dev/null; then echo true; \
	elif git diff --quiet "$(BASE)" HEAD -- $(PATHS); then echo false; \
	else echo true; fi
