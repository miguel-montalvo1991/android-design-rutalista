# RutaLista Inteligente

Aplicación Android para domiciliarios independientes que necesitan organizar y optimizar sus recorridos de entrega en el Área Metropolitana del Valle de Aburrá, Antioquia, Colombia.

**Estado:** En desarrollo.
**Programa:** Análisis y Desarrollo de Software (ADSO) – SENA.
**Plataforma:** Android.
**Lenguaje:** Kotlin.

## 1. Descripción del proyecto

RutaLista busca facilitar la organización de direcciones y recorridos para domiciliarios independientes.

La aplicación pretende reducir el tiempo dedicado a organizar entregas manualmente y ayudar a seleccionar recorridos más eficientes.

El usuario podrá establecer un punto de origen, agregar diferentes destinos y utilizar herramientas de geolocalización, mapas y optimización de rutas.

## 2. Objetivo general

Desarrollar una aplicación móvil que permita gestionar ubicaciones y optimizar recorridos de entrega mediante servicios de geolocalización y cálculo de rutas.

## 3. Tecnologías utilizadas

| Tecnología | Función |
|---|---|
| Kotlin | Lenguaje de programación |
| Android Studio | Entorno de desarrollo |
| Jetpack Compose | Construcción de interfaces |
| Material 3 | Componentes visuales |
| MVVM | Arquitectura de la aplicación |
| Retrofit | Comunicación con servicios REST |
| OpenStreetMap | Visualización cartográfica |
| osmdroid | Integración de mapas en Android |
| Google Play Services Location | Obtención de ubicación GPS |
| OpenRouteService | Geocodificación y servicios de rutas |

## 4. Arquitectura del proyecto

La aplicación utiliza el patrón arquitectónico **MVVM (Model – View – ViewModel)**, complementado con una capa Repository y una capa de acceso remoto.

La separación de responsabilidades permite mantener el código organizado, facilitar el mantenimiento y evitar que las interfaces contengan lógica de negocio.

### Model

Representa las estructuras de datos utilizadas por la aplicación.

Ubicación: `data/model/`

Archivos principales:

- `Direccion.kt`: representa las direcciones y ubicaciones.
- `GeocodeResponse.kt`: estructura de respuesta de geocodificación.
- `RutaRequest.kt`: modelo de solicitud de rutas.
- `RutaResponse.kt`: modelo de respuesta de rutas.
- `OptimizacionRequest.kt`: datos enviados para optimizar recorridos.
- `OptimizacionResponse.kt`: respuesta de optimización.

### View

Corresponde a las interfaces construidas con Jetpack Compose.

Ubicación: `ui/screens/` y `ui/components/`

Archivos principales:

- `RutaScreen.kt`: pantalla principal de gestión de rutas.
- `HeaderRutaLista.kt`: encabezado visual de la aplicación.
- `MapaSeleccionDialog.kt`: componente para seleccionar ubicaciones mediante el mapa.

Las interfaces deben concentrarse en mostrar información y comunicar las acciones del usuario al ViewModel.

### ViewModel

Administra el estado de la interfaz y coordina las operaciones de la aplicación.

Ubicación: `ui/viewmodel/`

Archivos:

- `RutaViewModel.kt`: gestiona las operaciones relacionadas con rutas y ubicaciones.
- `RutaUiState.kt`: representa el estado de la pantalla de rutas.
- `BusquedaUiState.kt`: representa el estado de búsqueda de direcciones.

El ViewModel se comunica con Repository y expone estados observables para que la interfaz se actualice.

### Repository

Ubicación: `data/repository/`

Archivo:

- `RutaRepository.kt`

Centraliza el acceso a los servicios externos y las operaciones relacionadas con geocodificación, consulta de rutas y optimización.

### Remote

Ubicación: `data/remote/`

Archivos:

- `OrsApi.kt`: define los servicios de comunicación con OpenRouteService.
- `RetrofitClient.kt`: configura el cliente Retrofit para las solicitudes HTTP.

### Theme

Ubicación: `ui/theme/`

Archivos:

- `Color.kt`: configuración de colores.
- `Theme.kt`: configuración del tema visual.
- `Type.kt`: configuración tipográfica.

## 5. Estructura del proyecto

