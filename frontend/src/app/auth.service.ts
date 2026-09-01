import { HttpInterceptorFn } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly key = 'cha-rifa-admin-token';
  get token(): string | null {
    return sessionStorage.getItem(this.key);
  }
  save(token: string): void {
    sessionStorage.setItem(this.key, token);
  }
  logout(): void {
    sessionStorage.removeItem(this.key);
  }
  isAuthenticated(): boolean {
    const token = this.token;
    if (!token) return false;
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      return payload.exp * 1000 > Date.now();
    } catch {
      this.logout();
      return false;
    }
  }
}
export const authInterceptor: HttpInterceptorFn = (request, next) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  const authenticatedRequest =
    request.url.startsWith('/api/v1/admin') && auth.token
      ? request.clone({ setHeaders: { Authorization: `Bearer ${auth.token}` } })
      : request;
  return next(authenticatedRequest).pipe(
    catchError((error) => {
      if (error.status === 401 && !request.url.endsWith('/auth/login')) {
        auth.logout();
        router.navigate(['/admin/login']);
      }
      return throwError(() => error);
    }),
  );
};
