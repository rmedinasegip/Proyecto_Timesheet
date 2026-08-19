import { Component } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent {
  email = '';
  password = '';
  loading = false;
  errorMessage: string | null = null;

  constructor(private authService: AuthService, private router: Router, private route: ActivatedRoute) {}

  submit(): void {
    if (!this.email || !this.password) {
      this.errorMessage = 'Ingrese su correo y contraseña';
      return;
    }
    this.loading = true;
    this.errorMessage = null;
    this.authService.login({ email: this.email, password: this.password }).subscribe({
      next: () => {
        this.loading = false;
        const returnUrl = this.route.snapshot.queryParamMap.get('returnUrl') || '/projects';
        this.router.navigateByUrl(returnUrl);
      },
      error: () => {
        this.loading = false;
        this.errorMessage = 'Correo o contraseña incorrectos';
      }
    });
  }
}