```text
com.rutalista.app/
│
├── MainActivity.kt
│
├── data/
│   ├── model/
│   │   ├── Direccion.kt
│   │   ├── GeocodeResponse.kt
│   │   ├── OptimizacionRequest.kt
│   │   ├── OptimizacionResponse.kt
│   │   ├── RutaRequest.kt
│   │   └── RutaResponse.kt
│   │
│   ├── remote/
│   │   ├── OrsApi.kt
│   │   └── RetrofitClient.kt
│   │
│   └── repository/
│       └── RutaRepository.kt
│
└── ui/
    ├── components/
    │   ├── HeaderRutaLista.kt
    │   └── MapaSeleccionDialog.kt
    │
    ├── screens/
    │   └── RutaScreen.kt
    │
    ├── theme/
    │   ├── Color.kt
    │   ├── Theme.kt
    │   └── Type.kt
    │
    └── viewmodel/
        ├── BusquedaUiState.kt
        ├── RutaUiState.kt
        └── RutaViewModel.kt
```

## 6. Funcionalidades desarrolladas

La implementación actual contempla:

- Interfaz principal de RutaLista.
- Selección de ubicaciones.
- Integración de mapas mediante OpenStreetMap.
- Obtención de ubicación mediante GPS.
- Comunicación con OpenRouteService mediante Retrofit.
- Modelos para solicitudes y respuestas de geocodificación.
- Modelos para consulta y optimización de rutas.
- Separación de responsabilidades mediante MVVM.

**Nota:** La integración técnica no significa que todas las funcionalidades estén completamente validadas. La búsqueda de direcciones, incorporación de destinos y autenticación de OpenRouteService continúan en proceso de revisión y pruebas.

## 7. Funcionalidades pendientes

- Corregir y validar la autenticación de OpenRouteService.
- Garantizar la selección y almacenamiento de múltiples destinos.
- Mejorar la búsqueda manual de direcciones.
- Validar la selección de ubicaciones desde el mapa.
- Validar la selección de ubicaciones mediante GPS.
- Probar el cálculo de rutas.
- Probar la optimización del orden de destinos.
- Implementar pruebas unitarias para el ViewModel y Repository.
- Desarrollar futuras funcionalidades de gestión de entregas e integración con WhatsApp.

## 8. Configuración del proyecto

### Requisitos

- Android Studio.
- JDK compatible con la configuración Gradle del proyecto.
- SDK de Android requerido por el proyecto.
- Conexión a Internet.
- Dispositivo Android o emulador.

### Clonar el repositorio

```bash
git clone https://github.com/miguel-montalvo1991/android-design-rutalista.git
```

Abrir el proyecto desde Android Studio y esperar la sincronización de Gradle.

### Configurar OpenRouteService

La aplicación requiere una clave válida de OpenRouteService.

La clave se configura localmente mediante `local.properties`, de acuerdo con la configuración Gradle del proyecto.

Ejemplo de propiedad:

```properties
ORS_API_KEY=TU_CLAVE_DE_OPENROUTESERVICE
```

No se debe publicar la clave real ni incluir `local.properties` en los commits.

Después de configurar la propiedad, sincronizar Gradle y reconstruir la aplicación.

## 9. Buenas prácticas de desarrollo

- Mantener la arquitectura MVVM.
- Separar las responsabilidades entre Model, View, ViewModel y Repository.
- Evitar lógica de negocio y llamadas directas a API dentro de los Composables.
- Mantener los modelos de datos organizados.
- Utilizar control de versiones con Git.
- Crear ramas independientes para cada línea de trabajo.
- Realizar commits descriptivos.
- Validar los cambios antes de integrarlos a la rama principal.
- No publicar claves, credenciales ni archivos locales.

## 10. Control de versiones y colaboración

El desarrollo se realiza utilizando Git y GitHub.

Cada colaborador trabaja en su propia rama para evitar interferencias durante la implementación.

Las integraciones se realizan mediante revisión de cambios y pruebas antes de fusionar el trabajo.

## 11. Privacidad y seguridad

- No incluir claves privadas en el repositorio.
- No publicar credenciales de servicios externos.
- Utilizar datos ficticios durante las pruebas.
- Solicitar permisos de ubicación cuando sean necesarios.
- Respetar los permisos concedidos por el usuario.

## 12. Autores

- Luis Miguel Montalvo.
- Diego Bermúdez.

Proyecto académico desarrollado en el programa de Análisis y Desarrollo de Software (ADSO) del SENA.