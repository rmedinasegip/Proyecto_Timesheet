import { Component, OnInit } from '@angular/core';
import { MessageService } from 'primeng/api';
import { CatalogItem, ProjectListItem, UserOption } from '../../projects/models/project.model';
import { CatalogService } from '../../projects/services/catalog.service';
import { ProjectService } from '../../projects/services/project.service';
import { AuthService } from '../../auth/services/auth.service';
import { WeeklyProgressActivity, WeeklyProgressFilters } from '../models/weekly-progress.model';
import { WeeklyProgressService } from '../services/weekly-progress.service';

interface DayEditForm {
  day1Perc: number | null;
  day2Perc: number | null;
  day3Perc: number | null;
  day4Perc: number | null;
  day5Perc: number | null;
  day6Perc: number | null;
  day7Perc: number | null;
}

function emptyEditForm(): DayEditForm {
  return { day1Perc: null, day2Perc: null, day3Perc: null, day4Perc: null, day5Perc: null, day6Perc: null, day7Perc: null };
}

function toIsoDate(date: Date): string {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}

function mondayOf(date: Date): Date {
  const result = new Date(date.getFullYear(), date.getMonth(), date.getDate());
  const dow = result.getDay();
  const diffToMonday = dow === 0 ? -6 : 1 - dow;
  result.setDate(result.getDate() + diffToMonday);
  return result;
}

const WEEKDAY_LABELS = ['Lun', 'Mar', 'Mié', 'Jue', 'Vie', 'Sáb', 'Dom'];

@Component({
  selector: 'app-weekly-progress-list',
  templateUrl: './weekly-progress-list.component.html',
  styleUrls: ['./weekly-progress-list.component.css'],
  providers: [MessageService]
})
export class WeeklyProgressListComponent implements OnInit {
  activities: WeeklyProgressActivity[] = [];
  loading = false;

  weekAnchor: Date = new Date();
  weekMonday: Date = mondayOf(new Date());
  weekSunday: Date = new Date();

  seqprojectFilter: number | null = null;
  memberuserFilter: number | null = null;
  statusFilter: string | null = null;

  projects: ProjectListItem[] = [];
  users: UserOption[] = [];
  statusItems: CatalogItem[] = [];

  editingSeqschedule: number | null = null;
  editForm: DayEditForm = emptyEditForm();

  readonly weekdayLabels = WEEKDAY_LABELS;

  constructor(
    private weeklyProgressService: WeeklyProgressService,
    private catalogService: CatalogService,
    private projectService: ProjectService,
    private messageService: MessageService,
    public authService: AuthService
  ) {}

  ngOnInit(): void {
    this.recomputeWeek();
    this.projectService.list().subscribe((data) => (this.projects = data));
    this.catalogService.getCatalogItems('PRJ_TSWEEKSTATUSCAT').subscribe((data) => (this.statusItems = data));
    if (this.canReview) {
      this.catalogService.getUsers().subscribe((data) => (this.users = data));
    }
    this.reload();
  }

  get canReview(): boolean {
    return this.authService.currentUser?.role === 'AUT';
  }

  get weekLabel(): string {
    const fmt = (d: Date) => d.toLocaleDateString('es-EC', { day: '2-digit', month: 'short' });
    return `${fmt(this.weekMonday)} – ${fmt(this.weekSunday)}`;
  }

  get weekBadgeLabel(): string {
    const fmt = (d: Date) => d.toLocaleDateString('es-EC', { day: '2-digit', month: 'short' }).toUpperCase();
    return `${fmt(this.weekMonday)} → ${fmt(this.weekSunday)} ${this.weekSunday.getFullYear()}`;
  }

  get weekDays(): { dow: string; dateLabel: string; isToday: boolean }[] {
    const todayIso = toIsoDate(new Date());
    return this.weekdayLabels.map((dow, i) => {
      const d = new Date(this.weekMonday);
      d.setDate(this.weekMonday.getDate() + i);
      return { dow, dateLabel: String(d.getDate()).padStart(2, '0'), isToday: toIsoDate(d) === todayIso };
    });
  }

  onWeekAnchorChange(): void {
    this.recomputeWeek();
    this.reload();
  }

  private recomputeWeek(): void {
    this.weekMonday = mondayOf(this.weekAnchor);
    this.weekSunday = new Date(this.weekMonday);
    this.weekSunday.setDate(this.weekMonday.getDate() + 6);
  }

  reload(): void {
    this.loading = true;
    const filters: WeeklyProgressFilters = {
      weekStart: toIsoDate(this.weekMonday),
      seqproject: this.seqprojectFilter,
      memberuser: this.canReview ? this.memberuserFilter : null,
      status: this.statusFilter
    };
    this.weeklyProgressService.listActivities(filters).subscribe({
      next: (data) => {
        this.activities = data;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
        this.messageService.add({ severity: 'error', summary: 'Error', detail: 'No se pudo cargar el avance semanal' });
      }
    });
  }

  clearFilters(): void {
    this.seqprojectFilter = null;
    this.memberuserFilter = null;
    this.statusFilter = null;
    this.reload();
  }

  isOwnRow(row: WeeklyProgressActivity): boolean {
    return row.memberuser != null && row.memberuser === this.authService.currentUser?.code;
  }

  startEdit(row: WeeklyProgressActivity): void {
    this.editingSeqschedule = row.seqschedule;
    this.editForm = {
      day1Perc: row.day1Perc,
      day2Perc: row.day2Perc,
      day3Perc: row.day3Perc,
      day4Perc: row.day4Perc,
      day5Perc: row.day5Perc,
      day6Perc: row.day6Perc,
      day7Perc: row.day7Perc
    };
  }

  cancelEdit(): void {
    this.editingSeqschedule = null;
  }

  saveEdit(row: WeeklyProgressActivity): void {
    this.weeklyProgressService.saveWeek({
      seqschedule: row.seqschedule,
      weekStart: toIsoDate(this.weekMonday),
      ...this.editForm
    }).subscribe({
      next: () => {
        this.editingSeqschedule = null;
        this.reload();
      },
      error: (err) => {
        const detail = err?.error?.message || 'No se pudo guardar el avance de la semana';
        this.messageService.add({ severity: 'error', summary: 'Error', detail });
      }
    });
  }

  approve(row: WeeklyProgressActivity): void {
    if (row.seqtsweek == null) {
      return;
    }
    this.weeklyProgressService.review(row.seqtsweek, { status: 'APR' }).subscribe(() => this.reload());
  }

  reject(row: WeeklyProgressActivity): void {
    if (row.seqtsweek == null) {
      return;
    }
    this.weeklyProgressService.review(row.seqtsweek, { status: 'REC' }).subscribe(() => this.reload());
  }
}
