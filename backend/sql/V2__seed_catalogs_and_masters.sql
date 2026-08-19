-- Fase 1: datos de prueba (catálogos + maestros JORUPE)
--
-- codeinstance/codecompany fijos para el piloto: 'LLACSAA' / 'LLA-EC'
-- (LLACSAA Ecuador), ver docs/erd.md.
--
-- Bootstrap de auditoría: Ricardo Medina se inserta primero en tins_person
-- y tins_user, por lo que su "code" identity queda determinísticamente en 1
-- (usercreate/userlastmodify=1 en todo el resto de este script no es FK,
-- ver nota en V1).

-- ============================================================
-- tins_company — confirmado por la hoja "Tablas" del Excel de registro
-- diario ("Lista de empresas": LLACSAA Perú, LLACSAA Ecuador, Externos)
-- ============================================================

INSERT INTO tins_company (codeinstance, code, name, usercreate, userlastmodify) VALUES
  ('LLACSAA', 'LLA-EC', 'LLACSAA Ecuador', 1, 1),
  ('LLACSAA', 'LLA-PE', 'LLACSAA Perú',    1, 1),
  ('LLACSAA', 'EXT',    'Externos',        1, 1);

-- ============================================================
-- tins_person — consultores (hoja "Registro diario", lista "Consultor")
-- + personas que representan a los 3 clientes de prueba (ver tins_customer)
-- ============================================================

INSERT INTO tins_person (codeinstance, codecompany, name, usercreate, userlastmodify) VALUES
  ('LLACSAA', 'LLA-EC', 'Ricardo Medina',        1, 1),
  ('LLACSAA', 'LLA-EC', 'Freddy Romero',         1, 1),
  ('LLACSAA', 'LLA-EC', 'Alan Pérez',            1, 1),
  ('LLACSAA', 'LLA-EC', 'Valeria Llulema',       1, 1),
  ('LLACSAA', 'LLA-EC', 'Elizabeth Mata',        1, 1),
  ('LLACSAA', 'LLA-EC', 'Eduardo Cruz',          1, 1),
  ('LLACSAA', 'LLA-EC', 'Julio García',          1, 1),
  ('LLACSAA', 'LLA-EC', 'Alcivar Llacsahuanga',  1, 1),
  ('LLACSAA', 'LLA-EC', 'Elena Yánez',           1, 1),
  ('LLACSAA', 'LLA-EC', 'Marisol Quispe',        1, 1),
  ('LLACSAA', 'LLA-EC', 'Ripley',                1, 1),
  ('LLACSAA', 'LLA-EC', 'Flamingo',              1, 1),
  ('LLACSAA', 'LLA-EC', 'Llacsaa',               1, 1);

-- ============================================================
-- tins_user — un usuario por consultor (los clientes NO tienen usuario)
-- ============================================================

INSERT INTO tins_user (codeinstance, codecompany, codeperson, usercreate, userlastmodify)
SELECT 'LLACSAA', 'LLA-EC', code, 1, 1
FROM tins_person
WHERE codeinstance = 'LLACSAA' AND codecompany = 'LLA-EC'
  AND name IN (
    'Ricardo Medina', 'Freddy Romero', 'Alan Pérez', 'Valeria Llulema',
    'Elizabeth Mata', 'Eduardo Cruz', 'Julio García', 'Alcivar Llacsahuanga',
    'Elena Yánez', 'Marisol Quispe'
  );

-- ============================================================
-- tins_customer — clientes de prueba (hoja "Registro diario", columna
-- "Cliente": Ripley, Flamingo, Llacsaa). Sin columna propia de nombre:
-- se resuelve vía codeperson -> tins_person.name (ver nota en V1).
-- ============================================================

INSERT INTO tins_customer (codeinstance, codecompany, codeperson, usercreate, userlastmodify)
SELECT 'LLACSAA', 'LLA-EC', code, 1, 1
FROM tins_person
WHERE codeinstance = 'LLACSAA' AND codecompany = 'LLA-EC'
  AND name IN ('Ripley', 'Flamingo', 'Llacsaa');

