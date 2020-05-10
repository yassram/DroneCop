#!/bin/bash
docker pull probablyfine/flume


docker build -t flume-image .
docker run -detach flume-image