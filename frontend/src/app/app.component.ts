import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { AuthService } from './auth/services/auth.service';
import { CurrentUser } from './auth/models/auth.model';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {
  title = 'Gestión y Control de Proyectos - TimeSheets';
  backendStatus = 'consultando...';

  constructor(private http: HttpClient, public authService: AuthService, private router: Router) {}

  ngOnInit(): void {
    this.http.get<{ status: string }>('/api/ping').subscribe({
      next: (res) => (this.backendStatus = res.status),
      error: () => (this.backendStatus = 'sin conexión con el backend')
    });

    // Restaura la sesión desde la cookie httpOnly si la página se recargó
    // con un login previo aún vigente — silencioso si no hay sesión, el
    // AuthGuard se encarga de redirigir a /login cuando haga falta.
    this.authService.fetchMe().subscribe({ error: () => {} });
  }

  get currentUser(): CurrentUser | null {
    return this.authService.currentUser;
  }

  logout(): void {
    this.authService.logout().subscribe(() => this.router.navigate(['/login']));
  }
}
