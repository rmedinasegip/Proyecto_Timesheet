# Modelo de datos — referencia rápida

Detalle completo de reglas de negocio en el plan de arquitectura:
`C:\Users\medin\.claude\plans\crear-un-plan-de-purrfect-spindle.md`.

Este archivo documenta el esquema físico real aplicado en Postgres,
fase a fase, según se va implementando (`backend/sql/V*.sql`).

## Contexto fijo del piloto

No hay selector de instancia/compañía en ningún mockup, así que el backend
usa un contexto fijo (`com.llacsaa.timesheet.common.PilotContext`):

- `codeinstance = 'LLACSAA'`
- `codecompany = 'LLA-EC'` (LLACSAA Ecuador — una de las 3 empresas de la
  lista del Excel de registro diario; se eligió como compañía operadora del
  piloto)

## Convención confirmada releyendo los 4 documentos de detalle (Proyecto,
## Schedule, Riesgos, News)

- Todo maestro `tins_*` usa una columna **`code`** genérica como llave de
  negocio propia (no repite su propio nombre, ej. no es
  `tins_company.codecompany` sino `tins_company.code`). Las tablas HIJAS sí
  usan nombres semánticos (`codecompany`, `codecustomer`, `codeuser_pm`, ...)
  para referenciar ese `code`.
- `tins_catalogueitem` se identifica como `(codeinstance, codecat, codeitem)`
  — confirmado literalmente en 3 documentos distintos.
- `tins_company.code` es alfanumérico corto (ej. `'LLA-EC'`); `tins_person` /
  `tins_customer` / `tins_user` usan `code` **numérico** autoincremental
  (confirmado por el ejemplo `usercreate ... Ej: 1005` en los documentos).