-- ============================================================
-- Catálogos — namespace PRJ_, alcance codeinstance (sin codecompany)
-- ============================================================

INSERT INTO tins_catalogue (codeinstance, codecat, name, usercreate, userlastmodify) VALUES
  ('LLACSAA', 'PRJ_SYSTEMSCAT',         'Sistemas',                          1, 1),
  ('LLACSAA', 'PRJ_MODULECAT',          'Módulos',                           1, 1),
  ('LLACSAA', 'PRJ_ACTIVITYTYTYPECAT',  'Tipo de actividad',                 1, 1),
  ('LLACSAA', 'PRJ_TIMESHEETSTATUSCAT', 'Estado de timesheet',               1, 1),
  ('LLACSAA', 'PRJ_PROJECTROLCAT',      'Rol en el proyecto',                1, 1),
  ('LLACSAA', 'PRJ_RISKTYPEIMPACTCAT',  'Tipo de impacto de riesgo',         1, 1),
  ('LLACSAA', 'PRJ_NEWTYPEIMPACTCAT',   'Tipo de impacto de novedad',        1, 1),
  ('LLACSAA', 'PRJ_PROJECTSTATUSCAT',   'Estado del proyecto',               1, 1),
  ('LLACSAA', 'PRJ_RISKSTATUSCAT',      'Estado del riesgo',                 1, 1),
  ('LLACSAA', 'PRJ_NEWSTATUSCAT',       'Estado de la novedad',              1, 1);

-- PRJ_SYSTEMSCAT — confirmado, hoja "Tablas" del Excel ("Sistema": Originador,
-- Smart Engine, SwitchClient, T24/Transact)
INSERT INTO tins_catalogueitem (codeinstance, codecat, codeitem, name, usercreate, userlastmodify) VALUES
  ('LLACSAA', 'PRJ_SYSTEMSCAT', 'ORIGINADOR',   'Originador',   1, 1),
  ('LLACSAA', 'PRJ_SYSTEMSCAT', 'SMARTENGINE',  'Smart Engine', 1, 1),
  ('LLACSAA', 'PRJ_SYSTEMSCAT', 'SWITCHCLIENT', 'SwitchClient', 1, 1),
  ('LLACSAA', 'PRJ_SYSTEMSCAT', 'T24',          'Transact',     1, 1);

-- PRJ_MODULECAT — confirmado, hoja "Tablas" del Excel
INSERT INTO tins_catalogueitem (codeinstance, codecat, codeitem, name, usercreate, userlastmodify) VALUES
  ('LLACSAA', 'PRJ_MODULECAT', 'INC',    'Atención incidencia',              1, 1),
  ('LLACSAA', 'PRJ_MODULECAT', 'ADM',    'Módulo administrativo',            1, 1),
  ('LLACSAA', 'PRJ_MODULECAT', 'GEN',    'Módulo generales de instancia',    1, 1),
  ('LLACSAA', 'PRJ_MODULECAT', 'RRHH',   'Módulo de recursos humanos',       1, 1),
  ('LLACSAA', 'PRJ_MODULECAT', 'CONT',   'Módulo de contabilidad',           1, 1),
  ('LLACSAA', 'PRJ_MODULECAT', 'AGEN',   'Agencia de viajes',                1, 1),
  ('LLACSAA', 'PRJ_MODULECAT', 'PROV',   'Proveedores',                      1, 1),
  ('LLACSAA', 'PRJ_MODULECAT', 'BANC',   'Bancos',                           1, 1),
  ('LLACSAA', 'PRJ_MODULECAT', 'AF',     'Activos fijos',                    1, 1),
  ('LLACSAA', 'PRJ_MODULECAT', 'REPLEG', 'Reportes legales',                 1, 1),
  ('LLACSAA', 'PRJ_MODULECAT', 'CCHICA', 'Caja chica',                       1, 1),
  ('LLACSAA', 'PRJ_MODULECAT', 'FAC',    'Facturación',                      1, 1),
  ('LLACSAA', 'PRJ_MODULECAT', 'CXCXP',  'Cuentas por cobrar / por pagar',   1, 1),
  ('LLACSAA', 'PRJ_MODULECAT', 'INVE',   'Inventarios',                      1, 1),
  ('LLACSAA', 'PRJ_MODULECAT', 'T24_DEP','T24 - Depósitos',                  1, 1),
  ('LLACSAA', 'PRJ_MODULECAT', 'T24_PRE','T24 - Préstamos',                  1, 1),
  ('LLACSAA', 'PRJ_MODULECAT', 'T24_TDC','T24 - Tarjeta Débito',             1, 1),
  ('LLACSAA', 'PRJ_MODULECAT', 'T24_CLI','T24 - Clientes',                   1, 1),
  ('LLACSAA', 'PRJ_MODULECAT', 'NOFUNC', 'Requerimientos no funcionales',    1, 1);

