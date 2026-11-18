# PlaySwipe

**Descubre música deslizando.** Desliza a la derecha y la canción se guarda en tu playlist; desliza a la izquierda y pasa a otra.

App Android hecha en equipo como **proyecto final del ciclo de DAM** (Desarrollo de Aplicaciones Multiplataforma). El repositorio incluye la app y su API REST.

> 🇬🇧 *Swipe-based music discovery app for Android (Kotlin + Jetpack Compose) with its own Spring Boot REST API. Final project of a software development degree, built as a team.*

## Funcionalidades

- Reproductor con gestos: derecha guarda la canción, izquierda la salta.
- Reproducción aleatoria que no repite las canciones recientes.
- Subida de canciones con su MP3 y su portada.
- Playlist personal: cambiar nombre y portada, quitar canciones.
- Amigos: enviar y aceptar solicitudes, ver el perfil de cada amigo.
- Registro, inicio de sesión, perfil con foto, buscador y ajustes de cuenta.

## Tecnologías

| Parte | Stack |
|---|---|
| App Android (`PlaySwipe/`) | Kotlin, Jetpack Compose, Navigation, Retrofit + Gson, ExoPlayer / Media3, Coil, Firebase Storage |
| API (`PlaySwipeBackend/`) | Java 17, Spring Boot 3, Spring Data JPA, MySQL, Firebase Admin SDK |
| Infraestructura (durante el proyecto) | MySQL en Amazon RDS, backend en AWS, archivos en Firebase Storage |

## Estructura

```
PlaySwipe/            App Android (Android Studio)
  app/src/main/java/com/playswipe/Kotlin/
    ConexionesBack/   Cliente Retrofit y modelos de la API
    InicioSesion/ Registro/ PaginaPrincipal/ Playlist/ Perfil/ AnadirAmigos/ ...
PlaySwipeBackend/     API REST (Spring Boot)
  src/main/java/com/example/backend/
    Canciones/ Playlist/ AnadirAmigos/ Registro/ Iniciosesion/ Perfil/ Buscador/ ...
```

### Endpoints principales

| Método | Ruta | Uso |
|---|---|---|
| POST | `/api/registro` | Crear cuenta |
| POST | `/login` | Iniciar sesión |
| GET / POST | `/api/canciones`, `/api/canciones/nueva` | Listar y subir canciones |
| GET / POST / DELETE | `/api/users/{userId}/playlist/...` | Playlist del usuario |
| GET / POST / PUT | `/api/users/{userId}/friends/...` | Solicitudes de amistad y amigos |
| GET / PUT | `/perfil/...` | Datos e imagen de perfil |
| GET | `/api/buscador/canciones` | Buscar canciones |
| POST | `/api/cambiar-nombre`, `/api/cambiar-contrasena` | Ajustes de cuenta |

## Cómo ejecutarlo

Los servidores y las claves que usamos durante el proyecto **no están en el repositorio**. Para probarlo hace falta una base de datos MySQL propia y un proyecto de Firebase propio.

### 1. API (Spring Boot)

Requisitos: JDK 17 y MySQL 8.

1. Crea la base de datos `playswipe` en tu MySQL.
2. En Firebase: *Configuración del proyecto → Cuentas de servicio → Generar nueva clave privada*. Guarda el JSON **fuera del repositorio**.
3. Define las variables de entorno y arranca:

```bash
# Linux / macOS
export DB_URL="jdbc:mysql://localhost:3306/playswipe"
export DB_USER="playswipe"
export DB_PASSWORD="tu-contraseña"
export FIREBASE_CREDENTIALS="/ruta/a/tu-clave-firebase.json"
cd PlaySwipeBackend && ./gradlew bootRun
```

```powershell
# Windows (PowerShell)
$env:DB_URL="jdbc:mysql://localhost:3306/playswipe"
$env:DB_USER="playswipe"
$env:DB_PASSWORD="tu-contraseña"
$env:FIREBASE_CREDENTIALS="C:\ruta\a\tu-clave-firebase.json"
cd PlaySwipeBackend; .\gradlew.bat bootRun
```

La API queda en `http://localhost:8080`.

### 2. App Android

1. Descarga el `google-services.json` de tu proyecto de Firebase y ponlo en `PlaySwipe/app/` (está en `.gitignore`).
2. Abre la carpeta `PlaySwipe/` en Android Studio.
3. En el emulador, la app apunta a `http://10.0.2.2:8080/` (el PC visto desde el emulador). Para un móvil real o un servidor, cambia `BASE_URL` en `ConexionesBack/RetrofitClient.kt`.
4. Ejecuta la app.

## Mejoras pendientes

Es la versión académica del proyecto. Lo siguiente que cambiaría:

- Guardar las contraseñas con hash (BCrypt) y autenticar con tokens (JWT) en lugar de enviar el id de usuario.
- Usar HTTPS y quitar el tráfico sin cifrar de `network_security_config.xml`.
- Pasar la URL del backend a `BuildConfig` por variante (debug / release).
- Tests de la API y de los ViewModels.

## Autoría

Proyecto en equipo del ciclo de DAM. Repositorio publicado por **Pablo del Puerto Blanco** · [Portfolio](https://pablodelpuertoblanco.github.io) · [GitHub](https://github.com/pablodelpuertoblanco)
