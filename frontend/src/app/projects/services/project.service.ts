import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  ChangeRequest,
  ChangeView,
  NewsRequest,
  NewsView,
  ProgressRequest,
  ProjectDetail,
  ProjectListItem,
  ProjectSaveRequest,
  RiskRequest,
  RiskView,
  ScheduleCreateRequest,
  ScheduleUpdateRequest,
  ScheduleView,
  TeamMemberRequest,
  TeamMemberView
} from '../models/project.model';

@Injectable({ providedIn: 'root' })
export class ProjectService {
  private base = '/api/projects';

  constructor(private http: HttpClient) {}

  list(): Observable<ProjectListItem[]> {
    return this.http.get<ProjectListItem[]>(this.base);
  }

  get(seq: number): Observable<ProjectDetail> {
    return this.http.get<ProjectDetail>(`${this.base}/${seq}`);
  }

  create(request: ProjectSaveRequest): Observable<ProjectDetail> {
    return this.http.post<ProjectDetail>(this.base, request);
  }

  update(seq: number, request: ProjectSaveRequest): Observable<ProjectDetail> {
    return this.http.put<ProjectDetail>(`${this.base}/${seq}`, request);
  }

  delete(seq: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${seq}`);
  }

  registerProgress(seq: number, request: ProgressRequest): Observable<ProjectDetail> {
    return this.http.put<ProjectDetail>(`${this.base}/${seq}/progress`, request);
  }

  // Equipo
  listTeam(seq: number): Observable<TeamMemberView[]> {
    return this.http.get<TeamMemberView[]>(`${this.base}/${seq}/team`);
  }

  addTeamMember(seq: number, request: TeamMemberRequest): Observable<TeamMemberView> {
    return this.http.post<TeamMemberView>(`${this.base}/${seq}/team`, request);
  }

  removeTeamMember(seq: number, seqteam: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${seq}/team/${seqteam}`);
  }

  // Schedule
  listSchedule(seq: number): Observable<ScheduleView[]> {
    return this.http.get<ScheduleView[]>(`${this.base}/${seq}/schedule`);
  }

  createSchedule(seq: number, request: ScheduleCreateRequest): Observable<ScheduleView> {
    return this.http.post<ScheduleView>(`${this.base}/${seq}/schedule`, request);
  }

  updateSchedule(seq: number, seqschedule: number, request: ScheduleUpdateRequest): Observable<ScheduleView> {
    return this.http.put<ScheduleView>(`${this.base}/${seq}/schedule/${seqschedule}`, request);
  }

  deleteSchedule(seq: number, seqschedule: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${seq}/schedule/${seqschedule}`);
  }

  // Riesgos
  listRisks(seq: number): Observable<RiskView[]> {
    return this.http.get<RiskView[]>(`${this.base}/${seq}/risks`);
  }

  createRisk(seq: number, request: RiskRequest): Observable<RiskView> {
    return this.http.post<RiskView>(`${this.base}/${seq}/risks`, request);
  }

  updateRisk(seq: number, seqrisk: number, request: RiskRequest): Observable<RiskView> {
    return this.http.put<RiskView>(`${this.base}/${seq}/risks/${seqrisk}`, request);
  }

  deleteRisk(seq: number, seqrisk: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${seq}/risks/${seqrisk}`);
  }

  // Novedades
  listNews(seq: number): Observable<NewsView[]> {
    return this.http.get<NewsView[]>(`${this.base}/${seq}/news`);
  }

  createNews(seq: number, request: NewsRequest): Observable<NewsView> {
    return this.http.post<NewsView>(`${this.base}/${seq}/news`, request);
  }

  updateNews(seq: number, seqNews: number, request: NewsRequest): Observable<NewsView> {
    return this.http.put<NewsView>(`${this.base}/${seq}/news/${seqNews}`, request);
  }

  deleteNews(seq: number, seqNews: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${seq}/news/${seqNews}`);
  }

  // Cambios Aprobados
  listChanges(seq: number): Observable<ChangeView[]> {
    return this.http.get<ChangeView[]>(`${this.base}/${seq}/changes`);
  }

  createChange(seq: number, request: ChangeRequest): Observable<ChangeView> {
    return this.http.post<ChangeView>(`${this.base}/${seq}/changes`, request);
  }

  updateChange(seq: number, seqchange: number, request: ChangeRequest): Observable<ChangeView> {
    return this.http.put<ChangeView>(`${this.base}/${seq}/changes/${seqchange}`, request);
  }

  deleteChange(seq: number, seqchange: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${seq}/changes/${seqchange}`);
  }
}
