import { HttpClient } from '@angular/common/http';
import { Injectable, computed, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { LoginResponse, Rol, Usuario } from '../models/usuario.model';

const STORAGE_KEY = 'hosteleria_auth';

interface StoredAuth {
  token: string;
  usuario: Usuario;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly authState = signal<StoredAuth | null>(this.readFromStorage());

  readonly usuario = computed(() => this.authState()?.usuario ?? null);
  readonly token = computed(() => this.authState()?.token ?? null);
  readonly isAuthenticated = computed(() => this.authState() !== null);
  readonly isAdmin = computed(() => this.authState()?.usuario.rol === 'ADMIN');

  constructor(private http: HttpClient) {}

  login(username: string, password: string): Observable<LoginResponse> {
    return this.http
      .post<LoginResponse>(`${environment.apiUrl}/auth/login`, { username, password })
      .pipe(
        tap((response) => {
          const usuario: Usuario = {
            id: response.id,
            username: response.username,
            nombre: response.nombre,
            rol: response.rol,
            activo: true
          };
          const stored: StoredAuth = { token: response.token, usuario };
          localStorage.setItem(STORAGE_KEY, JSON.stringify(stored));
          this.authState.set(stored);
        })
      );
  }

  logout(): void {
    localStorage.removeItem(STORAGE_KEY);
    this.authState.set(null);
  }

  hasRole(rol: Rol): boolean {
    return this.authState()?.usuario.rol === rol;
  }

  private readFromStorage(): StoredAuth | null {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (!raw) {
      return null;
    }
    try {
      return JSON.parse(raw) as StoredAuth;
    } catch {
      return null;
    }
  }
}