import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { TimesheetFilters, TimesheetRequest, TimesheetReviewRequest, TimesheetView } from '../models/timesheet.model';

@Injectable({ providedIn: 'root' })
export class TimesheetService {
  private base = '/api/timesheets';

  constructor(private http: HttpClient) {}

  search(filters: TimesheetFilters): Observable<TimesheetView[]> {
    let params = new HttpParams();
    if (filters.memberuser != null) params = params.set('memberuser', filters.memberuser);
    if (filters.dateFrom) params = params.set('dateFrom', filters.dateFrom);
    if (filters.dateTo) params = params.set('dateTo', filters.dateTo);
    if (filters.status) params = params.set('status', filters.status);
    if (filters.seqproject != null) params = params.set('seqproject', filters.seqproject);
    return this.http.get<TimesheetView[]>(this.base, { params });
  }

  create(request: TimesheetRequest): Observable<TimesheetView> {
    return this.http.post<TimesheetView>(this.base, request);
  }

  update(seqts: number, request: TimesheetRequest): Observable<TimesheetView> {
    return this.http.put<TimesheetView>(`${this.base}/${seqts}`, request);
  }

  review(seqts: number, request: TimesheetReviewRequest): Observable<TimesheetView> {
    return this.http.put<TimesheetView>(`${this.base}/${seqts}/review`, request);
  }

  delete(seqts: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${seqts}`);
  }
}
