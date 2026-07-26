import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

/**
 * Client des endpoints cartographiques de supervision (`/v1/maps/**`).
 *
 * `getDepartment()` renvoie le contour du département de référence, `getDepotoirs()` la liste
 * des dépotoirs géolocalisés. Les coordonnées sont des chaînes (latitude/longitude) côté DTO.
 */
@Injectable({ providedIn: 'root' })
export class MapsService {

  constructor(private http: HttpClient) { }

  getDepartment(): Observable<any> {
    return this.http.get<any>(`${environment.apiUrl}/maps/departments`);
  }

  getDepotoirs(): Observable<any[]> {
    return this.http.get<any[]>(`${environment.apiUrl}/maps/depotoirs`);
  }
}
