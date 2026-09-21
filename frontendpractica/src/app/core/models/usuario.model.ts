export type Rol = 'ADMIN' | 'CAMARERO';

export interface Usuario {
  id: number;
  username: string;
  nombre: string;
  rol: Rol;
  activo: boolean;
}

export interface LoginResponse {
  token: string;
  id: number;
  username: string;
  nombre: string;
  rol: Rol;
}