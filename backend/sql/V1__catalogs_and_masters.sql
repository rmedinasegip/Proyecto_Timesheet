-- Fase 1: Catálogos y maestros JORUPE
--
-- Convenciones confirmadas releyendo los documentos de detalle
-- (Pantalla CRUD de Proyecto y Otros / Riesgos / News / Schedule):
--   - Todo master tins_* usa una columna "code" genérica como llave de
--     negocio (no "codecompany"/"codeuser"/etc. dentro de la propia tabla;
--     esos nombres los usan las tablas HIJAS al apuntar hacia acá).
--   - tins_catalogueitem se referencia como (codeinstance, codecat, codeitem)
--     -- confirmado en 3 documentos distintos (proyecto/riesgos/news/schedule)
--     via "tins_catalogueitem.codecat" / "tins_catalogueitem.codeitem".
--   - tins_company.code es alfanumérico corto (ej. 'LLA-EC'); tins_person /
--     tins_customer / tins_user usan "code" numérico autoincremental
--     (confirmado por el ejemplo "usercreate ... Ej: 1005" en los docs).
--   - tins_customer NO tiene columna propia de nombre: su nombre se resuelve
--     vía codeperson -> tins_person.name (confirmado literalmente en
--     "Pantalla CRUD de Proyecto y Otros.docx": "Cliente: tins_person.name,
--     donde ... tins_person.code = tins_customer.codeperson"). tins_person
--     actúa como maestro genérico de "partes" (personas Y organizaciones),
--     reutilizado para consultores y para clientes.
--   - usercreate/userlastmodify se guardan como Number (bigint) pero SIN
--     FK de base de datos hacia tins_user: hay dependencia circular de
--     bootstrap (tins_company necesitaría a tins_user, tins_user necesita a
--     tins_company) y, además, un registro de auditoría no debe quedar
--     bloqueado si el usuario que lo creó es eliminado más adelante.
--   - codeinstance/codecompany del piloto son fijos (no hay selector de
--     instancia en ningún mockup): codeinstance='LLACSAA',
--     codecompany='LLA-EC' (LLACSAA Ecuador), documentado en docs/erd.md.

-- ============================================================
-- Catálogos (alcance: codeinstance, sin codecompany)
-- ============================================================

CREATE TABLE tins_catalogue (
  codeinstance    varchar(20)   NOT NULL,
  codecat         varchar(50)   NOT NULL,
  name            varchar(200)  NOT NULL,
  usercreate      bigint        NOT NULL,
  userlastmodify  bigint        NOT NULL,
  datecreate      timestamp     NOT NULL DEFAULT now(),
  datemodify      timestamp     NOT NULL DEFAULT now(),
  CONSTRAINT pk_tins_catalogue PRIMARY KEY (codeinstance, codecat)
);

CREATE TABLE tins_catalogueitem (
  codeinstance    varchar(20)   NOT NULL,
  codecat         varchar(50)   NOT NULL,
  codeitem        varchar(50)   NOT NULL,
  name            varchar(200)  NOT NULL,
  usercreate      bigint        NOT NULL,
  userlastmodify  bigint        NOT NULL,
  datecreate      timestamp     NOT NULL DEFAULT now(),
  datemodify      timestamp     NOT NULL DEFAULT now(),
  CONSTRAINT pk_tins_catalogueitem PRIMARY KEY (codeinstance, codecat, codeitem),
  CONSTRAINT fk_catalogueitem_catalogue FOREIGN KEY (codeinstance, codecat)
    REFERENCES tins_catalogue (codeinstance, codecat)
);

-- ============================================================
-- Maestros JORUPE (alcance: codeinstance + codecompany)
-- ============================================================

CREATE TABLE tins_company (
  codeinstance    varchar(20)   NOT NULL,
  code            varchar(20)   NOT NULL,
  name            varchar(200)  NOT NULL,
  usercreate      bigint        NOT NULL,
  userlastmodify  bigint        NOT NULL,
  datecreate      timestamp     NOT NULL DEFAULT now(),
  datemodify      timestamp     NOT NULL DEFAULT now(),
  CONSTRAINT pk_tins_company PRIMARY KEY (codeinstance, code)
);

CREATE TABLE tins_person (
  codeinstance    varchar(20)   NOT NULL,
  codecompany     varchar(20)   NOT NULL,
  code            bigint GENERATED ALWAYS AS IDENTITY,
  name            varchar(200)  NOT NULL,
  usercreate      bigint        NOT NULL,
  userlastmodify  bigint        NOT NULL,
  datecreate      timestamp     NOT NULL DEFAULT now(),
  datemodify      timestamp     NOT NULL DEFAULT now(),
  CONSTRAINT pk_tins_person PRIMARY KEY (codeinstance, codecompany, code),
  CONSTRAINT fk_person_company FOREIGN KEY (codeinstance, codecompany)
    REFERENCES tins_company (codeinstance, code)
);

CREATE TABLE tins_customer (
  codeinstance    varchar(20)   NOT NULL,
  codecompany     varchar(20)   NOT NULL,
  code            bigint GENERATED ALWAYS AS IDENTITY,
  codeperson      bigint        NOT NULL,
  usercreate      bigint        NOT NULL,
  userlastmodify  bigint        NOT NULL,
  datecreate      timestamp     NOT NULL DEFAULT now(),
  datemodify      timestamp     NOT NULL DEFAULT now(),
  CONSTRAINT pk_tins_customer PRIMARY KEY (codeinstance, codecompany, code),
  CONSTRAINT fk_customer_company FOREIGN KEY (codeinstance, codecompany)
    REFERENCES tins_company (codeinstance, code),
  CONSTRAINT fk_customer_person FOREIGN KEY (codeinstance, codecompany, codeperson)
    REFERENCES tins_person (codeinstance, codecompany, code)
);

CREATE TABLE tins_user (
  codeinstance    varchar(20)   NOT NULL,
  codecompany     varchar(20)   NOT NULL,
  code            bigint GENERATED ALWAYS AS IDENTITY,
  codeperson      bigint        NOT NULL,
  usercreate      bigint        NOT NULL,
  userlastmodify  bigint        NOT NULL,
  datecreate      timestamp     NOT NULL DEFAULT now(),
  datemodify      timestamp     NOT NULL DEFAULT now(),
  CONSTRAINT pk_tins_user PRIMARY KEY (codeinstance, codecompany, code),
  CONSTRAINT fk_user_company FOREIGN KEY (codeinstance, codecompany)
    REFERENCES tins_company (codeinstance, code),
  CONSTRAINT fk_user_person FOREIGN KEY (codeinstance, codecompany, codeperson)
    REFERENCES tins_person (codeinstance, codecompany, code)
);
