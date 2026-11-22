// backend/src/controllers/workdays.controller.js
// Recibe la petición, llama al servicio y devuelve la respuesta HTTP.

function crearControladorJornadas(servicio) {
  return {
    crear(req, res) {
      const jornada = servicio.crear(req.body);
      res.status(201).json(jornada); // 201 = recurso creado
    },

    listar(req, res) {
      res.json(servicio.listar()); // 200 por defecto
    },

    obtener(req, res) {
      res.json(servicio.obtener(req.params.id));
    },
  };
}

module.exports = { crearControladorJornadas };