-- Fase 2: Módulo Proyecto (tprj_project + Equipo + Schedule + Riesgos +
-- Novedades), con historial snapshot en las 5 tablas principales.
--
-- Fuente: "Pantalla CRUD de Proyecto y Otros.docx", "...Schedule...docx",
-- "...Riesgos...docx", "...News...docx" (releídos en esta sesión).
--
-- Decisiones/normalizaciones aplicadas (documentadas para no repetir la
-- discusión en Fase 3/4):
--   - Todas las PK propias (seq, seqteam, seqschedule, seqrisk, seq_news)
--     son bigint autoincremental — a diferencia de los maestros tins_* de
--     Fase 1, aquí sí se puede usar @GeneratedValue/IDENTITY simple en JPA.
--   - FKs compuestas (codeinstance, codecompany, code) hacia tins_customer/
--     tins_user SÍ se declaran (no hay problema de bootstrap circular acá,
--     a diferencia de usercreate/userlastmodify que siguen sin FK por el
--     mismo motivo que en Fase 1).
--   - Pares "<campo>cat"/"<campo>" (statuscat/status, risktypeimpactcat/
--     risktypeimpact, etc.) SÍ llevan FK compuesta hacia
--     tins_catalogueitem(codeinstance, codecat, codeitem). El único campo
--     de catálogo que aparece en el doc como columna única (tprj_proj_team.
--     projectrol, sin "projectrolcat" propio) se deja sin FK de BD — se
--     valida en la aplicación contra PRJ_PROJECTROLCAT.
--   - riskstatuscat/riskstatus y newstatuscat/newstatus se agregan aunque
--     los documentos de detalle de Riesgos/News no las traen: el usuario
--     confirmó explícitamente que el documento general sí las define y
--     prevalece (ver plan). Catálogos PRJ_RISKSTATUSCAT/PRJ_NEWSTATUSCAT ya
--     sembrados en Fase 1 (placeholder, a confirmar con cliente).
--   - lastcutoffdate se implementa como DATE (no Number): el propio
--     documento la describe como "fecha de ultimo corte", el tipo "Number"
--     de la tabla es inconsistente con su propia descripción.
--   - "solution" (riesgos/novedades) se deja NULLABLE aunque el documento no
--     lo marca "(null)": un riesgo/novedad recién creado normalmente aún no
--     tiene solución definida; forzar NOT NULL bloquearía el flujo de alta.
--   - tprj_project.actiondml (mencionado en el documento pegado justo
--     después de datemodify) se interpreta como una descripción fuera de
--     lugar del patrón "_his" (NEW/UPDATE), no como columna de la tabla
--     viva — se implementa únicamente en las tablas _his, consistente con
--     la convención ya usada en Fase 1/plan general.
--   - trpj_project_schedule.lastseqts (FK a una tabla "trpj_project_schedulets"
--     que no existe en ningún documento) se deja como bigint sin FK,
--     referencia adelantada a Fase 3 (tprj_project_timesheet.seqts).
--   - tprj_project_schedule.system/systemcat usa el catálogo PRJ_SYSTEMSCAT
--     ya sembrado en Fase 1 (con los valores reales del Excel de registro
--     diario) — el "Ej:" de este documento trae códigos distintos
--     (TRA/ORI/SE/SC/INF) que se interpretan como ilustrativos, no como una
--     redefinición del catálogo real ya confirmado operacionalmente.

-- ============================================================
-- tprj_project
-- ============================================================

