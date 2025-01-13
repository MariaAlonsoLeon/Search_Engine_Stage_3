#!/bin/bash

IMAGE_NAME="beepboopvictor/search-engine:latest"

docker build -t $IMAGE_NAME .
docker login
docker push $IMAGE_NAME

echo "Imagen $IMAGE_NAME subida a Docker Hub."
