-- Fase 4: Avance de Proyectos + reporte equivalente a la hoja "Detalle" del
-- Excel AD-RE-04_TIME_SHEET_2026.xlsx.
--
-- Sin documento CRUD dedicado para "Cambios Aprobados" (a diferencia de
-- Proyecto/Schedule/Riesgos/News en Fase 2) — reconstruida directamente de
-- las columnas que la hoja "Detalle" muestra para esa sección: Fase,
-- Entregable, Motivo, Consecuencia, Aprobador, Empresa, Variación de días,
-- fechas. Se sigue la misma convención ya usada en tprj_project_risk/
-- tprj_project_news para "Aprobador"/"Empresa": columnas varchar libres, no
-- FK hacia tins_user/tins_company — esos dos módulos ya establecieron ese
-- patrón para "responsable"/"compañía" en vez de referencia relacional, y
-- aquí no hay ningún documento que indique lo contrario.
--
-- El resto del reporte (Días del proyecto por fase, Hitos) NO requiere tabla
-- nueva: se lee directamente de tprj_project (fila TOTAL) y de las filas
-- "Padre"/"Hijo" ya existentes en tprj_project_schedule (fases/hitos), más
-- una agregación en vivo de tprj_project_timesheet.hoursconsumed para
-- "Días Utilizados (según T.S.)" — ver ProjectProgressReportService para el
-- detalle de la interpretación de columnas (documentado también en
-- docs/erd.md).

CREATE TABLE tprj_project_change (
  codeinstance      varchar(20)   NOT NULL,
  codecompany       varchar(20)   NOT NULL,
  seqproject        bigint        NOT NULL,
  seqchange         bigint GENERATED ALWAYS AS IDENTITY,
  changedate        date          NOT NULL,
  phase             varchar(100)  NOT NULL,
  deliverable       varchar(200)  NOT NULL,
  reason            varchar(500)  NOT NULL,
  consequence       varchar(500),
  approvedby        varchar(100)  NOT NULL,
  company           varchar(100)  NOT NULL,
  daysvariation     numeric       NOT NULL,
  plannedapplydate  date,
  usercreate        bigint        NOT NULL,
  userlastmodify    bigint        NOT NULL,
  datecreate        timestamp     NOT NULL DEFAULT now(),
  datemodify        timestamp     NOT NULL DEFAULT now(),
  CONSTRAINT pk_tprj_project_change PRIMARY KEY (seqchange),
  CONSTRAINT fk_change_company FOREIGN KEY (codeinstance, codecompany)
    REFERENCES tins_company (codeinstance, code),
  CONSTRAINT fk_change_project FOREIGN KEY (seqproject)
    REFERENCES tprj_project (seq)
);

CREATE TABLE tprj_project_change_his (
  seq_his           bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  actiondml         varchar(30)   NOT NULL,
  datechange        timestamp     NOT NULL DEFAULT now(),
  userchange        bigint        NOT NULL,
  codeinstance      varchar(20)   NOT NULL,
  codecompany       varchar(20)   NOT NULL,
  seqproject        bigint        NOT NULL,
  seqchange         bigint        NOT NULL,
  changedate        date          NOT NULL,
  phase             varchar(100)  NOT NULL,
  deliverable       varchar(200)  NOT NULL,
  reason            varchar(500)  NOT NULL,
  consequence       varchar(500),
  approvedby        varchar(100)  NOT NULL,
  company           varchar(100)  NOT NULL,
  daysvariation     numeric       NOT NULL,
  plannedapplydate  date,
  usercreate        bigint        NOT NULL,
  userlastmodify    bigint        NOT NULL,
  datecreate        timestamp     NOT NULL,
  datemodify        timestamp     NOT NULL
);