CREATE TABLE tprj_project (
  codeinstance         varchar(20)     NOT NULL,
  codecompany          varchar(20)     NOT NULL,
  seq                  bigint GENERATED ALWAYS AS IDENTITY,
  contract_number      varchar(20),
  project_code         varchar(20)     NOT NULL,
  project_name         varchar(100)    NOT NULL,
  project_description  varchar(500)    NOT NULL,
  project_duration     numeric(10,2)   NOT NULL,
  codecustomer         bigint          NOT NULL,
  codeuser_pm          bigint          NOT NULL,
  request_date         date            NOT NULL,
  base_start_date      date            NOT NULL,
  base_end_date        date            NOT NULL,
  planned_start_date   date            NOT NULL,
  planned_end_date     date            NOT NULL,
  real_start_date      date,
  real_end_date        date,
  statuscat            varchar(50)     NOT NULL,
  status                varchar(50)    NOT NULL,
  lastcutoffdate        date,
  advexpectedperc       numeric         DEFAULT 0,
  advrealperc           numeric         DEFAULT 0,
  advexpecteddays        numeric        DEFAULT 0,
  advrealdays            numeric        DEFAULT 0,
  daysconsumed            numeric       DEFAULT 0,
  varadvplannedperc      numeric        DEFAULT 0,
  efectivityperc          numeric       DEFAULT 0,
  usercreate            bigint          NOT NULL,
  userlastmodify        bigint          NOT NULL,
  datecreate            timestamp       NOT NULL DEFAULT now(),
  datemodify            timestamp       NOT NULL DEFAULT now(),
  CONSTRAINT pk_tprj_project PRIMARY KEY (seq),
  CONSTRAINT fk_project_company FOREIGN KEY (codeinstance, codecompany)
    REFERENCES tins_company (codeinstance, code),
  CONSTRAINT fk_project_customer FOREIGN KEY (codeinstance, codecompany, codecustomer)
    REFERENCES tins_customer (codeinstance, codecompany, code),
  CONSTRAINT fk_project_pm FOREIGN KEY (codeinstance, codecompany, codeuser_pm)
    REFERENCES tins_user (codeinstance, codecompany, code),
  CONSTRAINT fk_project_status FOREIGN KEY (codeinstance, statuscat, status)
    REFERENCES tins_catalogueitem (codeinstance, codecat, codeitem)
);

CREATE TABLE tprj_project_his (
  seq_his              bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  actiondml            varchar(30)     NOT NULL,
  datechange           timestamp       NOT NULL DEFAULT now(),
  userchange           bigint          NOT NULL,
  codeinstance         varchar(20)     NOT NULL,
  codecompany          varchar(20)     NOT NULL,
  seq                  bigint          NOT NULL,
  contract_number      varchar(20),
  project_code         varchar(20)     NOT NULL,
  project_name         varchar(100)    NOT NULL,
  project_description  varchar(500)    NOT NULL,
  project_duration     numeric(10,2)   NOT NULL,
  codecustomer         bigint          NOT NULL,
  codeuser_pm          bigint          NOT NULL,
  request_date         date            NOT NULL,
  base_start_date      date            NOT NULL,
  base_end_date        date            NOT NULL,
  planned_start_date   date            NOT NULL,
  planned_end_date     date            NOT NULL,
  real_start_date      date,
  real_end_date        date,
  statuscat            varchar(50)     NOT NULL,
  status                varchar(50)    NOT NULL,
  lastcutoffdate        date,
  advexpectedperc       numeric,
  advrealperc           numeric,
  advexpecteddays        numeric,
  advrealdays            numeric,
  daysconsumed            numeric,
  varadvplannedperc      numeric,
  efectivityperc          numeric,
  usercreate            bigint          NOT NULL,
  userlastmodify        bigint          NOT NULL,
  datecreate            timestamp       NOT NULL,
  datemodify            timestamp       NOT NULL
);

-- ============================================================
-- tprj_proj_team
-- ============================================================

CREATE TABLE tprj_proj_team (
  codeinstance   varchar(20)  NOT NULL,
  codecompany    varchar(20)  NOT NULL,
  seqproject     bigint       NOT NULL,
  seqteam        bigint GENERATED ALWAYS AS IDENTITY,
  memberuser     bigint       NOT NULL,
  projectrol     varchar(50)  NOT NULL,
  assignmentdate date         NOT NULL,
  usercreate     bigint       NOT NULL,
  userlastmodify bigint       NOT NULL,
  datecreate     timestamp    NOT NULL DEFAULT now(),
  datemodify     timestamp    NOT NULL DEFAULT now(),
  CONSTRAINT pk_tprj_proj_team PRIMARY KEY (seqteam),
  CONSTRAINT fk_team_company FOREIGN KEY (codeinstance, codecompany)
    REFERENCES tins_company (codeinstance, code),
  CONSTRAINT fk_team_project FOREIGN KEY (seqproject)
    REFERENCES tprj_project (seq),
  CONSTRAINT fk_team_member FOREIGN KEY (codeinstance, codecompany, memberuser)
    REFERENCES tins_user (codeinstance, codecompany, code)
);

