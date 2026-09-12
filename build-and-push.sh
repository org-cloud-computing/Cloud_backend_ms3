#!/bin/bash
# Script para compilar la imagen Docker y subirla a Docker Hub
set -e

IMAGE_NAME="joselam/backend_ms3:latest"

echo "=========================================================="
echo "Compilando imagen Docker: $IMAGE_NAME"
echo "=========================================================="
docker build -t "$IMAGE_NAME" .

echo "=========================================================="
echo "Iniciando sesión en Docker Hub..."
echo "=========================================================="
docker login

echo "=========================================================="
echo "Subiendo imagen a Docker Hub: $IMAGE_NAME"
echo "=========================================================="
docker push "$IMAGE_NAME"

echo "=========================================================="
echo "¡Imagen subida exitosamente!"
echo "Ahora puedes ejecutar en tu servidor:"
echo "docker compose up -d"
echo "=========================================================="
