// backend/src/repositories/workdays.repository.js
// Acceso a datos de la tabla workdays. No valida ni responde HTTP.

// Convierte una fila de la BD (snake_case) al formato JSON de la API (camelCase)
function aModelo(fila) {
  return {
    id: fila.id,
    name: fila.name,
    status: fila.status,
    startedAt: fila.started_at,
    closedAt: fila.closed_at,
  };
}

function crearRepositorioJornadas(db) {
  // Sentencias preparadas: más rápidas y protegen contra inyección SQL
  const insertar = db.prepare('INSERT INTO workdays (name) VALUES (?)');
  const buscarPorId = db.prepare('SELECT * FROM workdays WHERE id = ?');
  // Las más recientes primero; el id desempata jornadas creadas en el mismo instante
  const listarTodas = db.prepare('SELECT * FROM workdays ORDER BY started_at DESC, id DESC');

  return {
    // Inserta la jornada y devuelve el registro completo ya guardado
    crear(name) {
      const resultado = insertar.run(name);
      return aModelo(buscarPorId.get(resultado.lastInsertRowid));
    },

    // Devuelve todas las jornadas (lista vacía si no hay ninguna)
    listar() {
      return listarTodas.all().map(aModelo);
    },

    // Devuelve la jornada o null si el id no existe
    obtener(id) {
      const fila = buscarPorId.get(id);
      return fila ? aModelo(fila) : null;
    },
  };
}

module.exports = { crearRepositorioJornadas };