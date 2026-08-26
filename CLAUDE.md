# CLAUDE.md

Guía para Claude Code al trabajar en este repositorio. Mismo contenido que
`AGENTS.md` — se mantienen ambos archivos sincronizados porque distintas
herramientas leen uno u otro por convención. El plan de arquitectura completo
y su historial de decisiones vive en
`C:\Users\medin\.claude\plans\crear-un-plan-de-purrfect-spindle.md` — este
archivo resume lo operativo para el día a día.

## Qué es este proyecto

"Gestión y Control de Proyectos - TimeSheets": sistema web para registrar,
controlar y hacer seguimiento de timesheets de consultores por
proyecto/actividad, con equipo de proyecto, cronograma de avance (Schedule),
riesgos, novedades y reportes equivalentes a la plantilla Excel
`AD-RE-04_TIME_SHEET_2026.xlsx`.

## Stack (fijado por especificación del cliente, no negociable)

| Capa | Tecnología |
|---|---|
| Backend | Java 11 + Spring Boot 2.1.18.RELEASE |
| Frontend | Angular 12.2.2 + PrimeNG 12.1.1 |
| Base de datos | PostgreSQL 18, local, conexión directa `postgresql://postgres:postgres@localhost:5432/postgres` (sin Docker) |
| ORM | Spring Data JPA / Hibernate |
| Build | Maven (via Maven Wrapper, `backend/mvnw.cmd`) |

**Nota de plantilla PrimeNG**: la especificación pide el template pago
"Ultima" de PrimeNG. No se cuenta con ese template en este scaffolding — el
frontend usa el tema gratuito `saga-blue` de PrimeNG como placeholder
(`frontend/angular.json` → `styles`). Si el cliente tiene licencia de
"Ultima", reemplazar ese theme.css por los archivos del template.

**Nota Node/Angular 12**: Angular 12 usa Webpack 4, incompatible con OpenSSL 3
de Node ≥17 (error `ERR_OSSL_EVP_UNSUPPORTED`). Todos los scripts de
`frontend/package.json` (`start`, `build`, `watch`, `test`) ya usan
`cross-env NODE_OPTIONS=--openssl-legacy-provider` — usar siempre `npm run
<script>`, no invocar `ng` directamente sin ese flag.

## Comandos

```bash
cd backend && ./mvnw.cmd spring-boot:run   # backend, puerto 8080
cd frontend && npm start                    # frontend, puerto 4200 (proxy /api -> :8080)
cd backend && ./mvnw.cmd test               # tests backend
cd frontend && npm test                     # tests frontend
```

**Esquema de base de datos**: no se usa Flyway ni `ddl-auto` (queda en
`none`). El esquema se versiona como SQL plano en `backend/sql/V*.sql` y se
aplica manualmente contra el Postgres 18 local con `psql`
(`D:\PostgreSQL\18\bin\psql.exe` en esta máquina — no está en el `PATH`):

```bash
"/d/PostgreSQL/18/bin/psql.exe" -h localhost -U postgres -d postgres -v ON_ERROR_STOP=1 -f backend/sql/V1__catalogs_and_masters.sql
```

`.claude/launch.json` tiene las dos entradas de dev server (`backend`,
`frontend`) para el Browser pane.

`psql` necesita `PGPASSWORD=postgres` en el entorno (o `.pgpass`) — sin eso
queda colgado esperando la contraseña por stdin en vez de fallar.

## Estructura del monorepo

```
backend/src/main/java/com/llacsaa/timesheet/
  catalog/        TINS_CATALOGUE, TINS_CATALOGUEITEM
  master/         tins_company, tins_customer, tins_user, tins_person (maestros JORUPE)
  project/        tprj_project, tprj_proj_team + historial (snapshot)
  schedule/       tprj_project_schedule (actividades/fases) + historial
  timesheet/      tprj_project_timesheet (registro diario de horas)
  risk/           tprj_project_risk + historial
  news/           tprj_project_news + historial
  change/         tprj_project_change ("Cambios Aprobados") + historial
  report/         ProjectProgressReportService — reporte de avance (agregación en vivo, sin tabla propia) + export a Excel
  auth/           login JWT (cookie httpOnly), roles CON/AUT, AuthContext (usuario actual por request)
  weeklyprogress/ avance semanal de Schedules por consultor (tprj_project_schedulets -> tprj_projectts_week -> tprj_projectts_week_detail)
  common/         utilidades transversales (PilotContext: codeinstance/codecompany fijos)

frontend/src/app/
  projects/        módulo Proyecto: grilla + modal de 10 pestañas
  progress/        avance de proyecto + reporte (ProgressReportComponent, ruta /progress)
  timesheet/       registro diario del consultor
  weekly-progress/ avance semanal de Schedules por consultor (ruta /weekly-progress)
  auth/            login (LoginComponent, ruta /login), AuthService, AuthGuard, AuthInterceptor
```

