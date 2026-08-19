-- Fase 3: Registro de TimeSheets (tprj_project_timesheet + historial)
--
-- Esta tabla no tenía CRUD dedicado en la especificación (a diferencia del
-- módulo Proyecto en Fase 2) — se reconstruyó en la Fase 0/plan a partir de
-- la hoja "Registro diario" del Excel
-- "AD-RE-04 TIME SHEET 2026 Plantilla Ricardo Medina.xlsx". Se aplican aquí
-- las mismas convenciones ya confirmadas en Fase 2 releyendo los documentos
-- de detalle de Proyecto/Schedule/Riesgos/News:
--   - Pares "<campo>cat"/"<campo>" (systemcat/system, modulecat/module,
--     activitytypecat/activitytype, statuscat/status) con FK compuesta
--     hacia tins_catalogueitem — igual que tprj_project/tprj_project_schedule,
--     no la nomenclatura de una sola columna que traía el borrador original
--     del plan.
--   - seqts = PK bigint autoincremental (como seq/seqschedule/seqrisk).
--   - usercreate/userlastmodify sin FK (mismo motivo de bootstrap de Fase 1).
--   - Historial: tprj_project_timesheet_his, snapshot de fila completa con
--     actiondml NEW/UPDATE/DELETE (mismo patrón de las 5 tablas de Fase 2).
--   - hoursconsumed se calcula en la aplicación (TimesheetService), no como
--     columna generada por Postgres — mismo criterio que basedaystotal en
--     Fase 2 (evita la fricción de leer columnas GENERATED ALWAYS recién
--     insertadas sin un refresh adicional).
--
-- Reglas de negocio (Excel "Registro diario" + plan):
--   - seqproject es NULL cuando el registro es una incidencia sin proyecto
--     formal (columna "Incidencia" en vez de "Proyecto"/"Sprint").
--   - statuscat/status inicia en 'REG' (Registrado); un "autorizador" puede
--     pasar a 'APR'/'REC' vía un endpoint de revisión separado (ver
--     TimesheetController) que también setea reviewedby — no hay roles/auth
--     todavía (Fase 5), así que cualquier usuario puede llamar ese endpoint
--     en este piloto.

CREATE TABLE tprj_project_timesheet (
  codeinstance          varchar(20)   NOT NULL,
  codecompany           varchar(20)   NOT NULL,
  seqts                 bigint GENERATED ALWAYS AS IDENTITY,
  memberuser             bigint        NOT NULL,
  codecompanyconsultant varchar(20)   NOT NULL,
  codecustomer           bigint        NOT NULL,
  seqproject             bigint,
  systemcat              varchar(50)   NOT NULL,
  system                 varchar(50)   NOT NULL,
  modulecat               varchar(50)   NOT NULL,
  module                  varchar(50)   NOT NULL,
  sprint                  varchar(100),
  incidentref             varchar(300),
  activitytypecat         varchar(50)   NOT NULL,
  activitytype             varchar(50)   NOT NULL,
  activitydesc              varchar(500)  NOT NULL,
  tsdate                    date          NOT NULL,
  starttime                 time          NOT NULL,
  endtime                   time          NOT NULL,
  hoursconsumed             numeric       NOT NULL,
  statuscat                 varchar(50)   NOT NULL,
  status                    varchar(50)   NOT NULL,
  reviewedby                bigint,
  seqschedule               bigint,
  usercreate                bigint        NOT NULL,
  userlastmodify            bigint        NOT NULL,
  datecreate                timestamp     NOT NULL DEFAULT now(),
  datemodify                timestamp     NOT NULL DEFAULT now(),
  CONSTRAINT pk_tprj_project_timesheet PRIMARY KEY (seqts),
  CONSTRAINT fk_ts_company FOREIGN KEY (codeinstance, codecompany)
    REFERENCES tins_company (codeinstance, code),
  CONSTRAINT fk_ts_member FOREIGN KEY (codeinstance, codecompany, memberuser)
    REFERENCES tins_user (codeinstance, codecompany, code),
  CONSTRAINT fk_ts_companyconsultant FOREIGN KEY (codeinstance, codecompanyconsultant)
    REFERENCES tins_company (codeinstance, code),
  CONSTRAINT fk_ts_customer FOREIGN KEY (codeinstance, codecompany, codecustomer)
    REFERENCES tins_customer (codeinstance, codecompany, code),
  CONSTRAINT fk_ts_project FOREIGN KEY (seqproject)
    REFERENCES tprj_project (seq),
  CONSTRAINT fk_ts_system FOREIGN KEY (codeinstance, systemcat, system)
    REFERENCES tins_catalogueitem (codeinstance, codecat, codeitem),
  CONSTRAINT fk_ts_module FOREIGN KEY (codeinstance, modulecat, module)
    REFERENCES tins_catalogueitem (codeinstance, codecat, codeitem),
  CONSTRAINT fk_ts_activitytype FOREIGN KEY (codeinstance, activitytypecat, activitytype)
    REFERENCES tins_catalogueitem (codeinstance, codecat, codeitem),
  CONSTRAINT fk_ts_status FOREIGN KEY (codeinstance, statuscat, status)
    REFERENCES tins_catalogueitem (codeinstance, codecat, codeitem),
  CONSTRAINT fk_ts_reviewedby FOREIGN KEY (codeinstance, codecompany, reviewedby)
    REFERENCES tins_user (codeinstance, codecompany, code),
  CONSTRAINT fk_ts_schedule FOREIGN KEY (seqschedule)
    REFERENCES tprj_project_schedule (seqschedule)
);

CREATE TABLE tprj_project_timesheet_his (
  seq_his                bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  actiondml               varchar(30)   NOT NULL,
  datechange               timestamp     NOT NULL DEFAULT now(),
  userchange                bigint        NOT NULL,
  codeinstance              varchar(20)   NOT NULL,
  codecompany               varchar(20)   NOT NULL,
  seqts                     bigint        NOT NULL,
  memberuser                bigint        NOT NULL,
  codecompanyconsultant     varchar(20)   NOT NULL,
  codecustomer               bigint        NOT NULL,
  seqproject                 bigint,
  systemcat                  varchar(50)   NOT NULL,
  system                     varchar(50)   NOT NULL,
  modulecat                  varchar(50)   NOT NULL,
  module                     varchar(50)   NOT NULL,
  sprint                     varchar(100),
  incidentref                varchar(300),
  activitytypecat             varchar(50)   NOT NULL,
  activitytype                varchar(50)   NOT NULL,
  activitydesc                 varchar(500)  NOT NULL,
  tsdate                       date          NOT NULL,
  starttime                    time          NOT NULL,
  endtime                      time          NOT NULL,
  hoursconsumed                numeric       NOT NULL,
  statuscat                    varchar(50)   NOT NULL,
  status                       varchar(50)   NOT NULL,
  reviewedby                   bigint,
  seqschedule                  bigint,
  usercreate                   bigint        NOT NULL,
  userlastmodify                bigint        NOT NULL,
  datecreate                    timestamp     NOT NULL,
  datemodify                     timestamp    NOT NULL
);
