# JLG_CZG_SICENET - SICENET Mobile Application

## 📱 Descripción

Aplicación Android moderna para autenticación y consulta de perfil académico en la plataforma SICENET (Sistema de Información Centralizado de Educación del Tecnológico).

Construida con:
- **Android 5.0+** (minSdk = 24)
- **Jetpack Compose** para UI
- **Kotlin** como lenguaje
- **Retrofit** para requests SOAP
- **Material Design 3** para diseño

## ✨ Características

✅ **Autenticación SOAP** - Conexión segura a SICENET  
✅ **Gestión de Cookies** - Persistencia de sesión  
✅ **UI Moderna** - Jetpack Compose  
✅ **Patrón MVVM** - Arquitectura limpia  
✅ **Perfil Académico** - Visualización de datos  
✅ **Navegación Fluida** - Entre pantallas  
✅ **Manejo de Errores** - Robusto y detallado  
✅ **Temas Personalizados** - Material Design 3  

## 🚀 Inicio Rápido

### Compilación

```bash
# Sincronizar Gradle
./gradlew sync

# Compilar proyecto
./gradlew build

# Generar APK debug
./gradlew assembleDebug

# Instalar en emulador/dispositivo
./gradlew installDebug
```

### Ejecución

```bash
# Desde Android Studio
- Click en "Run" o Shift + F10
- Seleccionar emulador o dispositivo

# Desde terminal
./gradlew installDebug
./gradlew shell am start -n com.example.jlg_czg_sicenet/.MainActivity
```

## 📋 Requisitos

- Android Studio Hedgehog+
- JDK 11+
- Android SDK compileSdk 36
- Gradle 8.1+

## 🎯 Flujo de Uso

```
1. Iniciar aplicación
2. Ver pantalla de Login
3. Ingresar matrícula y contraseña válidas de SICENET
4. Clickear "Ingresar"
5. Esperar autenticación (2-3 segundos)
6. Ver pantalla de perfil académico
7. Clickear ← para cerrar sesión y volver al login
```

## 📁 Estructura del Proyecto

```
app/src/main/java/com/example/jlg_czg_sicenet/
├── ui/
│   ├── JLGSICENETApp.kt           # Navegación
│   ├── screens/
│   │   ├── LoginScreen.kt         # Formulario
│   │   ├── LoginViewModel.kt      # Lógica login
│   │   ├── ProfileScreen.kt       # Perfil
│   │   └── ProfileViewModel.kt    # Lógica perfil
│   └── theme/
│       ├── Color.kt               # Colores
│       └── Theme.kt               # Tema
├── data/
│   ├── SNRepository.kt            # Repository pattern
│   ├── AppContainer.kt            # Inyección de deps
│   ├── AddCookiesInterceptor.kt   # Cookies en peticiones
│   └── ReceivedCookiesInterceptor.kt # Captura de cookies
├── network/
│   └── SICENETWService.kt         # Interfaz SOAP
├── model/
│   ├── ProfileStudent.kt          # Perfil data class
│   └── ResponseAcceso.kt          # Respuesta SOAP
├── MainActivity.kt                # Activity principal
└── JLGSICENETApplication.kt       # Application class
```

## 🔒 Seguridad

- ✅ HTTPS obligatorio
- ✅ Validación de inputs
- ✅ Manejo seguro de cookies
- ✅ Logging detallado
- ✅ Headers de seguridad

## 🐛 Debugging

### Ver Logs
```
Android Studio → View → Tool Windows → Logcat
Filtrar por "SNRepository"
```

### Ver Cookies Almacenadas
```bash
adb shell
cd /data/data/com.example.jlg_czg_sicenet/shared_prefs/
cat androidx.preference_preferences.xml | grep PREF_COOKIES
```

### HTTP Monitoring
- HttpLoggingInterceptor configurado con Level.BODY
- Ver peticiones y respuestas completas en Logcat

## 📖 Documentación

Consultar los siguientes archivos:
- **IMPLEMENTACION_SICENET.md** - Detalles técnicos
- **CHECKLIST_IMPLEMENTACION.md** - Verificación de requisitos
- **GUIA_USO.md** - Manual de usuario
- **RESUMEN_IMPLEMENTACION.md** - Descripción general

## 🎨 Personalización

### Cambiar Colores
Editar `ui/theme/Color.kt`

### Cambiar Textos
Modificar directamente en los archivos de screens o usar strings.xml

### Cambiar Servidor
En `data/AppContainer.kt`, cambiar:
```kotlin
private val baseUrlSN = "https://sicenet.itsur.edu.mx"
```

## 📦 Dependencias

```gradle
// Retrofit & HTTP
com.squareup.retrofit2:retrofit:2.9.0
com.squareup.retrofit2:converter-gson:2.9.0
com.squareup.okhttp3:okhttp:4.11.0
com.squareup.okhttp3:logging-interceptor:4.11.0

// XML Parsing
org.simpleframework:simple-xml:2.7.1

// Navigation
androidx.navigation:navigation-compose:2.7.5

// Preferences
androidx.preference:preference-ktx:1.2.1
```

## ⚠️ Problemas Conocidos

**Ninguno reportado hasta el momento**

## 🤝 Contribución

Para contribuir:
1. Fork el proyecto
2. Crear branch para feature
3. Commit cambios
4. Push a branch
5. Crear Pull Request

## 📄 Licencia

Proyecto desarrollado con fines educativos.

## 📞 Soporte

Para problemas o preguntas:
1. Revisar la documentación
2. Verificar logs en Logcat
3. Consultar la guía de uso

## 🎉 Versión

**Version**: 1.0  
**Release Date**: Febrero 2026  
**Status**: ✅ Stable

---

## 📝 Changelog

### v1.0 (Febrero 2026)
- ✅ Implementación inicial completa
- ✅ Autenticación SOAP funcional
- ✅ Gestión de sesiones
- ✅ UI con Compose
- ✅ Navegación navegación
- ✅ Documentación completa

---

## 🙌 Agradecimientos

Proyecto desarrollado usando:
- Kotlin
- Jetpack Compose
- Android Architecture Components
- Material Design 3
- Retrofit
- OkHttp

---

**¡Gracias por usar SICENET Mobile App!** 🚀