-- PRJ_ACTIVITYTYTYPECAT — confirmado, hoja "Tablas" del Excel + INC
-- (Atención incidencia) confirmado en "Pantalla CRUD de Riesgos...docx"
INSERT INTO tins_catalogueitem (codeinstance, codecat, codeitem, name, usercreate, userlastmodify) VALUES
  ('LLACSAA', 'PRJ_ACTIVITYTYTYPECAT', 'DES', 'Desarrollo',          1, 1),
  ('LLACSAA', 'PRJ_ACTIVITYTYTYPECAT', 'QA',  'QA',                  1, 1),
  ('LLACSAA', 'PRJ_ACTIVITYTYTYPECAT', 'ESP', 'Especificaciones',    1, 1),
  ('LLACSAA', 'PRJ_ACTIVITYTYTYPECAT', 'ANA', 'Análisis',            1, 1),
  ('LLACSAA', 'PRJ_ACTIVITYTYTYPECAT', 'DOC', 'Documentación',       1, 1),
  ('LLACSAA', 'PRJ_ACTIVITYTYTYPECAT', 'COM', 'Comercial',           1, 1),
  ('LLACSAA', 'PRJ_ACTIVITYTYTYPECAT', 'PRE', 'Presentaciones',      1, 1),
  ('LLACSAA', 'PRJ_ACTIVITYTYTYPECAT', 'CAP', 'Capacitación',        1, 1),
  ('LLACSAA', 'PRJ_ACTIVITYTYTYPECAT', 'REU', 'Reunión',             1, 1),
  ('LLACSAA', 'PRJ_ACTIVITYTYTYPECAT', 'ISO', 'ISO',                 1, 1),
  ('LLACSAA', 'PRJ_ACTIVITYTYTYPECAT', 'INC', 'Atención incidencia', 1, 1);

-- PRJ_TIMESHEETSTATUSCAT — confirmado, hoja "Registro diario" del Excel
-- (columna "Status": Registrado / Aprobado / Rechazado)
INSERT INTO tins_catalogueitem (codeinstance, codecat, codeitem, name, usercreate, userlastmodify) VALUES
  ('LLACSAA', 'PRJ_TIMESHEETSTATUSCAT', 'REG', 'Registrado', 1, 1),
  ('LLACSAA', 'PRJ_TIMESHEETSTATUSCAT', 'APR', 'Aprobado',   1, 1),
  ('LLACSAA', 'PRJ_TIMESHEETSTATUSCAT', 'REC', 'Rechazado',  1, 1);

-- PRJ_PROJECTROLCAT — confirmado, "Pantalla CRUD de Schedule de
-- Proyecto.docx" ("Ej: Desarrollador, QA, Líder")
INSERT INTO tins_catalogueitem (codeinstance, codecat, codeitem, name, usercreate, userlastmodify) VALUES
  ('LLACSAA', 'PRJ_PROJECTROLCAT', 'DEV', 'Desarrollador', 1, 1),
  ('LLACSAA', 'PRJ_PROJECTROLCAT', 'QA',  'QA',            1, 1),
  ('LLACSAA', 'PRJ_PROJECTROLCAT', 'LID', 'Líder',         1, 1);

