// backend/src/errors.js
// Error controlado de la API: lleva el código HTTP y el código del formato
// { error: { code, message } } que ya usamos en las respuestas.

class ApiError extends Error {
  constructor(status, code, message) {
    super(message);
    this.status = status; // ej. 400, 404
    this.code = code;     // ej. 'VALIDATION_ERROR'
  }
}

module.exports = { ApiError };