No hay `packages/shared` como en el proyecto vecino Next.js — este stack no
lo necesita; los tipos/DTOs se definen directamente en cada lado (Java records
o clases en `backend`, interfaces TypeScript en `frontend`).

## Convenciones del modelo de datos (ver plan para detalle completo)

- Prefijo `tprj_` para todas las tablas de negocio (normalizado; la
  especificación alternaba `tprj_`/`trpj_`).
- Toda tabla de negocio lleva `codeinstance` + `codecompany` (contexto
  multi-instancia/multi-compañía) y auditoría estándar (`usercreate`,
  `userlastmodify`, `datecreate`, `datemodify`).
- **Histórico = snapshot de fila completa**, no log a nivel de campo: cada
  `INSERT`/`UPDATE` en `tprj_project`, `tprj_proj_team`,
  `tprj_project_schedule`, `tprj_project_risk` o `tprj_project_news` escribe
  una copia íntegra en su tabla `_his` correspondiente
  (`seq_his` PK + `actiondml` `NEW`/`UPDATE` + `datechange`/`userchange`).
- **Riesgos y Novedades son pestañas del modal de Proyecto** (no pantallas
  independientes), y sí llevan campo de estado (`riskstatuscat`/`riskstatus`,
  `newstatuscat`/`newstatus`) — confirmado por el usuario sobre el documento
  general, pese a que los documentos de detalle por módulo decían lo
  contrario.
- `codeinstance`/`codecompany` se resuelven desde configuración fija en este
  piloto (no hay selector de instancia en ningún mockup).
- Filtros opcionales en endpoints de búsqueda: usar JPA Specifications (cada
  filtro devuelve `null` si el parámetro no vino), nunca JPQL con
  `(:param IS NULL OR ...)` — ese patrón rompe con Postgres/JDBC cuando el
  parámetro solo aparece dentro de una comparación `IS NULL`.
- Campos de entidad con un dígito seguido de mayúscula (ej. `day1Perc`) NO
  quedan mapeados por la estrategia de nombres por defecto de Hibernate a la
  columna con guión bajo esperada (`day1_perc` → generaría `day1perc`) —
  usar `@Column(name = "...")` explícito en esos casos.
- Cuidado con el operador ternario de Java mezclando `Long`/`long`: si una
  rama es un `Long` que puede ser `null` y la otra es un `long` primitivo
  (p. ej. el resultado de `AuthContext.currentUserCode()`), Java desempaqueta
  la rama `Long` para unificar tipos y lanza NPE si es `null` — usar
  `if`/`else` en vez de ternario en esos casos.

## Módulos funcionales (ver plan para reglas de negocio detalladas)

1. **Módulo Proyecto** — grilla + modal de 10 pestañas: Datos Generales,
   Planificación, Ejecución Real, Equipo de Trabajo, Consulta, Avance,
   Schedule Proyecto (edición en línea, jerarquía Padre/Hermano/Hijo),
   Riesgos, Novedades, Auditoría. La grilla tiene 3 acciones por fila: Ver
   (lupa, `ProjectFormComponent [readOnly]="true"` — mismo modal de 10
   pestañas en modo solo lectura, sin crear/editar/eliminar), Editar y
   Eliminar.
2. **Avance de Proyectos** — reporte agregado equivalente a la hoja "Detalle"
   del Excel (Días del proyecto, Riesgos, Novedades, Hitos, Cambios
   Aprobados).