CREATE TABLE tprj_proj_team_his (
  seq_his        bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  actiondml      varchar(30)  NOT NULL,
  datechange     timestamp    NOT NULL DEFAULT now(),
  userchange     bigint       NOT NULL,
  codeinstance   varchar(20)  NOT NULL,
  codecompany    varchar(20)  NOT NULL,
  seqproject     bigint       NOT NULL,
  seqteam        bigint       NOT NULL,
  memberuser     bigint       NOT NULL,
  projectrol     varchar(50)  NOT NULL,
  assignmentdate date         NOT NULL,
  usercreate     bigint       NOT NULL,
  userlastmodify bigint       NOT NULL,
  datecreate     timestamp    NOT NULL,
  datemodify     timestamp    NOT NULL
);

-- ============================================================
-- tprj_project_schedule
-- ============================================================

CREATE TABLE tprj_project_schedule (
  codeinstance          varchar(20)   NOT NULL,
  codecompany           varchar(20)   NOT NULL,
  companyallocate       varchar(20),
  seqproject            bigint        NOT NULL,
  seqschedule           bigint GENERATED ALWAYS AS IDENTITY,
  seqscheduleparent     bigint,
  lastseqts             bigint,
  systemcat             varchar(50),
  system                varchar(50),
  memberuser            bigint,
  sprint                varchar(100),
  servicecode           varchar(100),
  activitytypecat       varchar(50),
  activitytype          varchar(50),
  shortactivitydesc     varchar(100)  NOT NULL,
  activitydesc          varchar(500)  NOT NULL,
  advexpecteddays       numeric       DEFAULT 0,
  advrealdays           numeric       DEFAULT 0,
  advexpectedperc       numeric       DEFAULT 0,
  advrealperc           numeric       DEFAULT 0,
  advrealnoconfirmperc  numeric       DEFAULT 0,
  basedays              numeric       DEFAULT 0,
  baseadicional         numeric       DEFAULT 0,
  basedaystotal         numeric       DEFAULT 0,
  planneddays           numeric       DEFAULT 0,
  realdays              numeric       DEFAULT 0,
  varadvplannedperc     numeric       DEFAULT 0,
  efectivityperc        numeric       DEFAULT 0,
  base_start_date       date,
  base_end_date         date,
  planned_start_date    date,
  planned_end_date      date,
  real_start_date       date,
  real_end_date         date,
  daysconsumedts        numeric       DEFAULT 0,
  daysconsumedaut       numeric       DEFAULT 0,
  datestamentday        date,
  usercreate            bigint        NOT NULL,
  userlastmodify        bigint        NOT NULL,
  datecreate            timestamp     NOT NULL DEFAULT now(),
  datemodify             timestamp    NOT NULL DEFAULT now(),
  CONSTRAINT pk_tprj_project_schedule PRIMARY KEY (seqschedule),
  CONSTRAINT fk_schedule_company FOREIGN KEY (codeinstance, codecompany)
    REFERENCES tins_company (codeinstance, code),
  CONSTRAINT fk_schedule_companyallocate FOREIGN KEY (codeinstance, companyallocate)
    REFERENCES tins_company (codeinstance, code),
  CONSTRAINT fk_schedule_project FOREIGN KEY (seqproject)
    REFERENCES tprj_project (seq),
  CONSTRAINT fk_schedule_parent FOREIGN KEY (seqscheduleparent)
    REFERENCES tprj_project_schedule (seqschedule),
  CONSTRAINT fk_schedule_member FOREIGN KEY (codeinstance, codecompany, memberuser)
    REFERENCES tins_user (codeinstance, codecompany, code),
  CONSTRAINT fk_schedule_system FOREIGN KEY (codeinstance, systemcat, system)
    REFERENCES tins_catalogueitem (codeinstance, codecat, codeitem),
  CONSTRAINT fk_schedule_activitytype FOREIGN KEY (codeinstance, activitytypecat, activitytype)
    REFERENCES tins_catalogueitem (codeinstance, codecat, codeitem)
);

