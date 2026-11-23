# RutaLista

Aplicación Android para planear recorridos de entrega. Permite definir un origen, buscar y agregar destinos, calcular una ruta y proponer un orden optimizado de visitas.

El repositorio contiene dos aplicaciones:

- `app/`: cliente Android desarrollado con Kotlin y Jetpack Compose.
- `backend/`: API REST local desarrollada con Node.js, Express y SQLite.

## Estado actual

> Última revisión: 22 de noviembre de 2026.

### Aplicación Android

| Funcionalidad | Estado |
| --- | --- |
| Interfaz principal para crear una ruta | Disponible |
| Búsqueda de direcciones mediante OpenRouteService | Disponible |
| Selección de origen y destinos | Disponible |
| Obtención de ubicación del dispositivo (GPS) | Disponible, requiere permiso del usuario |
| Selección de puntos en mapa | Disponible |
| Cálculo de ruta para vehículo | Disponible |
| Optimización del orden de destinos | Disponible |
| Persistir jornadas y pedidos en el backend propio | Pendiente |
| Consumir la API propia desde Android | Pendiente |

### Backend

| Funcionalidad | Estado |
| --- | --- |
| Servidor Express | Disponible |
| `GET /health` | Disponible |
| Base SQLite y esquema de datos | Disponible |
| Tablas `workdays` y `delivery_orders` | Disponible |
| Pruebas automatizadas del servidor y base de datos | 6 pruebas aprobadas |
| Endpoints para jornadas | Pendiente |
| Endpoints para pedidos | Pendiente |
| Integración Android ↔ backend | Pendiente |

## Tecnologías

### Android

- Kotlin 2.2 y Java 17
- Jetpack Compose y Material 3
- ViewModel, `StateFlow` y corrutinas
- Retrofit y Gson para HTTP
- OpenStreetMap (`osmdroid`) y Google Play Services Location
- OpenRouteService para geocodificación, rutas y optimización

### Backend

- Node.js 22 o superior
- Express 5
- SQLite mediante `better-sqlite3`
- Pruebas con el ejecutor integrado `node:test`

## Estructura

```text
android-design-rutalista/
├── app/                         # Aplicación Android
│   └── src/main/java/com/rutalista/app/
│       ├── core/                # Utilidades compartidas, por ejemplo PolylineDecoder
│       ├── data/
│       │   ├── model/           # Clases de datos actuales
│       │   ├── remote/          # RetrofitClient y OrsApi
│       │   └── repository/      # Acceso a OpenRouteService y ubicación
│       ├── navigation/          # Navegación de la app
│       ├── ui/
│       │   ├── components/      # Componentes reutilizables de Compose
│       │   ├── screens/         # Pantallas completas
│       │   ├── theme/           # Tema, colores y tipografía
│       │   └── viewmodel/       # Estado y lógica de presentación
│       └── MainActivity.kt      # Punto de entrada Android
├── backend/
│   ├── src/
│   │   ├── app.js               # Configuración de Express y rutas generales
│   │   ├── server.js            # Inicio del servidor y de SQLite
│   │   └── db/
│   │       ├── database.js      # Apertura de SQLite y aplicación del esquema
│   │       └── schema.sql       # Tablas y restricciones
│   └── test/                    # Pruebas del backend
└── README.md
```

La organización Android evolucionará gradualmente hacia `data/network`, `data/api`, `data/dto`, `data/repository` y `presentation`. No se moverán paquetes masivamente mientras se desarrolla una funcionalidad, para no introducir errores de imports innecesarios.

## Requisitos

- Android Studio con Android SDK 37 y JDK 17.
- Node.js 22 o superior para el backend.
- Una clave de API de OpenRouteService para las funciones de búsqueda, ruta y optimización.

## Ejecutar la app Android

1. Abre la carpeta raíz del proyecto en Android Studio.
2. En `local.properties`, conserva la propiedad `sdk.dir` que creó Android Studio y añade tu clave:

   ```properties
   ORS_API_KEY=tu_clave_de_openrouteservice
   ```

3. Sincroniza Gradle y ejecuta la app en un emulador o dispositivo Android.

También puedes compilar desde PowerShell, en la raíz del repositorio:

```powershell
.\gradlew.bat assembleDebug
```

`local.properties` no se sube al repositorio. No publiques la clave de OpenRouteService en código, capturas o commits.

## Ejecutar el backend

Desde la carpeta `backend`:

```powershell
npm install
npm run dev
```

El servidor escucha en `http://localhost:3000` por defecto. Para comprobar que inició:

```powershell
Invoke-RestMethod http://localhost:3000/health
```

La primera ejecución crea `backend/data/rutalista.db` y aplica el esquema. Esa base está ignorada por Git porque puede contener datos de pedidos.

### Pruebas del backend

```powershell
cd backend
npm test
```

Las pruebas usan una base SQLite en memoria; no modifican la base local.

## Modelo de datos actual

- Una **jornada** (`workdays`) agrupa los pedidos de un día de trabajo y puede estar `OPEN` o `CLOSED`.
- Un **pedido de entrega** (`delivery_orders`) pertenece a una jornada. Incluye cliente, dirección, coordenadas opcionales, pago, estado de entrega y posición en la ruta.
- SQLite garantiza las relaciones mediante claves foráneas y elimina los pedidos de una jornada si esta se borra.

## Próximos pasos

1. Implementar `POST /workdays` para abrir una jornada.
2. Implementar consulta y cierre de jornadas.
3. Crear los endpoints CRUD de pedidos.
4. Definir el contrato JSON de la API.
5. Conectar el cliente Android a la API propia mediante Retrofit.
6. Reorganizar los paquetes Android de forma incremental al trabajar cada capa.

## Seguridad

- No subas `local.properties`, `.env`, claves de API ni bases SQLite.
- No publiques direcciones ni teléfonos de clientes en repositorios, capturas o mensajes.
- La API propia se desarrolla primero para uso local; antes de desplegarla se deberán añadir autenticación, validación de entrada, configuración de CORS y gestión segura de variables de entorno.
