import { HttpClient } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from './auth.service';
interface LoginResponse {
  token: string;
  tokenType: string;
  expiresAt: string;
  username: string;
}
@Component({
  selector: 'app-admin-login',
  imports: [FormsModule, RouterLink],
  template: ` <main class="login-page">
    <a routerLink="/" class="back">← Voltar para a rifa</a>
    <section class="login-card">
      <div class="admin-mark">JL</div>
      <span>ÁREA ADMINISTRATIVA</span>
      <h1>Acessar painel</h1>
      <p>Entre com suas credenciais para gerenciar as reservas.</p>
      <form #formRef="ngForm" (ngSubmit)="login()">
        <label
          >Usuário<input
            name="username"
            required
            [(ngModel)]="form.username"
            autocomplete="username" /></label
        ><label
          >Senha<input
            name="password"
            type="password"
            required
            [(ngModel)]="form.password"
            autocomplete="current-password"
        /></label>
        @if (error()) {
          <div class="login-error">{{ error() }}</div>
        }
        <button [disabled]="formRef.invalid || loading()">
          {{ loading() ? 'Entrando...' : 'Entrar' }}
        </button>
      </form>
    </section>
  </main>`,
  styles: [
    `
      .login-page {
        min-height: 100vh;
        display: grid;
        place-items: center;
        padding: 30px;
        background: linear-gradient(145deg, #eaf3f8, #c7dce9);
        font-family: 'DM Sans', sans-serif;
      }
      .back {
        position: absolute;
        top: 28px;
        left: 32px;
        color: #36566a;
        text-decoration: none;
        font-size: 13px;
      }
      .login-card {
        width: min(430px, 100%);
        padding: 45px;
        border-radius: 14px;
        background: #fbf8f1;
        box-shadow: 0 25px 70px #29475833;
      }
      .admin-mark {
        display: grid;
        place-items: center;
        width: 52px;
        height: 52px;
        margin-bottom: 22px;
        border-radius: 50%;
        background: #27475d;
        color: white;
        font:
          italic 17px 'Playfair Display',
          serif;
      }
      .login-card > span {
        color: #7196b4;
        font-size: 11px;
        font-weight: 700;
        letter-spacing: 0.16em;
      }
      .login-card h1 {
        margin: 10px 0;
        color: #27475d;
        font:
          600 37px 'Playfair Display',
          serif;
      }
      .login-card > p {
        color: #71838e;
        font-size: 14px;
        line-height: 1.6;
      }
      .login-card form {
        display: grid;
        gap: 17px;
        margin-top: 27px;
      }
      .login-card label {
        display: grid;
        gap: 7px;
        color: #526976;
        font-size: 12px;
        font-weight: 700;
      }
      .login-card input {
        padding: 13px;
        border: 1px solid #d2d9da;
        border-radius: 6px;
        outline: none;
      }
      .login-card input:focus {
        border-color: #7196b4;
        box-shadow: 0 0 0 3px #7196b422;
      }
      .login-card button {
        padding: 14px;
        border: 0;
        border-radius: 6px;
        background: #27475d;
        color: white;
        font-weight: 700;
        cursor: pointer;
      }
      .login-card button:disabled {
        opacity: 0.6;
      }
      .login-error {
        padding: 11px;
        border-radius: 6px;
        background: #f7e4e4;
        color: #9a3f3f;
        font-size: 13px;
      }
    `,
    `
      @media (max-width: 520px) {
        .login-page {
          padding: 16px;
          align-items: center;
        }
        .back {
          top: 16px;
          left: 16px;
        }
        .login-card {
          padding: 32px 20px;
        }
        .login-card h1 {
          font-size: 32px;
        }
        .admin-mark {
          margin-top: 18px;
        }
      }
    `,
  ],
})
export class AdminLogin {
  private http = inject(HttpClient);
  private auth = inject(AuthService);
  private router = inject(Router);
  form = { username: '', password: '' };
  loading = signal(false);
  error = signal('');
  login(): void {
    this.loading.set(true);
    this.error.set('');
    this.http.post<LoginResponse>('/api/v1/admin/auth/login', this.form).subscribe({
      next: (response) => {
        this.auth.save(response.token);
        this.router.navigate(['/admin']);
      },
      error: () => {
        this.error.set('Usuário ou senha inválidos.');
        this.loading.set(false);
      },
    });
  }
}
