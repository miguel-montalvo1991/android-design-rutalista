# RutaLista Inteligente

Aplicación Android para domiciliarios independientes que reciben pedidos por
WhatsApp y necesitan organizar sus entregas en el Área Metropolitana del
Valle de Aburrá (Antioquia, Colombia).

> **Estado:** en desarrollo. Fase actual: preparación del proyecto.
> Proyecto de grado del programa Análisis y Desarrollo de Software (ADSO) - SENA.

## Problema

Los domiciliarios reciben listas desorganizadas de pedidos por WhatsApp
(cliente, dirección, teléfono, pago, hora máxima). Ordenarlas a mano consume
tiempo y puede causar recorridos ineficientes, retrasos u olvidos.

## Qué hará la aplicación

- Importar una lista de pedidos compartida desde WhatsApp o pegada a mano.
- Reconocer los datos y dejar que el usuario los revise y corrija.
- Verificar direcciones y usar la ubicación actual como punto de partida.
- Sugerir un orden de entrega, que el usuario puede cambiar.
- Abrir la navegación, WhatsApp o una llamada hacia cada cliente.
- Registrar el resultado de cada entrega y resumir la jornada.

La app asiste al domiciliario; no le impone el recorrido.

## Herramientas TIC

1. **API REST** (Node.js + Express + SQLite): pedidos, jornadas y estados.
2. **Geolocalización, mapas y rutas**: GPS del dispositivo y proveedor de rutas.
3. **Integración con WhatsApp**: texto compartido, enlaces a chats y llamadas.

## Arquitectura

App Android con patrón **MVVM** (Kotlin, Jetpack Compose, Material 3):
Model - View - ViewModel - Repository, conectada al backend mediante Retrofit.

## Estructura del repositorio

```
ruta-lista/
├── app/         Aplicación Android (Kotlin + Jetpack Compose)
├── gradle/      Configuración de compilación
├── backend/     API REST (pendiente)
├── docs/        Documentación académica (pendiente)
└── README.md
```

## Requisitos y ejecución

Se completarán a medida que avance el desarrollo
(Android Studio, Node.js, instrucciones del emulador y del backend).

## Privacidad

Durante el desarrollo solo se usan datos ficticios. Las claves de servicios
externos nunca se incluyen en el repositorio.

## Autores

- Luis Miguel Montalvo
- Diego Andrez bermudez