#!/bin/bash

DOCKER_USER="beepboopvictor"

IMAGE_NAME="nginx_custom"
IMAGE_TAG="latest"

echo "Haciendo pull de la imagen desde DockerHub..."
docker pull "$DOCKER_USER/$IMAGE_NAME:$IMAGE_TAG"

echo "Ejecutando el contenedor..."
docker run -d --network hazelcast-network -p 80:80 "$DOCKER_USER/$IMAGE_NAME:$IMAGE_TAG"

echo "El contenedor está corriendo y NGINX está disponible en el puerto 80."
