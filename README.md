# Cocinética 🍳📱

**Cocinética** es una aplicación móvil desarrollada en Android Studio
con Kotlin, cuyo objetivo es facilitar la creación, exploración y
valoración de recetas de cocina. Los usuarios pueden registrarse,
iniciar sesión, crear recetas, comentar, guardar favoritas y mucho más.

------------------------------------------------------------------------

## 🚀 Funcionalidades principales

-   Registro e inicio de sesión con Firebase Authentication
-   CRUD de recetas (crear, leer, actualizar y eliminar)
-   Guardado de recetas favoritas
-   Comentarios y valoraciones
-   Gestión de perfil personal (nombre, descripción, foto de perfil)
-   Buscador y filtros
-   Sistema de historial de búsqueda
-   Sección de ayuda en WebView con contenido local

------------------------------------------------------------------------

## 📱 Compatibilidad

-   **Plataforma:** Android\
-   **Versión mínima:** Android 5.0 (Lollipop) --- API 21
-   **Versión objetivo:** Android 13 (Tiramisu) --- API 33
-   **Permisos requeridos:**
    -   Cámara
    -   Almacenamiento (para subir imágenes de recetas)

------------------------------------------------------------------------

## 🛠️ Tecnologías utilizadas

-   **Android Studio + Kotlin**
-   **Firebase Authentication**
-   **MariaDB + PHP (API REST)**
-   **Docker en Raspberry Pi 5**
-   **Cloudflare Tunnel + dominio: alesismedia.es**
-   **Visual Studio Code** (para la vista de ayuda HTML5)
-   **Retrofit + Moshi** (para comunicación cliente-servidor)
-   **GitHub** para control de versiones

------------------------------------------------------------------------

## 🔌 Arquitectura

    Android App (Kotlin)
       ↓ Retrofit
    API REST (PHP) → MariaDB (Docker en Raspberry Pi)
       ↑
    Firebase Auth

-   API alojada en `https://api.alesismedia.es/API/crud/`
-   Base de datos en Docker + MariaDB
-   Autenticación con Firebase
-   Dominio gestionado con Cloudflare Tunnel

------------------------------------------------------------------------

## 👨‍💻 Autor

**Alejandro Moreno Lechado**\
[Repositorio en GitHub](https://github.com/Alesis124/Cocinetica)
