# Manual de Instalación en Producción

Sistema: **Gestión y Control de Proyectos - TimeSheets**
Stack: Java 11 + Spring Boot 2.1.18 · Angular 12.2.2 + PrimeNG 12.1.1 · PostgreSQL 18

Este manual describe cómo instalar el sistema en un servidor de producción
Linux, partiendo de un repositorio ya construido y probado en desarrollo
(roadmap Fases 0-7, ver `CLAUDE.md`/`AGENTS.md`). No cubre CI/CD ni
contenedores — el proyecto no usa Docker en ningún ambiente (decisión de
diseño ya tomada, ver `CLAUDE.md`), así que el despliegue es directo sobre
el sistema operativo.

## 1. Arquitectura de despliegue propuesta

```
                         Internet
                            |
                         HTTPS (443)
                            |
                    +---------------+
                    |     Nginx     |  reverse proxy + TLS + archivos estáticos
                    +---------------+
                     /              \
        sirve /  (Angular build)     proxy_pass /api -> 127.0.0.1:8080
        dist/frontend/browser        |
                                      v
                            +-------------------+
                            |  Spring Boot JAR   |  systemd service, puerto 8080
                            |  (timesheet-       |  solo escucha en localhost
                            |   backend.jar)     |
                            +-------------------+
                                      |
                                      v
                            +-------------------+
                            |   PostgreSQL 18    |  puerto 5432, solo localhost
                            +-------------------+
```

- Un único servidor alcanza para el volumen de este piloto (Nginx, backend y
  Postgres en la misma máquina). Si el cliente lo requiere, Postgres puede
  vivir en un host aparte — solo cambia la URL de conexión, no la
  arquitectura.
- El frontend Angular ya llama a la API con rutas relativas (`/api/...`, sin
  `http://localhost` hardcodeado en ningún componente/servicio) — por eso
  puede servirse como archivos estáticos desde el mismo dominio que hace de
  reverse proxy hacia el backend, sin configuración adicional de CORS.

## 2. Prerrequisitos del servidor

| Software | Versión | Notas |
|---|---|---|
| Sistema operativo | Linux (Ubuntu 22.04 LTS o similar) | Cualquier distro con `systemd` |
| Java | JDK 11 (headless está bien, solo corre el JAR) | `java -version` debe reportar 11.x |
| PostgreSQL | 18 | Igual que en desarrollo (`postgresql://.../postgres`) |
| Nginx | 1.18+ | Reverse proxy + servidor de estáticos |
| Node.js | 16.x (solo en la máquina de build, no en el servidor) | Necesario únicamente para compilar el frontend — ver nota Angular 12/OpenSSL abajo |
| Certificado TLS | Let's Encrypt (certbot) u otro | HTTPS es obligatorio, ver §7 |

**Nota Node/Angular 12**: Angular 12 usa Webpack 4, incompatible con OpenSSL 3
de Node ≥17 (`ERR_OSSL_EVP_UNSUPPORTED`). Usar Node 16.x para el build, o
Node 18+ solo si se antepone `NODE_OPTIONS=--openssl-legacy-provider` (ya
resuelto en los scripts de `package.json` — usar siempre `npm run build`, no
`ng build` directo).

El build del frontend (`npm run build`) puede hacerse en la máquina de
desarrollo/CI y subir solo el resultado (`dist/frontend/`) al servidor — no
hace falta instalar Node en producción.

## 3. Base de datos

### 3.1 Crear rol y base

No reutilizar el usuario `postgres` de superusuario en producción (en
desarrollo el proyecto usa `postgres`/`postgres` directo, aceptable solo en
local). Crear un rol dedicado con privilegios acotados a su propia base:

```bash
sudo -u postgres psql -v ON_ERROR_STOP=1 <<'SQL'
CREATE ROLE timesheet_app WITH LOGIN PASSWORD 'CAMBIAR_ESTA_CLAVE';
CREATE DATABASE timesheet OWNER timesheet_app;
SQL
```

### 3.2 Aplicar el esquema

El proyecto no usa Flyway ni `ddl-auto` — el esquema se versiona como SQL
plano en `backend/sql/V*.sql` y se aplica manualmente, en orden estricto:

