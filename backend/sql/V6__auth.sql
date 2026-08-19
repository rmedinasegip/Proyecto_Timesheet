-- Fase 5: autenticación (JWT) + roles.
--
-- No se crea una tabla de usuarios nueva: tins_user (JORUPE) ya es la
-- identidad canónica de "usuario del sistema" en todo el modelo (PM,
-- integrante de equipo, autor de timesheets, autorizador...) — agregarle
-- las columnas de login es más consistente que introducir una segunda
-- tabla de credenciales paralela.
--
-- Roles: se sigue el patrón de catálogo de dos columnas ya usado en todo el
-- proyecto (statuscat/status, riskstatuscat/riskstatus, ...) en vez de un
-- enum embebido — mismo criterio, un solo lugar (tins_catalogueitem) para
-- listar/traducir valores. El plan original mencionaba una "relación
-- AUTHORIZERCAT" sin detallarla; PRJ_USERROLECAT + CON/AUT es la
-- interpretación de esa idea consistente con el resto del esquema.
--
-- email/passwordhash quedan NULL tras esta migración — los siembra
-- PasswordSeedRunner (backend/src/main/java/.../auth/PasswordSeedRunner.java)
-- en el primer arranque de la app: el hash bcrypt no se puede escribir a
-- mano en SQL plano, así que sembrarlo en Java (idempotente, como
-- seedAdminUser() en el proyecto Next.js hermano) evita depender de pgcrypto
-- o de una herramienta externa solo para generar un hash.

ALTER TABLE tins_user
  ADD COLUMN email varchar(150),
  ADD COLUMN passwordhash varchar(255),
  ADD COLUMN rolecat varchar(50),
  ADD COLUMN role varchar(50);

CREATE UNIQUE INDEX uq_tins_user_email ON tins_user (email);

ALTER TABLE tins_user
  ADD CONSTRAINT fk_user_role FOREIGN KEY (codeinstance, rolecat, role)
    REFERENCES tins_catalogueitem (codeinstance, codecat, codeitem);

INSERT INTO tins_catalogue (codeinstance, codecat, name, usercreate, userlastmodify)
VALUES ('LLACSAA', 'PRJ_USERROLECAT', 'Rol de usuario', 1, 1);

INSERT INTO tins_catalogueitem (codeinstance, codecat, codeitem, name, usercreate, userlastmodify) VALUES
  ('LLACSAA', 'PRJ_USERROLECAT', 'CON', 'Consultor',   1, 1),
  ('LLACSAA', 'PRJ_USERROLECAT', 'AUT', 'Autorizador', 1, 1);

-- Emails de los 10 consultores ya sembrados en V2 (mismo mapeo nombre->code
-- que V2 usó al crear estos tins_user vía SELECT ... WHERE name IN (...)).
UPDATE tins_user u
SET email = m.email,
    rolecat = 'PRJ_USERROLECAT',
    role = CASE WHEN m.person_name = 'Ricardo Medina' THEN 'AUT' ELSE 'CON' END
FROM (VALUES
  ('Ricardo Medina',        'ricardo.medina@llacsaa.com'),
  ('Freddy Romero',         'freddy.romero@llacsaa.com'),
  ('Alan Pérez',            'alan.perez@llacsaa.com'),
  ('Valeria Llulema',       'valeria.llulema@llacsaa.com'),
  ('Elizabeth Mata',        'elizabeth.mata@llacsaa.com'),
  ('Eduardo Cruz',          'eduardo.cruz@llacsaa.com'),
  ('Julio García',          'julio.garcia@llacsaa.com'),
  ('Alcivar Llacsahuanga',  'alcivar.llacsahuanga@llacsaa.com'),
  ('Elena Yánez',           'elena.yanez@llacsaa.com'),
  ('Marisol Quispe',        'marisol.quispe@llacsaa.com')
) AS m(person_name, email),
tins_person p
WHERE p.name = m.person_name
  AND p.codeinstance = 'LLACSAA' AND p.codecompany = 'LLA-EC'
  AND u.codeperson = p.code
  AND u.codeinstance = 'LLACSAA' AND u.codecompany = 'LLA-EC';
