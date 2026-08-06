import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { User } from '../models/user.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class RoleService {

  apiUrl = environment.apiUrl;
  constructor(private httpClient: HttpClient) { }

  getRolesByPage(): Observable<any> {
    return this.httpClient.get<User[]>(`${this.apiUrl}/roles?noPage=0&sizePage=0`);
  }
  getAllRoles(): Observable<any> {
    return this.httpClient.get<User[]>(`${this.apiUrl}/roles`);
  }

}
