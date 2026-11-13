# An example of a story built on the eventing platform, the White Rabbit: this folder is what a client of the
# platform has in a repository of its own. It names its image, includes the platform's targets, and that is the
# whole Makefile; `make help` lists what those targets are, the purse's README.md says how to make one like it.

IMAGE := white-rabbit
# the platform's targets: this checkout's, or a copy of the file next to a build outside it
PLATFORM_MK ?= ../../platform.mk
# where .env.<TRANSPORT>-<ENV>.private lives: the platform's root while the example sits inside it
ENV_DIR := ../..
include $(PLATFORM_MK)