3. **Registro de TimeSheets** — pantalla de registro diario del consultor
   (`tprj_project_timesheet`), reconstruida a partir de la hoja "Registro
   diario" del Excel — no tenía mockup ni tabla definida en la especificación
   original.
4. **Avance semanal de Schedules por consultor** — cada consultor registra,
   semana a semana, el % de avance de sus actividades ("Hijo") del Schedule,
   en la misma grilla que las lista (edición en línea, sin diálogo aparte).
   Alimenta `tprj_project_schedule.advrealperc`/`advrealdays`/
   `datestamentday`. Flujo de aprobación Registrado→Aprobado/Rechazado igual
   que Timesheets; acceso autoservicio estricto (un consultor solo ve/edita
   lo propio, forzado en el servidor).

## Reglas de UI/UX transversales

- Diseño responsive/mobile-first, sin resolución fija.
- Títulos de campo: primera letra mayúscula, resto minúscula, negrilla.
- Toda eliminación pide confirmación y queda auditada.

## Estado del roadmap

- ✅ **Fase 0** (scaffolding) — completa: monorepo, backend Spring Boot
  conectado a Postgres real, frontend Angular+PrimeNG con proxy al backend,
  ambos verificados end-to-end.
- ✅ **Fase 1** (catálogos y maestros JORUPE) — completa: DDL + seed en
  `backend/sql/V1__*.sql`/`V2__*.sql` (aplicados con `psql`, ver
  `docs/erd.md` para el detalle del esquema y las convenciones confirmadas
  releyendo los 4 documentos de detalle), entidades JPA + repositorios en
  `catalog/`/`master/`, endpoints de verificación (`/api/catalogs`,
  `/api/catalogs/{codecat}/items`, `/api/companies`, `/api/customers`,
  `/api/users`) probados contra datos reales.
- ✅ **Fase 2** (módulo Proyecto) — completa: `tprj_project`/`tprj_proj_team`/
  `tprj_project_schedule`/`tprj_project_risk`/`tprj_project_news` + sus 5
  tablas `_his` (`backend/sql/V3__project_module.sql`), servicios con
  historización NEW/UPDATE/DELETE, jerarquía Padre/Hermano/Hijo con bloqueo
  de borrado si hay hijas, grilla + modal de 10 pestañas en Angular
  (`frontend/src/app/projects/`). Verificado end-to-end vía API y navegador
  (ver `docs/erd.md` para las decisiones de diseño). Nota: se agregó
  `@angular/cdk` como dependencia (peer dependency de PrimeNG table/dropdown,
  no estaba instalada en el scaffolding de Fase 0).
- ✅ **Fase 3** (Registro de TimeSheets) — completa: `tprj_project_timesheet`
  + `_his` (`backend/sql/V4__timesheet_module.sql`), servicio con cálculo de
  horas en servidor, flujo de aprobación Registrado→Aprobado/Rechazado vía
  endpoint dedicado, filtros por consultor/fecha/proyecto/status usando
  Specifications (no JPQL con `:param IS NULL OR...` — ver `docs/erd.md`,
  ese patrón rompe con Postgres/JDBC cuando el filtro es nulo), pantalla
  Angular en `frontend/src/app/timesheet/`. Verificado end-to-end vía API y
  navegador.
- ✅ **Fase 4** (Avance de Proyectos + reporte) — completa: única tabla
  nueva `tprj_project_change` + `_his` ("Cambios Aprobados",
  `backend/sql/V5__project_change_module.sql`); el resto del reporte
  (Días del proyecto por fase, Hitos) se agrega en vivo sobre datos ya
  modelados en Fases 1-3 (`report/ProjectProgressReportService`), incl.
  "Días Utilizados (según T.S.)" calculado desde
  `tprj_project_timesheet.hoursconsumed` y una fila TOTAL con los
  porcentajes tomados directo de `tprj_project`. Nueva acción
  "Registrar avance" (`PUT /api/projects/{seq}/progress`) separada del PUT
  general de cabecera. Pantalla Angular en `frontend/src/app/progress/`
  (ruta `/progress`) con selector de proyecto, filtro de fecha de corte y
  CRUD en línea de Cambios Aprobados. Ver `docs/erd.md` para el detalle de
  las interpretaciones de columnas del reporte (documentado también como
  comentario de cabecera en `ProjectProgressReportService`). Verificado
  end-to-end vía API y navegador.
