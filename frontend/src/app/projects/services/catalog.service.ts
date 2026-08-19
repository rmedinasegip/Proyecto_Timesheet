import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CatalogItem, Company, Customer, UserOption } from '../models/project.model';

@Injectable({ providedIn: 'root' })
export class CatalogService {
  constructor(private http: HttpClient) {}

  getCatalogItems(codecat: string): Observable<CatalogItem[]> {
    return this.http.get<CatalogItem[]>(`/api/catalogs/${codecat}/items`);
  }

  getCustomers(): Observable<Customer[]> {
    return this.http.get<Customer[]>('/api/customers');
  }

  getUsers(): Observable<UserOption[]> {
    return this.http.get<UserOption[]>('/api/users');
  }

  getCompanies(): Observable<Company[]> {
    return this.http.get<Company[]>('/api/companies');
  }
}
