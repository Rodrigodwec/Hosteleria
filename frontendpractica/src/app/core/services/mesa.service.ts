import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Comanda } from '../models/comanda.model';
import { CrearMesaRequest, Mesa } from '../models/mesa.model';

@Injectable({ providedIn: 'root' })
export class MesaService {
  private readonly baseUrl = `${environment.apiUrl}/mesas`;

  constructor(private http: HttpClient) {}

  listar(): Observable<Mesa[]> {
    return this.http.get<Mesa[]>(this.baseUrl);
  }

    crear(request: CrearMesaRequest): Observable<Mesa> {
    return this.http.post<Mesa>(this.baseUrl, request);
  }

  ocupar(id: number): Observable<Comanda> {
    return this.http.post<Comanda>(`${this.baseUrl}/${id}/ocupar`, {});
  }
}