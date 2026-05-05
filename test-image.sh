#!/bin/bash

# 1. Rebuild the image
docker build -t order-menu:testing . --no-cache

# 2. Run it locally
docker run -it --rm -p 8082:8082 --name order-menu order-menu:testing
