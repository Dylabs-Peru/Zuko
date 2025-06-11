# Zuko Backend - Deploy con Docker Compose

Este proyecto contiene el backend de la aplicación Zuko, construido en Spring Boot y preparado para correr en cualquier entorno con Docker y Docker Compose.

## 🚀 Requisitos

- Docker (https://docs.docker.com/get-docker/)
- Docker Compose (https://docs.docker.com/compose/)
- Docker Desktop en Windows/Mac ya incluye Docker Compose

## ⚡️ Levantar el stack completo (backend + base de datos)

1. Clona este repositorio.
2. Ve a la carpeta del proyecto.
3. Ejecuta:
    ```bash
    docker-compose up
    ```
   > Esto descarga (si no la tienes) la imagen del backend `turppie/zuko-backend:latest` y la imagen oficial de Postgres, y levanta todo el entorno automáticamente.

## 🛠 Variables de entorno usadas

- `SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/zuko_db`
- `SPRING_DATASOURCE_USERNAME=postgres`
- `SPRING_DATASOURCE_PASSWORD=adminadmin`
- `SPRING_JPA_HIBERNATE_DDL_AUTO=update`

## 🌐 Endpoints

El backend estará disponible en:

http://localhost:8080/api/v1/

Consulta la documentación de la API o Swagger para rutas y uso.

(USA POSTMAN NOMAS FRACAZOE)
## 🧑‍💻 Uso en desarrollo

Para modificar el código y probar en caliente:
```bash
  docker-compose -f docker-compose.dev.yml up --build
```
Esto construye la imagen desde el Dockerfile local.

## 🐳 Imagen Docker

Puedes bajar la imagen directamente con:
```bash
  docker pull turppie/zuko-backend:latest
```
## 📝 Notas

El volumen postgres-data garantiza que los datos de la base no se pierdan si detienes o reinicias los contenedores.

Si cambias credenciales o nombres de la base, edita el docker-compose.yml.

Recuerda cerrar servicios previos en el puerto 5432 si tienes un Postgres local.

Cualquier duda, sugerencia o mejora, ¡abre un issue o contáctame!

---
