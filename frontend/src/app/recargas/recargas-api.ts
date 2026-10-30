import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { Operador, Recarga, ResumenRecargas, Vendedor, VentaRecarga } from './recargas.models';

/** Cliente de la API REST de recargas (Spring Boot). */
@Injectable({ providedIn: 'root' })
export class RecargasApi {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api';

  operadores(): Observable<Operador[]> {
    return this.http.get<Operador[]>(`${this.baseUrl}/operadores`);
  }

  vendedores(): Observable<Vendedor[]> {
    return this.http.get<Vendedor[]>(`${this.baseUrl}/vendedores`);
  }

  vender(venta: VentaRecarga): Observable<Recarga> {
    return this.http.post<Recarga>(`${this.baseUrl}/recargas`, venta);
  }

  ultimas(limite = 10): Observable<Recarga[]> {
    return this.http.get<Recarga[]>(`${this.baseUrl}/recargas`, {
      params: new HttpParams().set('limite', limite),
    });
  }

  resumen(desde?: string, hasta?: string): Observable<ResumenRecargas> {
    let params = new HttpParams();
    if (desde) {
      params = params.set('desde', desde);
    }
    if (hasta) {
      params = params.set('hasta', hasta);
    }
    return this.http.get<ResumenRecargas>(`${this.baseUrl}/recargas/resumen`, { params });
  }
}
