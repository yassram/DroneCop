#!/bin/bash
docker pull probablyfine/flume
docker run \
  --env FLUME_AGENT_NAME=docker \
  --volume /tmp/config.conf:/opt/flume-config/flume.conf \
  --detach \
  probablyfine/flume:latest

docker build -t flume-image .
docker run -detach flume-image