-- Fase 6: Módulo de avance semanal de Schedules por consultor
--
-- Fuente: "Especificación de Proyecto_Control_Proyectos_TimeSheets_2.docx"
-- (modelo de datos nuevo). Normaliza el mismo typo de prefijo ya resuelto en
-- Fase 2/3 (trpj_ -> tprj_, ver nota de cabecera de V3__project_module.sql).
--
-- Cadena: tprj_project -> tprj_project_schedule (actividad "Hijo",
-- memberuser = consultor asignado) -> tprj_project_schedulets (cabecera de
-- seguimiento semanal de esa actividad, 1 por actividad trackeada) ->
-- tprj_projectts_week (1 por semana) -> tprj_projectts_week_detail (1:1 con
-- la semana, día 1-7 = lunes-domingo).
--
-- Decisiones aplicadas (confirmadas con el usuario, ver plan Fase 6):
--   - day1_perc..day7_perc son un snapshot acumulado por día ("% completado
--     a esa fecha"), no un aporte incremental — el último día no nulo de la
--     semana es el que se propaga a tprj_project_schedule.advrealperc.
--   - tprj_projectts_week SÍ lleva flujo de aprobación (statuscat/status +
--     reviewedby), mismo patrón que tprj_project_timesheet en Fase 3/
--     PRJ_TIMESHEETSTATUSCAT — catálogo nuevo PRJ_TSWEEKSTATUSCAT con los
--     mismos 3 valores REG/APR/REC.
--   - UNIQUE(seqproject, seqschedule) en tprj_project_schedulets y
--     UNIQUE(seqtimesheets, datefrom) en tprj_projectts_week son la
--     validación de "no duplicar actividad+semana" que pide el
--     requerimiento — el service hace get-or-create sobre esas claves.
--   - tprj_projectts_week_detail es 1:1 con la semana (UNIQUE(seqtsweek)):
--     se crea/actualiza en la misma transacción que la semana, nunca por
--     separado.

CREATE TABLE tprj_project_schedulets (
  codeinstance    varchar(20) NOT NULL,
  codecompany     varchar(20) NOT NULL,
  seqts           bigint GENERATED ALWAYS AS IDENTITY,
  seqproject      bigint      NOT NULL,
  seqschedule     bigint      NOT NULL,
  statementdate   date,
  datefrom        date        NOT NULL,
  dateto          date,
  usercreate      bigint      NOT NULL,
  userlastmodify  bigint      NOT NULL,
  datecreate      timestamp   NOT NULL DEFAULT now(),
  datemodify      timestamp   NOT NULL DEFAULT now(),
  CONSTRAINT pk_tprj_project_schedulets PRIMARY KEY (seqts),
  CONSTRAINT uq_schedulets_project_schedule UNIQUE (seqproject, seqschedule),
  CONSTRAINT fk_schedulets_company FOREIGN KEY (codeinstance, codecompany)
    REFERENCES tins_company (codeinstance, code),
  CONSTRAINT fk_schedulets_project FOREIGN KEY (seqproject)
    REFERENCES tprj_project (seq),
  CONSTRAINT fk_schedulets_schedule FOREIGN KEY (seqschedule)
    REFERENCES tprj_project_schedule (seqschedule)
);

CREATE TABLE tprj_project_schedulets_his (
  seq_his         bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  actiondml       varchar(30) NOT NULL,
  datechange      timestamp   NOT NULL DEFAULT now(),
  userchange      bigint      NOT NULL,
  codeinstance    varchar(20) NOT NULL,
  codecompany     varchar(20) NOT NULL,
  seqts           bigint      NOT NULL,
  seqproject      bigint      NOT NULL,
  seqschedule     bigint      NOT NULL,
  statementdate   date,
  datefrom        date        NOT NULL,
  dateto          date,
  usercreate      bigint      NOT NULL,
  userlastmodify  bigint      NOT NULL,
  datecreate      timestamp   NOT NULL,
  datemodify      timestamp   NOT NULL
);

CREATE TABLE tprj_projectts_week (
  codeinstance    varchar(20) NOT NULL,
  codecompany     varchar(20) NOT NULL,
  seqtsweek       bigint GENERATED ALWAYS AS IDENTITY,
  seqtimesheets   bigint      NOT NULL,
  datefrom        date        NOT NULL,
  dateto          date        NOT NULL,
  statuscat       varchar(50) NOT NULL,
  status          varchar(50) NOT NULL,
  reviewedby      bigint,
  usercreate      bigint      NOT NULL,
  userlastmodify  bigint      NOT NULL,
  datecreate      timestamp   NOT NULL DEFAULT now(),
  datemodify      timestamp   NOT NULL DEFAULT now(),
  CONSTRAINT pk_tprj_projectts_week PRIMARY KEY (seqtsweek),
  CONSTRAINT uq_tsweek_schedulets_datefrom UNIQUE (seqtimesheets, datefrom),
  CONSTRAINT fk_tsweek_company FOREIGN KEY (codeinstance, codecompany)
    REFERENCES tins_company (codeinstance, code),
  CONSTRAINT fk_tsweek_schedulets FOREIGN KEY (seqtimesheets)
    REFERENCES tprj_project_schedulets (seqts),
  CONSTRAINT fk_tsweek_status FOREIGN KEY (codeinstance, statuscat, status)
    REFERENCES tins_catalogueitem (codeinstance, codecat, codeitem),
  CONSTRAINT fk_tsweek_reviewedby FOREIGN KEY (codeinstance, codecompany, reviewedby)
    REFERENCES tins_user (codeinstance, codecompany, code)
);

CREATE TABLE tprj_projectts_week_his (
  seq_his         bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  actiondml       varchar(30) NOT NULL,
  datechange      timestamp   NOT NULL DEFAULT now(),
  userchange      bigint      NOT NULL,
  codeinstance    varchar(20) NOT NULL,
  codecompany     varchar(20) NOT NULL,
  seqtsweek       bigint      NOT NULL,
  seqtimesheets   bigint      NOT NULL,
  datefrom        date        NOT NULL,
  dateto          date        NOT NULL,
  statuscat       varchar(50) NOT NULL,
  status          varchar(50) NOT NULL,
  reviewedby      bigint,
  usercreate      bigint      NOT NULL,
  userlastmodify  bigint      NOT NULL,
  datecreate      timestamp   NOT NULL,
  datemodify      timestamp   NOT NULL
);

CREATE TABLE tprj_projectts_week_detail (
  codeinstance    varchar(20)   NOT NULL,
  codecompany     varchar(20)   NOT NULL,
  seqtsweekdt     bigint GENERATED ALWAYS AS IDENTITY,
  seqtsweek       bigint        NOT NULL,
  datefrom        date          NOT NULL,
  dateto          date          NOT NULL,
  day1_perc       numeric(5,2),
  day2_perc       numeric(5,2),
  day3_perc       numeric(5,2),
  day4_perc       numeric(5,2),
  day5_perc       numeric(5,2),
  day6_perc       numeric(5,2),
  day7_perc       numeric(5,2),
  usercreate      bigint        NOT NULL,
  userlastmodify  bigint        NOT NULL,
  datecreate      timestamp     NOT NULL DEFAULT now(),
  datemodify      timestamp     NOT NULL DEFAULT now(),
  CONSTRAINT pk_tprj_projectts_week_detail PRIMARY KEY (seqtsweekdt),
  CONSTRAINT uq_tsweekdt_week UNIQUE (seqtsweek),
  CONSTRAINT fk_tsweekdt_company FOREIGN KEY (codeinstance, codecompany)
    REFERENCES tins_company (codeinstance, code),
  CONSTRAINT fk_tsweekdt_week FOREIGN KEY (seqtsweek)
    REFERENCES tprj_projectts_week (seqtsweek)
);

CREATE TABLE tprj_projectts_week_detail_his (
  seq_his         bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  actiondml       varchar(30)   NOT NULL,
  datechange      timestamp     NOT NULL DEFAULT now(),
  userchange      bigint        NOT NULL,
  codeinstance    varchar(20)   NOT NULL,
  codecompany     varchar(20)   NOT NULL,
  seqtsweekdt     bigint        NOT NULL,
  seqtsweek       bigint        NOT NULL,
  datefrom        date          NOT NULL,
  dateto          date          NOT NULL,
  day1_perc       numeric(5,2),
  day2_perc       numeric(5,2),
  day3_perc       numeric(5,2),
  day4_perc       numeric(5,2),
  day5_perc       numeric(5,2),
  day6_perc       numeric(5,2),
  day7_perc       numeric(5,2),
  usercreate      bigint        NOT NULL,
  userlastmodify  bigint        NOT NULL,
  datecreate      timestamp     NOT NULL,
  datemodify      timestamp     NOT NULL
);

-- ============================================================
-- Catálogo nuevo: PRJ_TSWEEKSTATUSCAT (mismo patrón/valores que
-- PRJ_TIMESHEETSTATUSCAT en V2__seed_catalogs_and_masters.sql)
-- ============================================================

INSERT INTO tins_catalogue (codeinstance, codecat, name, usercreate, userlastmodify) VALUES
  ('LLACSAA', 'PRJ_TSWEEKSTATUSCAT', 'Estado de avance semanal', 1, 1);

INSERT INTO tins_catalogueitem (codeinstance, codecat, codeitem, name, usercreate, userlastmodify) VALUES
  ('LLACSAA', 'PRJ_TSWEEKSTATUSCAT', 'REG', 'Registrado', 1, 1),
  ('LLACSAA', 'PRJ_TSWEEKSTATUSCAT', 'APR', 'Aprobado',   1, 1),
  ('LLACSAA', 'PRJ_TSWEEKSTATUSCAT', 'REC', 'Rechazado',  1, 1);
