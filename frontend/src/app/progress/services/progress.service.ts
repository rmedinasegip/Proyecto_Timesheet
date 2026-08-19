import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ProjectProgressReport } from '../models/progress.model';

@Injectable({ providedIn: 'root' })
export class ProgressService {
  constructor(private http: HttpClient) {}

  getReport(seqproject: number, cutoffDate: string | null): Observable<ProjectProgressReport> {
    let params = new HttpParams();
    if (cutoffDate) {
      params = params.set('cutoffDate', cutoffDate);
    }
    return this.http.get<ProjectProgressReport>(`/api/projects/${seqproject}/progress-report`, { params });
  }

  downloadExcel(seqproject: number, cutoffDate: string | null): Observable<Blob> {
    let params = new HttpParams();
    if (cutoffDate) {
      params = params.set('cutoffDate', cutoffDate);
    }
    return this.http.get(`/api/projects/${seqproject}/progress-report/excel`, { params, responseType: 'blob' });
  }
}
