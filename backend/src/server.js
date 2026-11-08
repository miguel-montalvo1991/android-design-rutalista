// backend/src/server.js
// Servidor mínimo para comprobar que el entorno funciona.
// En las fases siguientes se separará en rutas, controladores y servicios.

const express = require('express');

// Crea la aplicación de Express
const app = express();

// Puerto: si existe la variable de entorno PORT la usa; si no, 3000
const PORT = process.env.PORT || 3000;

// Ruta de salud: sirve para comprobar que el servidor responde
// (más adelante la app Android puede usarla para detectar si hay conexión con la API)
app.get('/health', (req, res) => {
  res.json({
    status: 'ok',
    service: 'rutalista-backend',
    time: new Date().toISOString(),
  });
});

// Pone el servidor a escuchar peticiones
app.listen(PORT, () => {
  console.log(`RutaLista backend escuchando en el puerto ${PORT}`);
});