-- PRJ_RISKTYPEIMPACTCAT — confirmado, "Pantalla CRUD de Riesgos de
-- Proyecto.docx" ("Ej: LOW-Low, MED-Medium, HIG-High")
INSERT INTO tins_catalogueitem (codeinstance, codecat, codeitem, name, usercreate, userlastmodify) VALUES
  ('LLACSAA', 'PRJ_RISKTYPEIMPACTCAT', 'LOW', 'Low',    1, 1),
  ('LLACSAA', 'PRJ_RISKTYPEIMPACTCAT', 'MED', 'Medium', 1, 1),
  ('LLACSAA', 'PRJ_RISKTYPEIMPACTCAT', 'HIG', 'High',   1, 1);

-- PRJ_NEWTYPEIMPACTCAT — confirmado, "Pantalla CRUD de News de
-- Proyecto.docx" ("Ej: LOW-Low, MED-Medium, HIG-High")
INSERT INTO tins_catalogueitem (codeinstance, codecat, codeitem, name, usercreate, userlastmodify) VALUES
  ('LLACSAA', 'PRJ_NEWTYPEIMPACTCAT', 'LOW', 'Low',    1, 1),
  ('LLACSAA', 'PRJ_NEWTYPEIMPACTCAT', 'MED', 'Medium', 1, 1),
  ('LLACSAA', 'PRJ_NEWTYPEIMPACTCAT', 'HIG', 'High',   1, 1);

-- PRJ_PROJECTSTATUSCAT — confirmado, "Pantalla CRUD de Schedule de
-- Proyecto.docx" ("Ej: PLA-Planned, ONG-Ongoing, SL-Slow, CLO-Closed")
INSERT INTO tins_catalogueitem (codeinstance, codecat, codeitem, name, usercreate, userlastmodify) VALUES
  ('LLACSAA', 'PRJ_PROJECTSTATUSCAT', 'PLA', 'Planned',  1, 1),
  ('LLACSAA', 'PRJ_PROJECTSTATUSCAT', 'ONG', 'Ongoing',  1, 1),
  ('LLACSAA', 'PRJ_PROJECTSTATUSCAT', 'SL',  'Slow',     1, 1),
  ('LLACSAA', 'PRJ_PROJECTSTATUSCAT', 'CLO', 'Closed',   1, 1);

-- PRJ_RISKSTATUSCAT / PRJ_NEWSTATUSCAT — el campo de estado en sí fue
-- confirmado por el usuario contra el documento general (ver plan), pero
-- ninguno de los 4 documentos de detalle enumera sus valores. Placeholder
-- de datos de prueba con un ciclo de vida genérico; a confirmar con el
-- cliente antes de Fase 4.
INSERT INTO tins_catalogueitem (codeinstance, codecat, codeitem, name, usercreate, userlastmodify) VALUES
  ('LLACSAA', 'PRJ_RISKSTATUSCAT', 'ABI', 'Abierto',        1, 1),
  ('LLACSAA', 'PRJ_RISKSTATUSCAT', 'SEG', 'En seguimiento', 1, 1),
  ('LLACSAA', 'PRJ_RISKSTATUSCAT', 'CER', 'Cerrado',        1, 1);

INSERT INTO tins_catalogueitem (codeinstance, codecat, codeitem, name, usercreate, userlastmodify) VALUES
  ('LLACSAA', 'PRJ_NEWSTATUSCAT', 'ABI', 'Abierto',        1, 1),
  ('LLACSAA', 'PRJ_NEWSTATUSCAT', 'SEG', 'En seguimiento', 1, 1),
  ('LLACSAA', 'PRJ_NEWSTATUSCAT', 'CER', 'Cerrado',        1, 1);
