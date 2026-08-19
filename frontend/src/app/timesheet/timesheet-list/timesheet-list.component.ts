import { Component, OnInit } from '@angular/core';
import { ConfirmationService, MessageService } from 'primeng/api';
import { CatalogItem, Company, Customer, UserOption } from '../../projects/models/project.model';
import { CatalogService } from '../../projects/services/catalog.service';
import { ProjectListItem } from '../../projects/models/project.model';
import { ProjectService } from '../../projects/services/project.service';
import { TimesheetFilters, TimesheetRequest, TimesheetView } from '../models/timesheet.model';
import { TimesheetService } from '../services/timesheet.service';
import { AuthService } from '../../auth/services/auth.service';

function emptyFilters(): TimesheetFilters {
  return { memberuser: null, dateFrom: null, dateTo: null, status: null, seqproject: null };
}

function emptyRequest(): TimesheetRequest {
  return {
    memberuser: null, codecompanyconsultant: null, codecustomer: null, seqproject: null,
    system: null, module: null, sprint: null, incidentref: null, activitytype: null,
    activitydesc: null, tsdate: null, starttime: null, endtime: null, seqschedule: null
  };
}

@Component({
  selector: 'app-timesheet-list',
  templateUrl: './timesheet-list.component.html',
  styleUrls: ['./timesheet-list.component.css'],
  providers: [ConfirmationService, MessageService]
})
export class TimesheetListComponent implements OnInit {
  entries: TimesheetView[] = [];
  loading = false;

  filters: TimesheetFilters = emptyFilters();

  users: UserOption[] = [];
  customers: Customer[] = [];
  companies: Company[] = [];
  projects: ProjectListItem[] = [];
  systemItems: CatalogItem[] = [];
  moduleItems: CatalogItem[] = [];
  activityTypeItems: CatalogItem[] = [];
  statusItems: CatalogItem[] = [];

  formVisible = false;
  editingSeqts: number | null = null;
  form: TimesheetRequest = emptyRequest();

  constructor(
    private timesheetService: TimesheetService,
    private catalogService: CatalogService,
    private projectService: ProjectService,
    private confirmationService: ConfirmationService,
    private messageService: MessageService,
    public authService: AuthService
  ) {}

  ngOnInit(): void {
    this.catalogService.getUsers().subscribe((data) => (this.users = data));
    this.catalogService.getCustomers().subscribe((data) => (this.customers = data));
    this.catalogService.getCompanies().subscribe((data) => (this.companies = data));
    this.projectService.list().subscribe((data) => (this.projects = data));
    this.catalogService.getCatalogItems('PRJ_SYSTEMSCAT').subscribe((data) => (this.systemItems = data));
    this.catalogService.getCatalogItems('PRJ_MODULECAT').subscribe((data) => (this.moduleItems = data));
    this.catalogService.getCatalogItems('PRJ_ACTIVITYTYTYPECAT').subscribe((data) => (this.activityTypeItems = data));
    this.catalogService.getCatalogItems('PRJ_TIMESHEETSTATUSCAT').subscribe((data) => (this.statusItems = data));
    this.reload();
  }

  reload(): void {
    this.loading = true;
    this.timesheetService.search(this.filters).subscribe({
      next: (data) => {
        this.entries = data;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
        this.messageService.add({ severity: 'error', summary: 'Error', detail: 'No se pudo cargar el registro de timesheets' });
      }
    });
  }

  clearFilters(): void {
    this.filters = emptyFilters();
    this.reload();
  }

  get totalHours(): number {
    return this.entries.reduce((sum, e) => sum + Number(e.hoursconsumed || 0), 0);
  }

  openNew(): void {
    this.editingSeqts = null;
    this.form = emptyRequest();
    this.formVisible = true;
  }

  openEdit(entry: TimesheetView): void {
    this.editingSeqts = entry.seqts;
    this.form = {
      memberuser: entry.memberuser,
      codecompanyconsultant: entry.codecompanyconsultant,
      codecustomer: entry.codecustomer,
      seqproject: entry.seqproject,
      system: entry.system,
      module: entry.module,
      sprint: entry.sprint,
      incidentref: entry.incidentref,
      activitytype: entry.activitytype,
      activitydesc: entry.activitydesc,
      tsdate: entry.tsdate,
      starttime: entry.starttime,
      endtime: entry.endtime,
      seqschedule: entry.seqschedule
    };
    this.formVisible = true;
  }

  submitForm(): void {
    const required = this.form.memberuser && this.form.codecompanyconsultant && this.form.codecustomer
      && this.form.system && this.form.module && this.form.activitytype && this.form.activitydesc
      && this.form.tsdate && this.form.starttime && this.form.endtime;
    if (!required) {
      this.messageService.add({ severity: 'warn', summary: 'Datos incompletos', detail: 'Complete todos los campos obligatorios' });
      return;
    }
    const request$ = this.editingSeqts
      ? this.timesheetService.update(this.editingSeqts, this.form)
      : this.timesheetService.create(this.form);

    request$.subscribe({
      next: () => {
        this.formVisible = false;
        this.reload();
      },
      error: (err) => {
        const detail = err?.error?.message || 'No se pudo guardar el registro';
        this.messageService.add({ severity: 'error', summary: 'Error', detail });
      }
    });
  }

  confirmDelete(entry: TimesheetView): void {
    this.confirmationService.confirm({
      message: `¿Eliminar el registro del ${entry.tsdate} (${entry.memberName})?`,
      header: 'Confirmar eliminación',
      acceptLabel: 'Eliminar',
      rejectLabel: 'Cancelar',
      accept: () => {
        this.timesheetService.delete(entry.seqts).subscribe(() => this.reload());
      }
    });
  }

  get canReview(): boolean {
    return this.authService.currentUser?.role === 'AUT';
  }

  approve(entry: TimesheetView): void {
    const reviewedby = this.authService.currentUser?.code ?? null;
    this.timesheetService.review(entry.seqts, { status: 'APR', reviewedby }).subscribe(() => this.reload());
  }

  reject(entry: TimesheetView): void {
    const reviewedby = this.authService.currentUser?.code ?? null;
    this.timesheetService.review(entry.seqts, { status: 'REC', reviewedby }).subscribe(() => this.reload());
  }
}