CREATE TABLE tprj_project_schedule_his (
  seq_his               bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  actiondml             varchar(30)   NOT NULL,
  datechange            timestamp     NOT NULL DEFAULT now(),
  userchange            bigint        NOT NULL,
  codeinstance          varchar(20)   NOT NULL,
  codecompany           varchar(20)   NOT NULL,
  companyallocate       varchar(20),
  seqproject            bigint        NOT NULL,
  seqschedule           bigint        NOT NULL,
  seqscheduleparent     bigint,
  lastseqts             bigint,
  systemcat             varchar(50),
  system                varchar(50),
  memberuser            bigint,
  sprint                varchar(100),
  servicecode           varchar(100),
  activitytypecat       varchar(50),
  activitytype          varchar(50),
  shortactivitydesc     varchar(100)  NOT NULL,
  activitydesc          varchar(500)  NOT NULL,
  advexpecteddays       numeric,
  advrealdays           numeric,
  advexpectedperc       numeric,
  advrealperc           numeric,
  advrealnoconfirmperc  numeric,
  basedays              numeric,
  baseadicional         numeric,
  basedaystotal         numeric,
  planneddays           numeric,
  realdays              numeric,
  varadvplannedperc     numeric,
  efectivityperc        numeric,
  base_start_date       date,
  base_end_date         date,
  planned_start_date    date,
  planned_end_date      date,
  real_start_date       date,
  real_end_date         date,
  daysconsumedts        numeric,
  daysconsumedaut       numeric,
  datestamentday        date,
  usercreate            bigint        NOT NULL,
  userlastmodify        bigint        NOT NULL,
  datecreate            timestamp     NOT NULL,
  datemodify             timestamp    NOT NULL
);

-- ============================================================
-- tprj_project_risk (incluye riskstatuscat/riskstatus por indicación
-- explícita del usuario sobre el documento general, ver nota superior)
-- ============================================================

CREATE TABLE tprj_project_risk (
  codeinstance      varchar(20)   NOT NULL,
  codecompany       varchar(20)   NOT NULL,
  seqproject        bigint        NOT NULL,
  seqrisk           bigint GENERATED ALWAYS AS IDENTITY,
  riskdate          date          NOT NULL,
  risktypeimpactcat varchar(50)   NOT NULL,
  risktypeimpact    varchar(50)   NOT NULL,
  riskdescimpact    varchar(1000) NOT NULL,
  personincharge    varchar(100)  NOT NULL,
  company           varchar(100)  NOT NULL,
  solution          varchar(300),
  probabilityperc   numeric       NOT NULL,
  riskstatuscat     varchar(50)   NOT NULL,
  riskstatus        varchar(50)   NOT NULL,
  usercreate        bigint        NOT NULL,
  userlastmodify    bigint        NOT NULL,
  datecreate        timestamp     NOT NULL DEFAULT now(),
  datemodify        timestamp     NOT NULL DEFAULT now(),
  CONSTRAINT pk_tprj_project_risk PRIMARY KEY (seqrisk),
  CONSTRAINT fk_risk_company FOREIGN KEY (codeinstance, codecompany)
    REFERENCES tins_company (codeinstance, code),
  CONSTRAINT fk_risk_project FOREIGN KEY (seqproject)
    REFERENCES tprj_project (seq),
  CONSTRAINT fk_risk_typeimpact FOREIGN KEY (codeinstance, risktypeimpactcat, risktypeimpact)
    REFERENCES tins_catalogueitem (codeinstance, codecat, codeitem),
  CONSTRAINT fk_risk_status FOREIGN KEY (codeinstance, riskstatuscat, riskstatus)
    REFERENCES tins_catalogueitem (codeinstance, codecat, codeitem)
);

