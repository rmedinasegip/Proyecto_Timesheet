import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  WeeklyProgressActivity,
  WeeklyProgressFilters,
  WeeklyProgressReviewRequest,
  WeeklyProgressWeekRequest
} from '../models/weekly-progress.model';

@Injectable({ providedIn: 'root' })
export class WeeklyProgressService {
  private base = '/api/schedule-progress';

  constructor(private http: HttpClient) {}

  listActivities(filters: WeeklyProgressFilters): Observable<WeeklyProgressActivity[]> {
    let params = new HttpParams();
    if (filters.weekStart) params = params.set('weekStart', filters.weekStart);
    if (filters.seqproject != null) params = params.set('seqproject', filters.seqproject);
    if (filters.memberuser != null) params = params.set('memberuser', filters.memberuser);
    if (filters.status) params = params.set('status', filters.status);
    return this.http.get<WeeklyProgressActivity[]>(`${this.base}/activities`, { params });
  }

  saveWeek(request: WeeklyProgressWeekRequest): Observable<WeeklyProgressActivity> {
    return this.http.put<WeeklyProgressActivity>(`${this.base}/week`, request);
  }

  review(seqtsweek: number, request: WeeklyProgressReviewRequest): Observable<WeeklyProgressActivity> {
    return this.http.put<WeeklyProgressActivity>(`${this.base}/week/${seqtsweek}/review`, request);
  }
}