- **`tins_customer` no tiene columna propia de nombre**: su nombre se
  resuelve vía `codeperson -> tins_person.name` (confirmado literalmente:
  "Cliente: tins_person.name, donde... tins_person.code =
  tins_customer.codeperson"). `tins_person` actúa como maestro genérico de
  "partes" (reutilizado tanto para consultores como para clientes/empresas).
- `usercreate`/`userlastmodify` se guardan como `bigint` pero **sin FK de
  base de datos** hacia `tins_user`: evita una dependencia circular de
  bootstrap (`tins_company` necesitaría a `tins_user`, que a su vez necesita
  a `tins_company`) y un registro de auditoría no debe quedar bloqueado si el
  usuario que lo creó se elimina después.
- Los scripts DDL/seed son SQL plano aplicado directamente con `psql`
  (`backend/sql/V1__*.sql`, `V2__*.sql`) — sin Flyway ni Docker, según lo ya
  decidido para este proyecto.

## Tablas por fase

### Fase 1 (✅ completa) — Catálogos y maestros JORUPE

`backend/sql/V1__catalogs_and_masters.sql` (DDL) +
`backend/sql/V2__seed_catalogs_and_masters.sql` (datos de prueba).

- `tins_catalogue` (codeinstance, codecat) PK
- `tins_catalogueitem` (codeinstance, codecat, codeitem) PK, FK →
  tins_catalogue
- `tins_company` (codeinstance, code) PK
- `tins_person` (codeinstance, codecompany, code) PK, FK → tins_company
- `tins_customer` (codeinstance, codecompany, code) PK, FK → tins_company,
  FK codeperson → tins_person
- `tins_user` (codeinstance, codecompany, code) PK, FK → tins_company,
  FK codeperson → tins_person

10 catálogos sembrados bajo el namespace `PRJ_`: `PRJ_SYSTEMSCAT` (4 ítems,
confirmado por el Excel), `PRJ_MODULECAT` (19 ítems, confirmado por el
Excel), `PRJ_ACTIVITYTYTYPECAT` (11 ítems, confirmado por el Excel + doc de
Riesgos), `PRJ_TIMESHEETSTATUSCAT` (3 ítems, confirmado por el Excel),
`PRJ_PROJECTROLCAT` (3 ítems, confirmado por el doc de Schedule),
`PRJ_RISKTYPEIMPACTCAT` / `PRJ_NEWTYPEIMPACTCAT` (3 ítems c/u, confirmado por
los docs de Riesgos/News), `PRJ_PROJECTSTATUSCAT` (4 ítems, confirmado por el
doc de Schedule). `PRJ_RISKSTATUSCAT` / `PRJ_NEWSTATUSCAT` (3 ítems c/u) son
**datos de prueba/placeholder** — el campo de estado en sí fue confirmado por
el usuario contra el documento general, pero ningún documento de detalle
enumera sus valores reales; a confirmar con el cliente antes de Fase 4.

Maestros sembrados: 3 `tins_company` (LLACSAA Ecuador/Perú, Externos — lista
exacta del Excel), 13 `tins_person` (10 consultores reales del Excel +
Ripley/Flamingo/Llacsaa como "partes" cliente), 10 `tins_user` (uno por
consultor), 3 `tins_customer` (Ripley/Flamingo/Llacsaa).

Endpoints de verificación (sin pantalla propia — se consumirán como
catálogos/dropdowns desde Fase 2): `GET /api/catalogs`,
`GET /api/catalogs/{codecat}/items`, `GET /api/companies`,
`GET /api/customers`, `GET /api/users`.

### Fase 2 (✅ completa) — Módulo Proyecto

`backend/sql/V3__project_module.sql`. Todas las PK propias (`seq`, `seqteam`,
`seqschedule`, `seqrisk`, `seq_news`) son `bigint` autoincremental — a
diferencia de los maestros `tins_*` de Fase 1, aquí no hace falta llave
compuesta natural.

- `tprj_project` (+`_his`) — 10-pestañas del modal; `statuscat`/`status` →
  `PRJ_PROJECTSTATUSCAT` (PLA/ONG/SL/CLO, confirmado en el doc de Schedule).
- `tprj_proj_team` (+`_his`) — equipo N:M; `projectrol` → `PRJ_PROJECTROLCAT`,
  sin columna `projectrolcat` propia (el documento solo define un campo, no
  el par cat/item habitual — se valida en la aplicación, no en la BD).
- `tprj_project_schedule` (+`_his`) — jerarquía Padre/Hermano/Hijo vía
  `seqscheduleparent` (self-FK). **No existe una columna de "tipo de nodo"
  persistida**: una fila es "Padre" cuando `memberuser` es `null` (deriva de
  qué campos tiene poblados, ver `ScheduleService`/`ScheduleView.padre`) —
  así evita un campo redundante y sigue fielmente la regla del documento
  ("cuando la selección jerárquica sea Padre, solo Descripción/Descripción
  larga son editables"). Eliminar está bloqueado (409) si la actividad tiene
  hijas (`seqscheduleparent` apuntando a ella).
- `tprj_project_risk` (+`_his`) — incluye `riskstatuscat`/`riskstatus`
  (`PRJ_RISKSTATUSCAT`, default `'ABI'` al crear) aunque el documento
  dedicado de Riesgos no las trae: confirmado por el usuario contra el
  documento general, que sí las define (ver plan).
- `tprj_project_news` (+`_his`) — mismo caso con `newstatuscat`/`newstatus`
  (`PRJ_NEWSTATUSCAT`).
- **Auditoría de eliminaciones**: las 5 tablas `_his` usan `actiondml` con
  tres valores — `NEW`/`UPDATE` (documentados) y **`DELETE`** (extensión
  deliberada: se guarda el último estado de la fila justo antes de borrarla,
  para cumplir la regla transversal "toda eliminación... queda auditada" del
  documento general, que no detalla cómo).
- `companyallocate` (tprj_project_schedule) y las columnas de catálogo
  (`statuscat`/`status`, `risktypeimpactcat`/`risktypeimpact`, etc.) sí
  llevan FK compuesta real hacia `tins_company`/`tins_catalogueitem` — a
  diferencia de `usercreate`/`userlastmodify`, que se mantienen sin FK por
  el mismo motivo de bootstrap explicado en Fase 1.
- `tprj_project_schedule.lastseqts` (bigint, sin FK) es una referencia
  adelantada a `tprj_project_timesheet.seqts` (Fase 3, aún no existe).

**Flujo de guardado del modal** (interpretación explícita, ver
`ProjectFormComponent`): las pestañas 4/5/6/7/8/9/10 (Equipo, Consulta,
Avance, Schedule, Riesgos, Novedades, Auditoría) permanecen deshabilitadas
hasta el primer "Guardar Proyecto" — ese primer guardado crea `tprj_project`
(+ su historial) y habilita el resto; cada alta/edición/baja posterior en
esas pestañas se persiste de inmediato (su propia llamada REST + su propio
snapshot `_his`), no se acumula para un guardado conjunto. Los documentos de
detalle de Riesgos/News describen justamente esa persistencia inmediata por
registro; el documento general sugiere (sin sido explícito) que todo se
guarda junto — se prioriza la lectura más precisa de los documentos
dedicados.

### Fase 3 (✅ completa) — Registro de TimeSheets

`backend/sql/V4__timesheet_module.sql`. Sin documento CRUD dedicado (a
diferencia de Fase 2) — reconstruida en Fase 0 a partir de la hoja "Registro
diario" del Excel, aplicando aquí las mismas convenciones ya confirmadas en
Fase 2:

- `tprj_project_timesheet` (+`_his`) — PK `seqts` bigint autoincremental.
  Pares `<campo>cat`/`<campo>` con FK compuesta real hacia
  `tins_catalogueitem` para `systemcat`/`system` (`PRJ_SYSTEMSCAT`),
  `modulecat`/`module` (`PRJ_MODULECAT`), `activitytypecat`/`activitytype`
  (`PRJ_ACTIVITYTYTYPECAT`) y `statuscat`/`status` (`PRJ_TIMESHEETSTATUSCAT`)
  — normalizado desde el borrador de una sola columna del plan original,
  igual que se hizo con `tprj_project_schedule` en Fase 2.
- `seqproject` es `NULL` para incidencias (sin proyecto formal); `sprint`
  solo aplica si hay proyecto, `incidentref` solo si es incidencia — regla
  de negocio, no constraint de BD.
- `hoursconsumed` se calcula siempre en `TimesheetService` a partir de
  `starttime`/`endtime` (nunca se acepta del cliente) — mismo criterio que
  `basedaystotal` en Fase 2, evitando columnas `GENERATED ALWAYS` de
  Postgres para no depender de un refresh extra tras el insert.
- **Flujo de aprobación**: `statuscat`/`status` inicia en `'REG'`
  (Registrado). Un endpoint separado, `PUT /api/timesheets/{seqts}/review`,
  es el único que puede pasarlo a `'APR'`/`'REC'` (Aprobado/Rechazado) y
  setear `reviewedby` — separado del `PUT` de edición normal para reflejar
  que son dos roles distintos (consultor vs. autorizador) aunque, sin
  auth/roles todavía (Fase 5), cualquiera puede llamar ambos endpoints en
  este piloto.
- **Bug real encontrado y corregido durante la verificación**: el patrón
  JPQL `(:param IS NULL OR campo = :param)` para filtros opcionales
  (`GET /api/timesheets?memberuser=&dateFrom=&dateTo=&status=&seqproject=`)
  dispara `ERROR: no se pudo determinar el tipo del parámetro $N` en
  Postgres/JDBC cuando un parámetro solo aparece dentro de comparaciones
  `IS NULL` en la consulta (el driver no puede inferir su tipo). Se
  reemplazó por `Specification`s de Spring Data JPA (`TimesheetSpecifications`),
  que simplemente no agregan el predicado cuando el filtro es `null` —
  evita la clase de problema por completo en vez de parchearlo campo por
  campo.

### Fase 4 (✅ completa) — Avance de Proyectos + reporte

`backend/sql/V5__project_change_module.sql`. Sin documento CRUD dedicado
(igual que Timesheets en Fase 3) — la única tabla nueva es
`tprj_project_change` (+`_his`), "Cambios Aprobados": reconstruida
directamente de las columnas que muestra la hoja "Detalle" del Excel
(Fase, Entregable, Motivo, Consecuencia, Aprobador, Empresa, Variación de
días, Fecha planificada de aplicación), con `aprobador`/`empresa` como
varchar libres — mismo patrón ya establecido por `tprj_project_risk`/
`tprj_project_news` para "responsable"/"compañía" (no FK hacia
`tins_user`/`tins_company`), no una decisión nueva de este módulo.

El resto del reporte **no necesitó tablas nuevas** — se lee/agrega en vivo
sobre datos ya modelados en Fases 1-3, con las siguientes interpretaciones
(documentadas también como comentario de cabecera en
`ProjectProgressReportService`):

- **"Días del proyecto por fase"**: una fila por actividad "Padre" de
  `tprj_project_schedule` (fase). La regla de negocio de Fase 2 deja las
  columnas de días/% de una fila "Padre" siempre en `null` — solo sus
  "Hijo" las tienen editables (ver `ScheduleService`). Por eso cada fila de
  fase usa el valor propio de la fila Padre **si existe**, y si no
  (el caso real en la práctica) lo agrega entre sus actividades "Hijo":
  suma para columnas de días, promedio para columnas de %. Leer directo la
  columna del Padre habría dejado esta sección vacía para cualquier
  proyecto real — se confirmó este comportamiento probando contra datos
  reales antes de cerrar la fase (ver Errors abajo).
- **"Días Utilizados (según T.S.)"** no es una columna almacenada: se
  agrega en vivo sumando `tprj_project_timesheet.hoursconsumed` de los
  registros vinculados a esa fase (su propio `seqschedule` + el de sus
  hijas) y dividiendo entre 8 (jornada estándar asumida, sin documento que
  fije el valor — mismo criterio que otros supuestos sin fuente en Fase 3).
  "Saldo en días (según T.S.)" = Total días proyecto - esa cifra.
- **"% Variación avance proyecto"** tampoco es una columna almacenada: se
  calcula `advrealperc - advexpectedperc`. Es distinta de
  "% Variación asignación planificada", que sí tiene su propia columna
  (`varadvplannedperc`) desde Fase 2.
- **Fila TOTAL**: las columnas de "días" se suman entre fases (incluye
  todo lo comprometido/consumido real); las columnas de "%" se leen
  directo de `tprj_project` (el snapshot de avance a nivel de proyecto,
  ver abajo) — no un promedio de las fases, porque esos campos existen
  para representar el avance global tal como lo evalúa el PM. Si el
  proyecto nunca tuvo un "Registrar avance", esas columnas muestran vacío
  (no 0) para distinguir "sin avance registrado" de "0% de avance".
- **"Hitos"**: actividades "Hijo" de `tprj_project_schedule` (con
  `memberuser`), agrupadas por su fase padre, mismas métricas que las
  fases + su propio "Días T.S.".
- **Riesgos/Novedades/Cambios Aprobados** se filtran por su propia fecha
  `<= fecha de corte` del reporte (query param `cutoffDate`, default hoy)
  — única semántica razonable de "reporte a una fecha de corte" para
  listas que ya tienen fecha propia.

**"Registrar avance"**: a diferencia del resto del modal de Proyecto, el
grupo de campos de avance de `tprj_project` (`lastcutoffdate`,
`advexpectedperc`, `advrealperc`, `advexpecteddays`, `advrealdays`,
`daysconsumed`, `varadvplannedperc`, `efectivityperc`) se edita con su
propio endpoint `PUT /api/projects/{seq}/progress`
(`ProjectService#registerProgress`), no con el `PUT` general de cabecera —
refleja que es una acción de negocio distinta (registrar un corte de
avance), mismo criterio que separó `review()` de `update()` en Timesheets
(Fase 3). La pestaña "Avance" del modal (antes solo lectura) ahora tiene un
botón "Registrar avance" que abre este formulario.

Endpoint del reporte: `GET /api/projects/{seq}/progress-report?cutoffDate=`.
Pantalla Angular en `frontend/src/app/progress/` (`ProgressReportComponent`,
ruta `/progress`), con selector de proyecto + filtro de fecha de corte,
las tablas de fases/hitos/riesgos/novedades y un CRUD en línea de Cambios
Aprobados (mismo patrón que Riesgos/Novedades en el modal de Proyecto).
Verificado end-to-end vía API y navegador contra datos reales de proyecto
(incluida la corrección de agregación fase↔hijas descrita arriba).

### Fase 5 (✅ completa) — Autenticación/roles, pulido responsive, exportación

`backend/sql/V6__auth.sql`. Sin tabla de credenciales nueva — se agregan
`email`/`passwordhash`/`rolecat`/`role` directo a `tins_user`: ya era la
identidad canónica de "usuario del sistema" en todo el modelo (PM,
integrante de equipo, autor de timesheets, autorizador), así que reutilizarla
es más consistente que introducir una segunda tabla paralela solo para login.

- **Roles**: `PRJ_USERROLECAT` (`CON` Consultor / `AUT` Autorizador), mismo
  patrón de catálogo de dos columnas usado en todo el proyecto — el plan
  original mencionaba una "relación AUTHORIZERCAT" sin detallarla; esta es
  la interpretación consistente con el resto del esquema. De los 10 usuarios
  sembrados en Fase 1, solo Ricardo Medina (`code=1`, ya el autorizador
  hardcodeado hasta esta fase) quedó `AUT`; el resto `CON`.
- **Contraseñas**: `Llacsaa2026` para los 10 usuarios de demo — sembrada en
  runtime por `auth/PasswordSeedRunner` (`ApplicationRunner`, idempotente,
  actualiza solo filas con `passwordhash IS NULL`), no en `V6__auth.sql`: un
  hash bcrypt no se puede escribir a mano en SQL plano de forma práctica, así
  que se genera en el primer arranque de la app — mismo criterio que
  `seedAdminUser()` en el proyecto Next.js hermano.
- **Transporte del JWT**: cookie httpOnly `auth_token` (`HttpOnly`,
  `SameSite=Strict`, `Secure=false` en este entorno de desarrollo sobre
  http plano — poner `true` al desplegar con https real), nunca en el body
  de la respuesta de login (`AuthController` devuelve `{code, email, name,
  role, roleName}`). Firmado HS256 (`io.jsonwebtoken`/jjwt), secreto en
  `jwt.secret` (`application.yml`, con el mismo criterio ya aceptado en este
  proyecto de credenciales de desarrollo inline — ver `DATABASE_URL`).
- **Enforcement backend**: `auth/AuthFilter` (`javax.servlet.Filter`,
  registrado solo sobre `/api/*`) verifica la cookie en cada request;
  `/api/auth/login` y `/api/ping` (health check sin datos sensibles, usado
  por el header antes de iniciar sesión) son las únicas rutas públicas. El
  usuario autenticado queda en `auth/AuthContext` (ThreadLocal, poblado por
  el filtro) — reemplaza el `PilotContext.CURRENT_USER_CODE` fijo de las
  Fases 1-4 en los 6 servicios que escriben `usercreate`/`userlastmodify`/
  `userchange` (Project, Schedule, Risk, News, Change, Timesheet).
  `TimesheetService#review` además exige `AuthContext.isAuthorizer()`
  (403 si no) — es la única acción con restricción de rol en este piloto.
- **Enforcement frontend**: `AuthGuard` en las rutas `/projects`,
  `/progress`, `/timesheets` (no en `/login`) — conveniencia de UX, no el
  límite de seguridad real (ese es el filtro del backend, mismo criterio
  que el par proxy.ts/backend del proyecto Next.js hermano).
  `AuthInterceptor` redirige a `/login` ante cualquier 401 fuera de
  `/api/auth/**`, cubriendo el caso de una cookie que expira a mitad de
  sesión. Los botones Aprobar/Rechazar de Timesheets solo se muestran si
  `currentUser.role === 'AUT'` (gate de UX; el 403 del backend es el límite
  real si igual se intentara).
- **Exportación**: `GET /api/projects/{seq}/progress-report/excel` genera
  un `.xlsx` (Apache POI) con las mismas secciones que la vista Angular del
  reporte, en el mismo orden — equivalente a la hoja "Detalle" del Excel
  original del cliente. Botón "Descargar Excel" en `/progress`
  (fetch-blob-and-download, mismo patrón que los downloads de PDF del
  proyecto Next.js hermano).
- **Responsive**: verificado a 375px (mobile) en las 4 pantallas
  principales — sin overflow horizontal en ninguna. La mayor parte ya
  venía cubierta desde Fases 2-4 (`form-grid` con `repeat(auto-fit,
  minmax(...))`, `p-table[responsiveLayout="scroll"]`, wrappers
  `.table-scroll` en el reporte de avance); esta fase solo necesitó
  agregar `flex-wrap` al header (`app.component.html`) para que el nuevo
  bloque de usuario/rol/"Cerrar sesión" no rompiera el layout en pantallas
  angostas.
