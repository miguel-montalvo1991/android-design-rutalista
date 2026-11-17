'use strict';

// Módulo de acceso a la base de datos SQLite.
// Su única responsabilidad es abrir la base y garantizar que el esquema exista.

const fs = require('node:fs');
const path = require('node:path');
const Database = require('better-sqlite3');

// Ubicación por defecto del archivo: backend/data/rutalista.db
// Se puede cambiar con la variable de entorno DB_PATH.
const DEFAULT_DB_PATH = path.join(__dirname, '..', '..', 'data', 'rutalista.db');

/**
 * Abre (o crea) la base de datos y aplica el esquema.
 * @param {string} dbPath ruta del archivo, o ':memory:' para pruebas.
 * @returns {import('better-sqlite3').Database}
 */
function createDatabase(dbPath = process.env.DB_PATH || DEFAULT_DB_PATH) {
  // Si es un archivo real, crea la carpeta contenedora si no existe.
  if (dbPath !== ':memory:') {
    fs.mkdirSync(path.dirname(dbPath), { recursive: true });
  }

  const db = new Database(dbPath);

  // SQLite no hace cumplir las llaves foráneas por defecto: hay que activarlo.
  db.pragma('foreign_keys = ON');

  // Lee y ejecuta el esquema (crea las tablas si aún no existen).
  const schema = fs.readFileSync(path.join(__dirname, 'schema.sql'), 'utf8');
  db.exec(schema);

  return db;
}

module.exports = { createDatabase };