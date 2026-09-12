# Microservicio Carrito (MS3) - Guía de Docker y Despliegue

## Variables de Entorno Configuradas (.env)

El archivo `.env` ya cuenta con los valores exactos para la máquina virtual y MongoDB:

```env
MONGO_COLLECTION=carritos
MONGO_ROOT_USER=ms3_analytics_admin
MONGO_ROOT_PASSWORD=X7b@2nM$5kZ!9wRq
MONGO_DATABASE=ms3_analytics_prod_db
MONGO_AUTH_DB=admin

MONGO_HOST=172.31.37.154
MONGO_PORT=27017

HOST_PORT=8083
SERVER_PORT=8080

DOCKER_IMAGE=joselam/backend_ms3:latest
```

---

## 1. Ejecutar localmente o en el servidor con Docker Compose

Para levantar el servicio con la configuración lista:

```bash
cd backend
docker compose up -d
```

O si necesitas compilar la imagen localmente al iniciar:

```bash
docker compose up --build -d
```

El servicio estará disponible en el puerto **8083** del host:
- Endpoint API: `http://localhost:8083/api/carritos`
- Swagger UI: `http://localhost:8083/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8083/v3/api-docs`

---

## 2. Compilar y Subir a Docker Hub (`joselam/backend_ms3:latest`)

Para construir la imagen y subirla a tu cuenta de Docker Hub:

```bash
cd backend

# Paso A: Iniciar sesión en Docker Hub
docker login

# Paso B: Compilar la imagen con el tag solicitado
docker build -t joselam/backend_ms3:latest .

# Paso C: Pushear a Docker Hub
docker push joselam/backend_ms3:latest
```

*(O simplemente ejecuta `./build-and-push.sh`)*

---

## 3. Servicio en docker-compose.yml

```yaml
version: '3.8'

services:
  backend-integrante3:
    image: joselam/backend_ms3:latest
    container_name: backend_ms3_carrito
    restart: always
    ports:
      - "8083:8080"
    env_file:
      - .env
```

---

## Estructura del Proyecto

```
backend/
├── pom.xml                                   # Configuración de Maven y dependencias
├── Dockerfile                                # Multi-stage build (Maven 3.9 + JRE 17 Alpine)
├── docker-compose.yml                        # Definición del servicio backend-integrante3
├── .dockerignore                             # Exclusiones de build
├── .env                                      # Variables de entorno con credenciales de la MV
├── .env.example                              # Plantilla de referencia
├── build-and-push.sh                         # Script automatizado para Docker Hub
├── Carrito_Microservicio.postman_collection.json # Colección de pruebas para Postman v2.1
└── src/
    └── main/
        ├── java/com/example/backendcarrito/
        │   ├── BackendCarritoApplication.java
        │   ├── controller/CarritoController.java
        │   ├── dto/
        │   ├── exception/
        │   ├── model/
        │   ├── repository/
        │   └── service/CarritoService.java
        └── resources/
            └── application.properties
```
