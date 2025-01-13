#!/bin/bash

DOCKER_USER="beepboopvictor"

IMAGE_NAME="nginx_custom"
IMAGE_TAG="latest"

echo "Construyendo la imagen Docker..."
docker build -t "$DOCKER_USER/$IMAGE_NAME:$IMAGE_TAG" .

echo "Haciendo push de la imagen a DockerHub..."
docker push "$DOCKER_USER/$IMAGE_NAME:$IMAGE_TAG"

echo "Push completado. La imagen está disponible en DockerHub como $DOCKER_USER/$IMAGE_NAME:$IMAGE_TAG"
