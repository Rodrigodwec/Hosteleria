import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Comanda } from '../models/comanda.model';

@Injectable({ providedIn: 'root' })
export class ComandaService {
  private readonly baseUrl = `${environment.apiUrl}/comandas`;

  constructor(private http: HttpClient) {}

  obtenerPorMesa(mesaId: number): Observable<Comanda> {
    return this.http.get<Comanda>(`${this.baseUrl}/mesa/${mesaId}`);
  }

  agregarLinea(comandaId: number, productoId: number, cantidad: number): Observable<Comanda> {
    return this.http.post<Comanda>(`${this.baseUrl}/${comandaId}/lineas`, { productoId, cantidad });
  }

  actualizarCantidad(comandaId: number, lineaId: number, cantidad: number): Observable<Comanda> {
    return this.http.put<Comanda>(`${this.baseUrl}/${comandaId}/lineas/${lineaId}`, { cantidad });
  }

  eliminarLinea(comandaId: number, lineaId: number): Observable<Comanda> {
    return this.http.delete<Comanda>(`${this.baseUrl}/${comandaId}/lineas/${lineaId}`);
  }

  eliminarComanda(comandaId: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${comandaId}`);
  }

  cobrar(comandaId: number): Observable<Comanda> {
    return this.http.post<Comanda>(`${this.baseUrl}/${comandaId}/cobrar`, {});
  }
}