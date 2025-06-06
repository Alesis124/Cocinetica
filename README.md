# Cocinética 🍳📱

**Cocinética** es una aplicación móvil desarrollada en Android Studio con Kotlin, cuyo objetivo es facilitar la creación, exploración y valoración de recetas de cocina. Los usuarios pueden registrarse, iniciar sesión, crear recetas, comentar, guardar favoritas y mucho más.

---

## 🚀 Funcionalidades principales

- Registro e inicio de sesión con Firebase Authentication
- CRUD de recetas (crear, leer, actualizar y eliminar)
- Guardado de recetas favoritas por usuario
- Comentarios y valoraciones de recetas
- Gestión de perfil personal (nombre, descripción, imagen de perfil)
- Buscador y filtros
- Sistema de historial de búsqueda
- Sección de ayuda en WebView con contenido local

---

## 🛠️ Tecnologías utilizadas

- **Android Studio + Kotlin**
- **Firebase Authentication**
- **MariaDB + PHP (API REST)**
- **Docker en Raspberry Pi 5**
- **Cloudflare Tunnel + dominio: alesismedia.es**
- **Visual Studio Code (para vista de ayuda HTML5)**
- **Retrofit + Moshi** (para comunicación cliente-servidor)
- **GitHub** para control de versiones

---

## 🔌 Arquitectura

```
Android App (Kotlin)
   ↓ Retrofit
API REST (PHP) → MariaDB (Docker en Raspberry Pi)
   ↑
Firebase Auth
```

- API alojada en `https://api.alesismedia.es/API/crud/`
- Base de datos con Docker + MariaDB
- Autenticación con Firebase
- Dominio gestionado con Cloudflare Tunnel

---

## 📸 Capturas destacadas

1. Inicio y búsqueda de recetas
2. Vista detallada de receta
3. Crear receta paso a paso
4. Panel de usuario y edición de cuenta

---

## 📁 Estructura del proyecto

```
Cocinetica/
├── app/                  ← Código fuente principal (Android)
├── api/                  ← Scripts PHP (leer.php, insertar.php, etc.)
├── assets/ayuda/         ← HTML para WebView de ayuda
├── docker/               ← Configuración inicial de contenedores
└── README.md             ← Este documento
```

---

## 👨‍💻 Autor

**Alejandro Moreno Lechado**  
[Repositorio en GitHub](https://github.com/Alesis124/Cocinetica)

