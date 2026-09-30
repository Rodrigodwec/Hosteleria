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

export interface CrearUsuarioRequest {
  username: string;
  password: string;
  nombre: string;
}

export interface ActualizarUsuarioRequest {
  nombre: string;
  password?: string;
  activo: boolean;
}