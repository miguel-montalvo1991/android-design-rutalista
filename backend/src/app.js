// backend/src/app.js
// Crea y configura la aplicación de Express, pero NO la pone a escuchar.
// Separarla de server.js permite probarla sin ocupar un puerto fijo.

const express = require('express');

// Fábrica de la aplicación: cada llamada devuelve una app nueva e independiente
function crearApp() {
  const app = express();

  // Permite leer cuerpos JSON en las peticiones (POST y PUT)
  app.use(express.json());

  // Ruta de salud: sirve para comprobar que el servidor responde
  app.get('/health', (req, res) => {
    res.json({
      status: 'ok',
      service: 'rutalista-backend',
      time: new Date().toISOString(),
    });
  });

  // Aquí se montarán las rutas de jornadas y pedidos (Fase 6)

  // Ruta no encontrada: responde 404 con el formato de error de la API
  app.use((req, res) => {
    res.status(404).json({
      error: {
        code: 'NOT_FOUND',
        message: `No existe la ruta ${req.method} ${req.originalUrl}`,
      },
    });
  });

  // Manejador de errores: Express 5 envía aquí también los errores de funciones async
  app.use((err, req, res, next) => {
    // Los errores de cliente (ej. JSON mal formado) traen status 4xx; el resto se trata como 500
    const status = err.status || err.statusCode || 500;

    // Se registra solo el mensaje técnico, nunca el cuerpo de la petición (puede traer datos personales)
    console.error(`[error ${status}] ${err.message}`);

    res.status(status).json({
      error: {
        code: status >= 500 ? 'INTERNAL_ERROR' : 'BAD_REQUEST',
        message:
          status >= 500
            ? 'Ocurrió un error interno del servidor'
            : 'La solicitud no es válida',
      },
    });
  });

  return app;
}

module.exports = { crearApp };