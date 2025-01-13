#!/bin/bash

IMAGE_NAME="beepboopvictor/search-engine:latest"
LOCAL_IP=$(ipconfig | findstr "IPv4" | head -n 2 | tail -n 1 | awk -F: '{print $2}' | tr -d '[:space:]')
OTHER_NODES="10.26.14.237,10.26.14.238,10.26.14.234,10.26.14.235,10.26.14.231,10.26.14.232,10.26.14.233,10.26.14.228,10.26.14.229,10.26.14.230,10.26.14.225,10.26.14.226,10.26.14.227"

DOCKER_USER="beepboopvictor"
DOCKER_PASSWORD="BigData2025"

echo "Iniciando sesión en Docker..."
echo "$DOCKER_PASSWORD" | docker login -u "$DOCKER_USER" --password-stdin

docker system prune -f -a

docker network create hazelcast-network

docker pull $IMAGE_NAME

docker run -d --name search-engine-node \
    --network hazelcast-network \
    -e HZ_CLUSTERNAME=my-cluster \
    -e HZ_NETWORK_JOIN_TCPIP_ENABLED=true \
    -e HZ_NETWORK_JOIN_TCPIP_MEMBERS=$OTHER_NODES \
    -e HZ_NETWORK_PUBLICADDRESS=$LOCAL_IP:5701 \
    -p 4567:4567 \
    -p 5701:5701 \
    -m 8g \
    --memory-swap 8g \
    -e JAVA_TOOL_OPTIONS="-Xms2g -Xmx10g" \
    $IMAGE_NAME

echo "Contenedor ejecutándose en http://$LOCAL_IP:4567"