```bash
export PGPASSWORD='CAMBIAR_ESTA_CLAVE'
for f in backend/sql/V1__catalogs_and_masters.sql \
         backend/sql/V2__seed_catalogs_and_masters.sql \
         backend/sql/V3__project_module.sql \
         backend/sql/V4__timesheet_module.sql \
         backend/sql/V5__project_change_module.sql \
         backend/sql/V6__auth.sql \
         backend/sql/V7__schedule_weekly_progress.sql; do
  echo "Aplicando $f..."
  psql -h localhost -U timesheet_app -d timesheet -v ON_ERROR_STOP=1 -f "$f"
done
```

`PGPASSWORD` es obligatorio en el entorno (o usar `.pgpass`) — sin eso
`psql` queda colgado esperando la contraseña por stdin en vez de fallar
limpio, y en un script no interactivo eso cuelga el despliegue.

`V2__seed_catalogs_and_masters.sql` siembra datos de **prueba** (10
consultores/usuarios JORUPE de ejemplo, clientes/compañías de demo). Antes
de ir a producción real con datos del cliente, decidir con el negocio si
ese seed se aplica tal cual (piloto/demo) o se reemplaza por un script de
maestros reales — no está automatizado, es una decisión de negocio fuera
del alcance de este manual.

### 3.3 Contraseñas de usuario (login)

`tins_user.passwordhash` no se siembra en SQL plano (un hash bcrypt no se
escribe a mano de forma práctica) — lo genera
`auth/PasswordSeedRunner` en el primer arranque del backend, para todo
usuario cuyo `passwordhash` esté en `null`. Contraseña de demo:
**`Llacsaa2026`**.

> ⚠️ Es una contraseña de demo/piloto compartida por los 10 usuarios
> sembrados. Antes de dar acceso real a producción, cada usuario debe
> cambiarla (hoy el sistema no tiene pantalla de "cambiar contraseña" — ver
> §9, es una tarea pendiente a evaluar con el cliente antes de ir en vivo
> con usuarios reales).

## 4. Backend: build y configuración

### 4.1 Empaquetar

```bash
cd backend
./mvnw.cmd clean package -DskipTests   # o ./mvnw en Linux si el wrapper Unix está presente
```

Genera `backend/target/timesheet-backend-0.1.0-SNAPSHOT.jar` (Spring Boot
"fat jar", incluye Tomcat embebido — no hace falta instalar un servidor de
aplicaciones aparte).

### 4.2 Configuración de producción — variables de entorno

`backend/src/main/resources/application.yml` trae valores de **desarrollo
local** hardcodeados (usuario/clave de Postgres, secreto JWT). Spring Boot
permite sobreescribir cualquier propiedad vía variable de entorno sin tocar
el archivo (relaxed binding: `spring.datasource.url` →
`SPRING_DATASOURCE_URL`) — usar ese mecanismo en producción, nunca editar
el `application.yml` del build con credenciales reales.

Variables a definir en el entorno del servicio (ver plantilla systemd
en §4.3):

| Variable | Reemplaza | Ejemplo / regla |
|---|---|---|
| `SPRING_DATASOURCE_URL` | `spring.datasource.url` | `jdbc:postgresql://localhost:5432/timesheet` |
| `SPRING_DATASOURCE_USERNAME` | `spring.datasource.username` | `timesheet_app` |
| `SPRING_DATASOURCE_PASSWORD` | `spring.datasource.password` | la clave creada en §3.1 |
| `JWT_SECRET` | `jwt.secret` | cadena aleatoria ≥32 caracteres, **distinta** a la de dev (`JwtService` rechaza arrancar si mide menos de 32) — generar con `openssl rand -base64 48` |
| `JWT_EXPIRES_IN_HOURS` | `jwt.expires-in-hours` | `8`–`24` según política del cliente (default actual: 24) |
| `SERVER_PORT` | `server.port` | dejar `8080`; Nginx es el único punto expuesto a Internet |

No es necesario editar código para esto — Spring ya lee `${...}` desde el
entorno por convención estándar de Spring Boot.

### 4.3 Servicio systemd

