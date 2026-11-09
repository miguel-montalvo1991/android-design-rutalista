-- Esquema de la base de datos de RutaLista (SQLite).
-- Se ejecuta cada vez que arranca el servidor; "IF NOT EXISTS" evita errores
-- si las tablas ya existen.

-- Una jornada agrupa los pedidos de un día de trabajo del domiciliario.
CREATE TABLE IF NOT EXISTS workdays (
  id         INTEGER PRIMARY KEY AUTOINCREMENT,
  name       TEXT    NOT NULL,
  -- Estado controlado: solo se aceptan estos dos valores.
  status     TEXT    NOT NULL DEFAULT 'OPEN'
             CHECK (status IN ('OPEN', 'CLOSED')),
  -- Fechas en formato ISO 8601 (texto), como "2026-11-09T14:30:00.000Z".
  started_at TEXT    NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ', 'now')),
  closed_at  TEXT
);

-- Un pedido pertenece a una jornada. Refleja la entidad DeliveryOrder de la app.
CREATE TABLE IF NOT EXISTS delivery_orders (
  id                  INTEGER PRIMARY KEY AUTOINCREMENT,
  workday_id          INTEGER NOT NULL
                      REFERENCES workdays(id) ON DELETE CASCADE,
  customer_name       TEXT    NOT NULL,
  phone               TEXT,
  address_text        TEXT    NOT NULL,
  neighborhood        TEXT,
  municipality        TEXT,
  -- Las coordenadas son opcionales: primero se guarda el borrador y después
  -- se verifica la dirección.
  latitude            REAL    CHECK (latitude  IS NULL OR latitude  BETWEEN -90  AND 90),
  longitude           REAL    CHECK (longitude IS NULL OR longitude BETWEEN -180 AND 180),
  product_description TEXT,
  -- Por defecto "pendiente de confirmar": nunca se asume que está pagado.
  payment_status      TEXT    NOT NULL DEFAULT 'PENDING_CONFIRMATION'
                      CHECK (payment_status IN
                        ('PAID', 'COLLECT_ON_DELIVERY', 'PENDING_CONFIRMATION', 'NOT_APPLICABLE')),
  deadline_at         TEXT,
  notes               TEXT,
  delivery_status     TEXT    NOT NULL DEFAULT 'PENDING'
                      CHECK (delivery_status IN
                        ('PENDING', 'IN_PROGRESS', 'DELIVERED', 'FAILED', 'RESCHEDULED')),
  -- Posición elegida por el usuario en la secuencia de entrega.
  position            INTEGER,
  source_text         TEXT,
  created_at          TEXT    NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ', 'now'))
);

-- Índice para consultar rápido los pedidos de una jornada.
CREATE INDEX IF NOT EXISTS idx_orders_workday ON delivery_orders(workday_id);