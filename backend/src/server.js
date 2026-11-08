// backend/src/server.js
// Punto de entrada: arranca el servidor HTTP con la app definida en app.js.

const { crearApp } = require('./app');

// Puerto: si existe la variable de entorno PORT la usa; si no, 3000
const PORT = process.env.PORT || 3000;

const app = crearApp();

// Pone el servidor a escuchar peticiones
app.listen(PORT, () => {
  console.log(`RutaLista backend escuchando en el puerto ${PORT}`);
});