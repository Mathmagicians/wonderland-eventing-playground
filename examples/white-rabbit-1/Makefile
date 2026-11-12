# The White Rabbit, a story built on the eventing platform the way a client builds one, the demo's client: its
# image's name, the platform's targets, and that is the whole Makefile; `make help` lists the targets.

IMAGE := white-rabbit
# the platform's targets: this checkout's, or a copy of the file next to a build outside it
PLATFORM_MK ?= ../../platform.mk
# where .env.<TRANSPORT>-<ENV>.private lives: the platform's root while the example sits inside it
ENV_DIR := ../..
include $(PLATFORM_MK)