- ✅ **Fase 5** (autenticación/roles, pulido responsive, exportación) —
  completa, cierra el roadmap: login JWT (`backend/sql/V6__auth.sql` agrega
  `email`/`passwordhash`/`rolecat`/`role` a `tins_user` — no se creó tabla
  de credenciales aparte, `tins_user` ya era la identidad canónica de
  usuario en todo el modelo), token viaja como cookie httpOnly
  `auth_token` (nunca en el body de la respuesta), filtro propio
  (`auth/AuthFilter`) protege todo `/api/**` salvo `/api/auth/login` y
  `/api/ping`. Roles `PRJ_USERROLECAT` (CON/AUT, mismo patrón de catálogo
  de dos columnas que el resto del esquema) — `PUT /api/timesheets/{id}/review`
  ahora exige rol AUT (403 si no). Contraseña de demo de los 10 usuarios
  sembrados en Fase 1: `Llacsaa2026` (sembrada en runtime por
  `auth/PasswordSeedRunner`, idempotente — un hash bcrypt no se puede
  escribir a mano en SQL plano). Frontend: `LoginComponent` + `AuthGuard`
  en las rutas `/projects`/`/progress`/`/timesheets` + `AuthInterceptor`
  (redirige a `/login` en cualquier 401), header muestra
  usuario/rol/"Cerrar sesión", botones Aprobar/Rechazar de Timesheets
  ocultos para rol CON. Exportación del reporte de avance a Excel (.xlsx,
  Apache POI) vía `GET /api/projects/{seq}/progress-report/excel`, botón
  "Descargar Excel" en `/progress`. Pulido responsive: verificado a 375px
  sin overflow horizontal en ninguna pantalla — ya venía cubierto desde
  Fases 2-4 (`form-grid` auto-fit, `p-table[responsiveLayout=scroll]`,
  wrappers `.table-scroll`), solo se ajustó el header (nav con
  `flex-wrap`) para el nuevo bloque de usuario/logout. Verificado
  end-to-end vía curl (401/403/200 en cada combinación de rol) y
  navegador (login, guard, logout, persistencia de sesión tras recargar,
  descarga de Excel).

Con Fase 5 completa, el roadmap del plan original queda cerrado. Fases
posteriores (6+) son módulos nuevos pedidos fuera de ese roadmap original.

- ✅ **Fase 6** (avance semanal de Schedules por consultor) — módulo nuevo,
  fuera del roadmap original, a partir de
  `Especificación de Proyecto_Control_Proyectos_TimeSheets_2.docx`: cada
  consultor registra semana a semana el % de avance de sus actividades
  ("Hijo") del Schedule. Nuevas tablas `tprj_project_schedulets`,
  `tprj_projectts_week` (con flujo Registrado→Aprobado/Rechazado, catálogo
  `PRJ_TSWEEKSTATUSCAT`) y `tprj_projectts_week_detail` (día1-7, snapshot
  acumulado por día) + sus 3 `_his`
  (`backend/sql/V7__schedule_weekly_progress.sql`), paquete backend
  `weeklyprogress/` (`WeeklyProgressService`/`Controller`, endpoints
  `/api/schedule-progress/*`). Al guardar una semana, el último día con
  valor se propaga como nuevo `tprj_project_schedule.advrealperc`/
  `advrealdays`/`datestamentday`, historizado con el mismo patrón de
  `ScheduleService` — campos que existían desde la Fase 2 pero que ningún
  módulo alimentaba todavía. Acceso autoservicio estricto: un `CON` solo
  ve/edita sus propias actividades (forzado en el servidor, no solo en el
  filtro del cliente — confirmado con pruebas de API directas); un `AUT`
  ve todos los consultores y aprueba/rechaza semanas, pero no puede
  escribir porcentajes por otro (403). Pantalla Angular en
  `frontend/src/app/weekly-progress/` (ruta `/weekly-progress`), grilla con
  edición en línea (mismo patrón de la pestaña "Schedule Proyecto": toggle
  lápiz→inputs→check/X en la misma fila, sin diálogo aparte). Verificado
  end-to-end vía API real (curl/fetch) y navegador: registro semanal como
  `CON`, avance reflejado en `tprj_project_schedule` y en la pestaña Schedule
  del modal de Proyecto, aprobación como `AUT`, y los dos límites de
  seguridad (alcance forzado en GET, 403 en PUT sobre actividad ajena)
  confirmados con requests directos. Los dos bugs de Hibernate/ternario que
  aparecieron durante esta fase ya quedaron generalizados como convención en
  la sección "Convenciones del modelo de datos" de arriba.

