#!/bin/bash
docker image build -t demo-spring-observability:latest --build-arg CACHEBUST=$(date +%s) .
