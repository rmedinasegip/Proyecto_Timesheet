# Gestión y Control de Proyectos - TimeSheets

Sistema web para registro, control y seguimiento de timesheets de
consultores por proyecto/actividad, con equipo de proyecto, cronograma de
avance, riesgos, novedades y reportes.

Ver [CLAUDE.md](./CLAUDE.md) para arquitectura, convenciones y estado del
roadmap.

## Requisitos

- Java 11
- Node.js (cualquier versión reciente — los scripts ya incluyen el flag de
  compatibilidad con OpenSSL 3 que Angular 12 necesita)
- PostgreSQL 18 corriendo en `localhost:5432` (usuario/clave `postgres`)

## Arrancar en desarrollo

```bash
# Backend (puerto 8080)
cd backend
./mvnw.cmd spring-boot:run

# Frontend (puerto 4200, con proxy /api -> backend:8080)
cd frontend
npm install
npm start
```