- ✅ **Fase 7** (ajustes de UI/UX y acceso por rol) — pedidos puntuales fuera
  del roadmap original, todos frontend:
  - **Menú por rol**: el link "Proyectos" del header (`app.component.html`)
    se oculta con `*ngIf="currentUser.role !== 'CON'"` cuando el usuario
    logueado es `CON`; un `AUT` lo sigue viendo igual. Es el único ajuste de
    menú/acceso por rol implementado — no hay guard de ruta ni bloqueo en
    backend para `/projects`/`/timesheets`/`/progress` bajo `CON` (decisión
    explícita del usuario: se pidió acotar el cambio solo al link del menú).
  - **Acción "Ver" en la grilla de Proyectos** (ícono de lupa, junto a
    Editar/Eliminar): abre el mismo modal de 10 pestañas con
    `ProjectFormComponent.readOnly` (`@Input`) en `true` — todos los campos
    de Datos generales/Planificación/Ejecución real quedan `disabled`, se
    ocultan los botones de creación/edición/eliminación en Equipo/Schedule/
    Riesgos/Novedades y los botones "Guardar cambios"/"Registrar avance", y
    el footer dice "Cerrar" en vez de "Cancelar".
  - **Barra de pestañas del modal de Proyecto rediseñada**: se agregó
    `[scrollable]="true"` al `<p-tabView>` — sin ese input PrimeNG no
    dibuja flechas de navegación y las 10 pestañas solo se desplazaban por
    un overflow-x sin scrollbar visible ni soporte de arrastre con mouse,
    o sea que no había forma real de llegar a Novedades/Auditoría. Se
    agregó también subrayado de 3px en `--signal` para la pestaña activa,
    hover con fondo tintado, separadores finos entre pestañas y fade en los
    bordes del contenedor scrollable (todo en `styles.css`, con selectores
    prefijados `.p-tabview` porque el theme `saga-blue` define las mismas
    reglas con esa misma especificidad y gana si no se iguala).
  - **Encabezados de tabla en dos líneas**: en la grilla de Proyectos (y ya
    antes en Timesheets) los `<th>` permiten wrap (`white-space: normal`)
    mientras los `<td>` de datos siguen forzados a una sola línea
    (`white-space: nowrap`) — evita que un título largo ("Fecha inicio
    planificación") ensanche la columna más que su propio dato y dispare
    scroll horizontal innecesario.
  - **Menú renombrado y reordenado**: `Timesheets`→`Cronograma`,
    `Avance`→`Avance Proyecto`, `Avance Semanal`→`Avance Proyecto Semanal`,
    manteniendo las rutas (`/timesheets`, `/progress`, `/weekly-progress`)
    intactas — solo cambia la etiqueta visible y el orden de despliegue.
  - **Riesgos/Novedades (pestañas del modal de Proyecto)**: cabecera de alta
    reorganizada en `.inline-form-grid` (grid de `.field` label+control, un
    campo por celda, mismo patrón que `.filters` en Timesheets/Avance
    semanal) en vez del `.inline-form` flex-wrap sin etiquetas que traía
    antes; columna Fecha forzada a una sola línea
    (`.table-scroll:not(.schedule-table) th/td:first-child { white-space:
    nowrap }`), el resto de columnas envuelve libremente según su
    contenido; calendarios (`riskdate`/`newsdate`, cabecera y fila de
    edición) con `[minDate]="todayDate"` — solo fechas de hoy en adelante
    son seleccionables. `Probabilidad %` en Riesgos valida rango 0-100 en
    frontend (`p-inputNumber [min]="0" [max]="100"`) y backend
    (`RiskService.validateProbability`, 400 si está fuera de rango).
  - **Schedule Proyecto**: se eliminó la restricción "solo Padre edita
    Descripción/Descripción larga" — con la Fase 6 alimentando
    `advrealperc`/`advrealdays` vía Avance Semanal, todos los campos con
    dato son editables también para nodos Hijo (`ScheduleService.update()`
    ya no gatea `applyHijoFields(...)` tras un `isPadre` calculado del
    estado en BD). Fila de edición reescrita a 8 `<td>` explícitos
    (Descripción, Responsable, Días base, Días adicional, Total días base
    de solo lectura, Fecha inicio/fin base, acciones) alineados 1:1 con las
    columnas de cabecera, sin `colspan` ni `*ngIf` de gating; columnas
    numéricas (`Días base`/`Días adicional`/`Total días base`) alineadas a
    la derecha (`.num-col`); calendarios con `[minDate]="todayDate"`.
  - **Solapamiento de títulos/contenido en las tablas del modal de
    Proyecto**: la causa raíz era `table-layout: fixed` (default de
    `<p-table>` en PrimeNG) en las 5 tablas del modal que no tenían
    `responsiveLayout="scroll"` — con columnas de ancho fijo repartido a
    partes iguales, el texto de columnas angostas (fechas nowrap, números)
    se desbordaba visualmente sobre la celda vecina. Se agregó
    `responsiveLayout="scroll"` (fuerza `table-layout: auto` +
    `overflow-x:auto`) a las 5 tablas (Equipo, Consulta, Schedule, Riesgos,
    Novedades) más `min-width` explícito en las columnas de texto libre
    (Descripción/Responsable) para que no queden más angostas que su propio
    contenido.
  - **Equipo de Trabajo**: cabecera de alta reorganizada en
    `.inline-form-grid` (Consultor/Rol/Fecha asignación, cada uno con su
    label) igual que Riesgos/Novedades; calendario de Fecha asignación con
    `[minDate]="todayDate"`.
  - **Validación de rango de fechas (Ejecución Real y Planificación)**:
    `ProjectService.applyRequest` valida, antes de persistir, que
    `baseEndDate ≥ baseStartDate`, `plannedEndDate ≥ plannedStartDate` y
    `realEndDate ≥ realStartDate` (`validateDateOrder(...)`, 400 si se
    viola); en frontend, `saveHeader()` corre la misma validación antes de
    llamar al backend (mensaje `messageService.warn` sin llegar a
    disparar el request) y los `p-calendar` correspondientes usan
    `[minDate]` calculado con `parseLocalDate`/`minDateOrFloor` (fechas
    pasadas deshabilitadas en los 3 pares de fecha).
  - **Planificación y Datos Generales**: campos reorganizados en grid
    dedicado (`.planning-grid`/`.general-grid`, `minmax(280px, 1fr)` en vez
    del `.form-grid` genérico de `minmax(220px, 1fr)`, para evitar filas
    con un único campo huérfano); se agregó botón "Guardar cambios"
    (`saveHeader()`) a ambas pestañas. En Datos Generales se renombraron
    las etiquetas `N.º contrato / propuesta`→`Nº. contrato`, `Descripción
    extendida`→`Descripción del Proyecto`, `Código Project Manager`→
    `Project Manager`.
  - **Avance de Proyectos** (`frontend/src/app/progress/`): los eyebrows
    "Datos Generales"/"Situación General" pasaron de una etiqueta gris
    chica a una barra de ancho completo (`.info-panel__heading`: fondo
    `var(--ink)`, texto `var(--signal)` en negrita) para que sobresalgan;
    se renombraron 6 etiquetas de fecha (`Fecha inicio`→`Fecha Inicio
    Proyecto`, `Fecha informe`→`Fecha Informe Proyecto`, `Inicio
    planificado`→`Fecha Inicio Planificado`, `Fin planificado`→`Fecha Fin
    Planificado`, `Inicio real`→`Fecha Inicio Real`, `Fin real`→`Fecha Fin
    Real`). La recarga de toda la pantalla en función de la fecha de corte
    ya estaba implementada correctamente antes de este pedido
    (`onCutoffChange()` dispara `loadReport()`, que reenvía `cutoffDate` al
    backend y este filtra riesgos/novedades/cambios y recalcula
    `reportDate` con ese valor) — no hizo falta cambio de código, solo se
    verificó end-to-end.

- ✅ **Fase 8** (cálculos del reporte de Avance de Proyectos y pulido de su
  cabecera) — pedidos puntuales sobre `report/ProjectProgressReportService.java`
  y `frontend/src/app/progress/`, todos con el mismo patrón: varias columnas
  de "Días del proyecto por fase"/"Hitos" leían columnas de
  `tprj_project_schedule`/`tprj_project` que ningún módulo alimenta
  (`advexpecteddays`, `varadvplannedperc`, `efectivityperc`) — se reemplazaron
  por cálculos en vivo a partir de columnas que sí tienen dato real, con
  `null` (no 0) cuando el denominador es cero/no aplica, para que el gauge
  del frontend lo muestre como "—" en vez de un valor engañoso:
  - **% Var. asig. planif.** = `Avance real (d) / Total x 100`
    (`plannedAssignmentVariation(...)`).
  - **% Efectividad** = `% Avance real / % Var. asig. planif.` (cociente
    directo, **sin** reescalar a porcentaje — corregido a pedido explícito
    tras una primera versión con `x 100`) (`effectiveness(...)`).
  - **Avance esperado (d)** = `Contratados x (% Var. asig. planif. / 100)`
    (`expectedAdvanceDays(...)`).
  - **Días T.S.** ya no suma `tprj_project_timesheet.hoursconsumed / 8`
    (`sumDaysConsumed`/`HOURS_PER_DAY`/`TimesheetRepository` eliminados de
    esta clase, quedaron sin otro uso) — ahora es una resta de fechas
    (`daysConsumedFromCutoff(...)`): en "Días del proyecto por fase",
    `Fecha de corte - tprj_project.realStartDate` (un único valor de
    proyecto, igual en cada fila y en TOTAL); en "Hitos", `Fecha de corte -
    schedule.baseStartDate` del propio hito (cada hito tiene su propia
    fecha, a diferencia de la fase) — puede dar negativo si el hito todavía
    no arranca a esa fecha de corte, es el resultado correcto.
  - **Fila TOTAL**: `% Var. asig. planif.` se recalcula con la misma fórmula
    sobre los totales ya sumados; `% Var. avance`, `% Avance actual`, `%
    Avance real` y `% Efectividad` ya NO se leen de `tprj_project` (daban
    0/"—" aunque las fases tuvieran datos) — se promedian
    (`averagePercent(...)`) los valores ya calculados en cada fila de fase,
    ignorando las que dieran `null`. De paso se resaltó visualmente la fila
    TOTAL (borde superior y valores de gauge en `--signal`, fondo
    `--pine-bg` — hubo que igualar la especificidad de
    `.p-datatable .p-datatable-tbody > tr` del theme `saga-blue` para que el
    fondo propio ganara).
  - **Hitos por Fase y Consultor** (renombrado desde "Hitos"): mismas dos
    fórmulas (`% Var. asig. planif.`/`% Efectividad`) aplicadas directo
    sobre `advrealdays`/`basedaystotal`/`advrealperc` del propio hito (son
    los nodos "Hijo" que alimenta Avance Semanal, ya traen dato real sin
    agregación) — se agregó la columna `% Var. asig. planif.` a la grilla y
    al Excel; etiquetas `Inicio base`/`Fin base` → `Fecha Inicio Base`/
    `Fecha Fin Base` (grilla y Excel).
  - **Sección "Cambios aprobados"**: deshabilitada temporalmente a pedido
    del cliente vía el flag `changesSectionEnabled = false` en
    `progress-report.component.ts` (todo el bloque envuelto en
    `<ng-container *ngIf="changesSectionEnabled">` en el HTML) — sin borrar
    lógica ni marcado, reactivar es cambiar ese único flag a `true`.
  - **Cabecera de la pantalla**: "Fecha de corte" pasó del `<input
    type="date">` nativo a `p-calendar` (igual al resto de la app), con
    `[minDate]` = Fecha Inicio Real del proyecto elegido; se agregó
    indicador de carga (`[loading]="loading"` en las 5 tablas + texto
    "Actualizando información…") y un hint bajo el campo ("Si no se elige,
    se usa la fecha actual."). **Bug crítico encontrado y corregido**: la
    primera versión usaba un *getter* `cutoffDateValue` que devolvía `new
    Date(...)` en cada ciclo de detección de cambios — `p-calendar` recibía
    una referencia distinta todo el tiempo, la interpretaba como "cambió" y
    entraba en loop de re-render, colgando el navegador ("La página no
    responde") al abrir el calendario. Se corrigió con una propiedad normal
    (`cutoffDateValue: Date | null`) actualizada solo vía `[(ngModel)]` +
    `(onSelect)` (mismo patrón ya usado en `weekly-progress-list`) — un
    *getter* sigue siendo seguro para `[minDate]` porque ese input no tiene
    ningún evento que vuelva a escribirlo (no hay ciclo de realimentación).
    Alineación de los 3 campos de la cabecera corregida agregando
    `width:100%` a `p-calendar`/`p-dropdown`, un `<label>` invisible en el
    campo del botón "Descargar Excel" (para igualar la altura con los
    campos que sí tienen label) y el hint posicionado `absolute` (para que
    no sume alto al campo y desalinee su control).
  - **Excel** (`ProjectProgressExcelExport.java`): etiquetas de cabecera
    sincronizadas con los renombres de Fase 7 (`Fecha inicio`→`Fecha Inicio
    Proyecto`, etc.) y título de sección `Hitos`→`Hitos por Fase y
    Consultor`.

- ✅ **Fase 9** (Excel de Avance sin "Cambios aprobados" + filtro/columna
  Project Manager en Timesheets) — dos pedidos puntuales:
  - **Excel de Avance de Proyectos**: la sección "Cambios aprobados" ya
    estaba deshabilitada en pantalla desde la Fase 8
    (`changesSectionEnabled = false`), pero el export a `.xlsx`
    (`ProjectProgressExcelExport.java`) seguía escribiéndola siempre — se
    eliminó por completo esa sección del Excel (título, tabla y el método
    `writeChangeTable(...)`, más el import ahora no usado de `ChangeView`),
    dejando "Novedades presentadas" como última sección. Verificado
    reiniciando el backend (las clases ya compiladas seguían sirviendo el
    Excel viejo hasta el restart) e inspeccionando el `.xlsx` descargado vía
    API (`unzip` + grep sobre `xl/sharedStrings.xml`) para confirmar que la
    etiqueta ya no aparece.
  - **Registro de Timesheets** (`frontend/src/app/timesheet/`): se agregó un
    filtro nuevo "Project Manager" (`pmFilter`, poblado con
    `pmOptions` — lista deduplicada de `codeuserPm`/`pmName` derivada en
    vivo de los proyectos ya cargados, nunca del catálogo genérico de
    usuarios, para evitar valores estáticos o no relacionados con proyectos
    existentes) y una columna "Project Manager" en la grilla, ubicada
    inmediatamente después de "Proyecto / Incidencia" (`pmNameFor(entry)`,
    resuelto por `entry.seqproject` contra la lista de proyectos ya
    cargada). El filtro se resuelve 100% en el cliente
    (`filteredEntries`/`applyPmFilter()`) porque el PM no es una columna de
    `tprj_project_timesheet` sino que se deriva de su proyecto — no requirió
    cambios de backend ni de `TimesheetFilters`. "Horas filtradas" y el
    `p-table` ahora leen de `filteredEntries` en vez de `entries`
    directamente. Insertar esta columna corrió en una posición la columna
    "Descripción" (de la 8ª a la 9ª) — el selector CSS `nth-child(8)` que la
    hacía la única columna de texto libre con wrap quedó apuntando a "Tipo
    actividad" por error; se corrigió a `nth-child(9)` y de paso se pidió
    "achicar" esa columna: se agregó `min-width:150px`/`max-width:200px` +
    `word-break:break-word` (antes solo tenía `min-width`, sin tope
    superior, así que el layout `auto` de la tabla la dejaba crecer tan
    ancha como el texto más largo en una sola línea en vez de partirlo en
    varias) — verificado midiendo `getBoundingClientRect()` de la celda:
    150px de ancho fijo y alto variable según cuántas líneas ocupa cada
    descripción.

Cada fase se implementa y valida (build + prueba manual en navegador) antes
de pasar a la siguiente.
