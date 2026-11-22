// backend/src/services/workdays.service.js
// Reglas de negocio y validación de entrada de las jornadas.

const { ApiError } = require('../errors');

const MAX_NOMBRE = 100;

function crearServicioJornadas(repositorio) {
  return {
    crear(cuerpo) {
      // En Express 5, req.body puede ser undefined si no llegó JSON
      const nombre = cuerpo?.name;

      if (typeof nombre !== 'string' || nombre.trim() === '') {
        throw new ApiError(400, 'VALIDATION_ERROR', 'El campo "name" es obligatorio');
      }
      if (nombre.trim().length > MAX_NOMBRE) {
        throw new ApiError(
          400,
          'VALIDATION_ERROR',
          `El campo "name" no puede superar ${MAX_NOMBRE} caracteres`
        );
      }

      return repositorio.crear(nombre.trim());
    },

    listar() {
      return repositorio.listar();
    },

    obtener(idTexto) {
      // El id llega como texto en la URL: debe ser un entero positivo (ej. "3", no "abc" ni "1.5")
      if (!/^[1-9]\d*$/.test(idTexto)) {
        throw new ApiError(400, 'VALIDATION_ERROR', 'El id de la jornada debe ser un número entero positivo');
      }

      const jornada = repositorio.obtener(Number(idTexto));
      if (!jornada) {
        throw new ApiError(404, 'NOT_FOUND', `No existe la jornada ${idTexto}`);
      }
      return jornada;
    },
  };
}

module.exports = { crearServicioJornadas };