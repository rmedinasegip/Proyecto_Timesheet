import { Injectable } from '@angular/core';
import { HttpErrorResponse, HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { AuthService } from '../services/auth.service';

/**
 * Cualquier 401 fuera de /api/auth/** (que ya maneja su propio error en
 * AuthGuard/LoginComponent) implica que la cookie expiró o se invalidó a
 * mitad de sesión — limpia el estado en memoria y redirige a /login. El
 * guard de rutas es la conveniencia de UX de entrada; esto cubre el caso de
 * quedarse "logueado" en el cliente mientras el backend ya rechaza todo.
 */
@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  constructor(private authService: AuthService, private router: Router) {}

  intercept(req: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    return next.handle(req).pipe(
      catchError((error: HttpErrorResponse) => {
        if (error.status === 401 && !req.url.startsWith('/api/auth/')) {
          this.authService.clearSession();
          this.router.navigate(['/login'], { queryParams: { returnUrl: this.router.url } });
        }
        return throwError(error);
      })
    );
  }
}
