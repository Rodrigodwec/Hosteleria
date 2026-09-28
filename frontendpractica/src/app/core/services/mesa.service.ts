import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Comanda } from '../models/comanda.model';
import { Mesa } from '../models/mesa.model';

@Injectable({ providedIn: 'root' })
export class MesaService {
  private readonly baseUrl = `${environment.apiUrl}/mesas`;

  constructor(private http: HttpClient) {}

  listar(): Observable<Mesa[]> {
    return this.http.get<Mesa[]>(this.baseUrl);
  }

  ocupar(id: number): Observable<Comanda> {
    return this.http.post<Comanda>(`${this.baseUrl}/${id}/ocupar`, {});
  }
}