CREATE TABLE tprj_project_risk_his (
  seq_his           bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  actiondml         varchar(30)   NOT NULL,
  datechange        timestamp     NOT NULL DEFAULT now(),
  userchange        bigint        NOT NULL,
  codeinstance      varchar(20)   NOT NULL,
  codecompany       varchar(20)   NOT NULL,
  seqproject        bigint        NOT NULL,
  seqrisk           bigint        NOT NULL,
  riskdate          date          NOT NULL,
  risktypeimpactcat varchar(50)   NOT NULL,
  risktypeimpact    varchar(50)   NOT NULL,
  riskdescimpact    varchar(1000) NOT NULL,
  personincharge    varchar(100)  NOT NULL,
  company           varchar(100)  NOT NULL,
  solution          varchar(300),
  probabilityperc   numeric       NOT NULL,
  riskstatuscat     varchar(50)   NOT NULL,
  riskstatus        varchar(50)   NOT NULL,
  usercreate        bigint        NOT NULL,
  userlastmodify    bigint        NOT NULL,
  datecreate        timestamp     NOT NULL,
  datemodify        timestamp     NOT NULL
);

-- ============================================================
-- tprj_project_news (incluye newstatuscat/newstatus, misma razón que riesgos)
-- ============================================================

CREATE TABLE tprj_project_news (
  codeinstance      varchar(20)  NOT NULL,
  codecompany       varchar(20)  NOT NULL,
  seqproject        bigint       NOT NULL,
  seq_news          bigint GENERATED ALWAYS AS IDENTITY,
  datenewarrival    date         NOT NULL,
  newstypeimpactcat varchar(50)  NOT NULL,
  newstypeimpact    varchar(50)  NOT NULL,
  descriptionnews   varchar(300) NOT NULL,
  personreporting   varchar(100) NOT NULL,
  affectation       varchar(100) NOT NULL,
  personincharge    varchar(100) NOT NULL,
  company           varchar(100) NOT NULL,
  solution          varchar(300),
  datesolution      date,
  daterealsolution  date,
  newstatuscat      varchar(50)  NOT NULL,
  newstatus         varchar(50)  NOT NULL,
  usercreate        bigint       NOT NULL,
  userlastmodify    bigint       NOT NULL,
  datecreate        timestamp    NOT NULL DEFAULT now(),
  datemodify        timestamp    NOT NULL DEFAULT now(),
  CONSTRAINT pk_tprj_project_news PRIMARY KEY (seq_news),
  CONSTRAINT fk_news_company FOREIGN KEY (codeinstance, codecompany)
    REFERENCES tins_company (codeinstance, code),
  CONSTRAINT fk_news_project FOREIGN KEY (seqproject)
    REFERENCES tprj_project (seq),
  CONSTRAINT fk_news_typeimpact FOREIGN KEY (codeinstance, newstypeimpactcat, newstypeimpact)
    REFERENCES tins_catalogueitem (codeinstance, codecat, codeitem),
  CONSTRAINT fk_news_status FOREIGN KEY (codeinstance, newstatuscat, newstatus)
    REFERENCES tins_catalogueitem (codeinstance, codecat, codeitem)
);

CREATE TABLE tprj_project_news_his (
  seq_his           bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  actiondml         varchar(30)  NOT NULL,
  datechange        timestamp    NOT NULL DEFAULT now(),
  userchange        bigint       NOT NULL,
  codeinstance      varchar(20)  NOT NULL,
  codecompany       varchar(20)  NOT NULL,
  seqproject        bigint       NOT NULL,
  seq_news          bigint       NOT NULL,
  datenewarrival    date         NOT NULL,
  newstypeimpactcat varchar(50)  NOT NULL,
  newstypeimpact    varchar(50)  NOT NULL,
  descriptionnews   varchar(300) NOT NULL,
  personreporting   varchar(100) NOT NULL,
  affectation       varchar(100) NOT NULL,
  personincharge    varchar(100) NOT NULL,
  company           varchar(100) NOT NULL,
  solution          varchar(300),
  datesolution      date,
  daterealsolution  date,
  newstatuscat      varchar(50)  NOT NULL,
  newstatus         varchar(50)  NOT NULL,
  usercreate        bigint       NOT NULL,
  userlastmodify    bigint       NOT NULL,
  datecreate        timestamp    NOT NULL,
  datemodify        timestamp    NOT NULL
);
