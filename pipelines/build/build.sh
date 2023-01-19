#!/bin/bash
# Build script
set -eo pipefail

build_tag=$1
name=$2
node=$3
org=$4

docker build -f Dockerfile --label commitHash=$(git rev-parse --short HEAD) -t ${org}/${name}:${build_tag} .
echo {\"image_name\" : \"${name}\", \"image_tag\" : \"${build_tag}\", \"node_name\" : \"$node\"} > metadata.json