Crear `/etc/systemd/system/timesheet-backend.service`:

```ini
[Unit]
Description=TimeSheets - Backend Spring Boot
After=network.target postgresql.service

[Service]
Type=simple
User=timesheet
WorkingDirectory=/opt/timesheet/backend
ExecStart=/usr/bin/java -jar /opt/timesheet/backend/timesheet-backend.jar
Restart=on-failure
RestartSec=5

Environment=SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/timesheet
Environment=SPRING_DATASOURCE_USERNAME=timesheet_app
Environment=SPRING_DATASOURCE_PASSWORD=CAMBIAR_ESTA_CLAVE
Environment=JWT_SECRET=CAMBIAR_POR_UN_SECRETO_ALEATORIO_DE_48_BYTES
Environment=JWT_EXPIRES_IN_HOURS=24

[Install]
WantedBy=multi-user.target
```

> Preferir un `EnvironmentFile=/opt/timesheet/backend/.env` (permisos
> `600`, dueño `timesheet`) en vez de listar `Environment=` con secretos
> directo en el `.service` — ese archivo queda legible por cualquiera con
> `systemctl cat` si no se separa.

Crear el usuario de sistema sin login, copiar el jar, y levantar el
servicio:

```bash
sudo useradd --system --no-create-home --shell /usr/sbin/nologin timesheet
sudo mkdir -p /opt/timesheet/backend
sudo cp backend/target/timesheet-backend-0.1.0-SNAPSHOT.jar /opt/timesheet/backend/timesheet-backend.jar
sudo chown -R timesheet:timesheet /opt/timesheet
sudo systemctl daemon-reload
sudo systemctl enable --now timesheet-backend
sudo systemctl status timesheet-backend
```

Verificar arranque en logs (`journalctl -u timesheet-backend -f`): debe
mostrar `Started TimesheetApplication` sin excepciones, y
`PasswordSeedRunner` corre una sola vez sembrando los hashes de demo.

## 5. Frontend: build de producción

```bash
cd frontend
npm ci
npm run build   # usa la config "production" por defecto (angular.json), sale en dist/frontend
```

`npm run build` ya antepone
`NODE_OPTIONS=--openssl-legacy-provider` (scripts de `package.json`) — no
usar `ng build` directo salvo agregando ese flag a mano.

El resultado queda en `frontend/dist/frontend/` (bundle Angular con
`fileReplacements` apuntando a `environment.prod.ts`, minificado,
hasheado). Copiar ese directorio al servidor:

```bash
sudo mkdir -p /opt/timesheet/frontend
sudo cp -r frontend/dist/frontend/* /opt/timesheet/frontend/
sudo chown -R www-data:www-data /opt/timesheet/frontend
```

## 6. Nginx: reverse proxy + estáticos + TLS

`/etc/nginx/sites-available/timesheet`:

```nginx
server {
    listen 80;
    server_name timesheet.ejemplo.com;
    return 301 https://$host$request_uri;
}

server {
    listen 443 ssl http2;
    server_name timesheet.ejemplo.com;

    ssl_certificate     /etc/letsencrypt/live/timesheet.ejemplo.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/timesheet.ejemplo.com/privkey.pem;

    # Angular: archivos estáticos + fallback a index.html (rutas del router)
    root /opt/timesheet/frontend;
    index index.html;
    location / {
        try_files $uri $uri/ /index.html;
    }

    # API: proxy al backend, que solo escucha en localhost
    location /api/ {
        proxy_pass http://127.0.0.1:8080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_cookie_path / /;
    }

    add_header X-Content-Type-Options nosniff;
    add_header X-Frame-Options DENY;
}
```

```bash
sudo ln -s /etc/nginx/sites-available/timesheet /etc/nginx/sites-enabled/
sudo certbot --nginx -d timesheet.ejemplo.com   # emite y configura el certificado
sudo nginx -t && sudo systemctl reload nginx
```

## 7. Ajuste de código requerido antes de exponer HTTPS real

`AuthController.setCookie(...)`
(`backend/src/main/java/com/llacsaa/timesheet/auth/AuthController.java`)
tiene hoy:

```java
ResponseCookie cookie = ResponseCookie.from(JwtService.AUTH_COOKIE_NAME, token)
        .httpOnly(true)
        .secure(false) // dev/piloto sobre http; poner true al desplegar con https real
        .sameSite("Strict")
        ...
```

El propio comentario en el código marca esto como pendiente para
producción. Con Nginx terminando TLS (§6), este flag debe pasar a `true`
antes de ir a producción — si no, la cookie de sesión (`auth_token`) viaja
sin el atributo `Secure`, lo que la expone si algún día el sitio se sirve
también por HTTP. **Este es el único cambio de código pendiente
identificado para producción**; todo lo demás en este manual es
configuración/infraestructura, sin tocar el repositorio. Aplicarlo, volver
a empaquetar (§4.1) y redesplegar antes del go-live.

## 8. Verificación post-instalación

```bash
# 1. Backend vivo (sin pasar por Nginx)
curl -s http://127.0.0.1:8080/api/ping

# 2. A través de Nginx, con TLS
curl -s https://timesheet.ejemplo.com/api/ping

# 3. Login con un usuario de demo (ver seed en docs/erd.md)
curl -i -s -X POST https://timesheet.ejemplo.com/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"<usuario-demo>@...","password":"Llacsaa2026"}'
# esperar 200 + Set-Cookie: auth_token=...; Secure; HttpOnly; SameSite=Strict
```

Luego, navegador: abrir `https://timesheet.ejemplo.com`, iniciar sesión,
confirmar que el menú se arma según el rol (`CON`/`AUT`), crear un proyecto
de prueba y verificar que se refleja en `/timesheets`, `/progress` y
`/weekly-progress`.

## 9. Checklist de seguridad antes de ir en vivo

- [ ] `JWT_SECRET` de producción generado aleatoriamente (≥32 caracteres),
      distinto al de desarrollo, no versionado en ningún repo.
- [ ] Clave del rol `timesheet_app` en Postgres distinta a `postgres`/`postgres`
      de desarrollo.
- [ ] `AuthController.setCookie(...)` con `.secure(true)` (§7) desplegado.
- [ ] Contraseña de demo `Llacsaa2026` cambiada o rotada para todo usuario
      con acceso real (hoy no hay pantalla de autoservicio para esto —
      evaluar con el cliente antes del go-live con usuarios reales).
- [ ] Postgres escuchando solo en `localhost` (o red privada si está en
      otro host) — nunca expuesto directo a Internet.
- [ ] Puerto 8080 del backend no expuesto públicamente — solo accesible vía
      el `proxy_pass` de Nginx en `127.0.0.1`.
- [ ] Firewall del servidor permite únicamente 80/443 entrantes (más SSH
      administrativo restringido por IP).
- [ ] Backups automatizados de Postgres (ver §10).

## 10. Backups y mantenimiento

```bash
# Dump diario, ejemplo de cron para /etc/cron.d/timesheet-backup
0 2 * * * postgres pg_dump -Fc timesheet > /var/backups/timesheet/timesheet_$(date +\%Y\%m\%d).dump
```

Rotar backups antiguos (ej. conservar 30 días) y probar la restauración
periódicamente (`pg_restore`) — un backup nunca verificado no cuenta como
backup.

Logs del backend: `journalctl -u timesheet-backend` (systemd ya rota logs
por defecto). Logs de Nginx: `/var/log/nginx/access.log` /
`error.log`, con `logrotate` estándar de la distro.

## 11. Actualizar una versión nueva

1. Aplicar en la base de datos, en orden, cualquier `V*.sql` nuevo que no
   se haya corrido todavía (comparar contra lo ya aplicado — no hay tabla
   de control de migraciones tipo Flyway, así que llevar registro manual
   de qué `V*.sql` se aplicó en cada ambiente).
2. `git pull` / desplegar el código nuevo, `mvnw package` (§4.1),
   `npm run build` (§5).
3. Copiar el jar y el `dist/frontend/` nuevos a `/opt/timesheet/`.
4. `sudo systemctl restart timesheet-backend`.
5. `sudo systemctl reload nginx` (solo si cambió la config de Nginx).
6. Repetir la verificación de §8.
