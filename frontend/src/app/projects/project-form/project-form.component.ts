import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { ConfirmationService, MessageService } from 'primeng/api';
import {
  CatalogItem,
  Customer,
  HierarchyType,
  NewsRequest,
  NewsView,
  ProgressRequest,
  ProjectDetail,
  ProjectSaveRequest,
  RiskRequest,
  RiskView,
  ScheduleCreateRequest,
  ScheduleUpdateRequest,
  ScheduleView,
  TeamMemberRequest,
  TeamMemberView,
  UserOption
} from '../models/project.model';
import { ProjectService } from '../services/project.service';
import { CatalogService } from '../services/catalog.service';

function emptyProjectForm(): ProjectSaveRequest {
  return {
    contractNumber: null,
    projectCode: '',
    projectName: '',
    projectDescription: '',
    codecustomer: null,
    codeuserPm: null,
    requestDate: null,
    projectDuration: null,
    baseStartDate: null,
    baseEndDate: null,
    plannedStartDate: null,
    plannedEndDate: null,
    realStartDate: null,
    realEndDate: null,
    statuscat: 'PRJ_PROJECTSTATUSCAT',
    status: null
  };
}

function emptyTeamRequest(): TeamMemberRequest {
  return { memberuser: null, projectrol: null, assignmentdate: null };
}

function emptyRiskRequest(): RiskRequest {
  return {
    riskdate: null, risktypeimpact: null, riskdescimpact: null, personincharge: null,
    company: null, solution: null, probabilityperc: null, riskstatus: null
  };
}

function emptyNewsRequest(): NewsRequest {
  return {
    datenewarrival: null, newstypeimpact: null, descriptionnews: null, personreporting: null,
    affectation: null, personincharge: null, company: null, solution: null,
    datesolution: null, daterealsolution: null, newstatus: null
  };
}

function emptyProgressForm(): ProgressRequest {
  return {
    lastcutoffdate: null, advexpectedperc: null, advrealperc: null, advexpecteddays: null,
    advrealdays: null, daysconsumed: null, varadvplannedperc: null, efectivityperc: null
  };
}

function emptyScheduleForm() {
  return {
    hierarchyType: 'PADRE' as HierarchyType,
    referenceSeqschedule: null as number | null,
    shortactivitydesc: '',
    activitydesc: '',
    memberuser: null as number | null,
    basedays: null as number | null,
    baseadicional: null as number | null,
    baseStartDate: null as string | null,
    baseEndDate: null as string | null
  };
}

@Component({
  selector: 'app-project-form',
  templateUrl: './project-form.component.html',
  styleUrls: ['./project-form.component.css'],
  providers: [ConfirmationService]
})
export class ProjectFormComponent implements OnInit {
  @Input() seq: number | null = null;
  @Input() readOnly = false;
  @Output() closed = new EventEmitter<boolean>();

  currentSeq: number | null = null;
  detail: ProjectDetail | null = null;
  savedOnce = false;

  form: ProjectSaveRequest = emptyProjectForm();
  saving = false;

  customers: Customer[] = [];
  users: UserOption[] = [];
  projectStatusItems: CatalogItem[] = [];
  projectRolItems: CatalogItem[] = [];
  riskTypeItems: CatalogItem[] = [];
  riskStatusItems: CatalogItem[] = [];
  newsTypeItems: CatalogItem[] = [];
  newsStatusItems: CatalogItem[] = [];

  team: TeamMemberView[] = [];
  schedule: ScheduleView[] = [];
  risks: RiskView[] = [];
  news: NewsView[] = [];
  scheduleLevels = new Map<number, number>();

  newTeamMember: TeamMemberRequest = emptyTeamRequest();

  progressFormVisible = false;
  progressForm: ProgressRequest = emptyProgressForm();

  scheduleFormVisible = false;
  scheduleForm = emptyScheduleForm();

  editingScheduleSeq: number | null = null;
  editingScheduleForm: ScheduleUpdateRequest = {
    shortactivitydesc: '', activitydesc: '', memberuser: null, basedays: null, baseadicional: null,
    baseStartDate: null, baseEndDate: null
  };

  newRisk: RiskRequest = emptyRiskRequest();
  editingRiskSeq: number | null = null;
  editingRiskForm: RiskRequest = emptyRiskRequest();

  newNews: NewsRequest = emptyNewsRequest();
  editingNewsSeq: number | null = null;
  editingNewsForm: NewsRequest = emptyNewsRequest();

  constructor(
    private projectService: ProjectService,
    private catalogService: CatalogService,
    private messageService: MessageService,
    private confirmationService: ConfirmationService
  ) {}

  ngOnInit(): void {
    this.currentSeq = this.seq;
    this.savedOnce = this.seq != null;

    this.catalogService.getCustomers().subscribe((data) => (this.customers = data));
    this.catalogService.getUsers().subscribe((data) => (this.users = data));
    this.catalogService.getCatalogItems('PRJ_PROJECTSTATUSCAT').subscribe((data) => (this.projectStatusItems = data));
    this.catalogService.getCatalogItems('PRJ_PROJECTROLCAT').subscribe((data) => (this.projectRolItems = data));
    this.catalogService.getCatalogItems('PRJ_RISKTYPEIMPACTCAT').subscribe((data) => (this.riskTypeItems = data));
    this.catalogService.getCatalogItems('PRJ_RISKSTATUSCAT').subscribe((data) => (this.riskStatusItems = data));
    this.catalogService.getCatalogItems('PRJ_NEWTYPEIMPACTCAT').subscribe((data) => (this.newsTypeItems = data));
    this.catalogService.getCatalogItems('PRJ_NEWSTATUSCAT').subscribe((data) => (this.newsStatusItems = data));

    if (this.currentSeq) {
      this.loadProject(this.currentSeq);
    }
  }

  get isEditMode(): boolean {
    return this.savedOnce;
  }

  loadProject(seq: number): void {
    this.projectService.get(seq).subscribe((detail) => {
      this.detail = detail;
      this.form = {
        contractNumber: detail.contractNumber,
        projectCode: detail.projectCode,
        projectName: detail.projectName,
        projectDescription: detail.projectDescription,
        codecustomer: detail.codecustomer,
        codeuserPm: detail.codeuserPm,
        requestDate: detail.requestDate,
        projectDuration: detail.projectDuration,
        baseStartDate: detail.baseStartDate,
        baseEndDate: detail.baseEndDate,
        plannedStartDate: detail.plannedStartDate,
        plannedEndDate: detail.plannedEndDate,
        realStartDate: detail.realStartDate,
        realEndDate: detail.realEndDate,
        statuscat: detail.statuscat,
        status: detail.status
      };
    });
    this.reloadTeam();
    this.reloadSchedule();
    this.reloadRisks();
    this.reloadNews();
  }

  saveHeader(): void {
    this.saving = true;
    if (!this.currentSeq) {
      this.projectService.create(this.form).subscribe({
        next: (detail) => {
          this.saving = false;
          this.currentSeq = detail.seq;
          this.savedOnce = true;
          this.detail = detail;
          this.messageService.add({ severity: 'success', summary: 'Proyecto creado', detail: detail.projectName });
        },
        error: () => {
          this.saving = false;
          this.messageService.add({ severity: 'error', summary: 'Error', detail: 'No se pudo crear el proyecto' });
        }
      });
    } else {
      this.projectService.update(this.currentSeq, this.form).subscribe({
        next: (detail) => {
          this.saving = false;
          this.detail = detail;
          this.messageService.add({ severity: 'success', summary: 'Proyecto actualizado', detail: detail.projectName });
        },
        error: () => {
          this.saving = false;
          this.messageService.add({ severity: 'error', summary: 'Error', detail: 'No se pudo actualizar el proyecto' });
        }
      });
    }
  }

  close(): void {
    this.closed.emit(this.savedOnce);
  }

  // ---- Avance ----

  openProgressForm(): void {
    if (!this.detail) return;
    this.progressForm = {
      lastcutoffdate: this.detail.lastcutoffdate,
      advexpectedperc: this.detail.advexpectedperc,
      advrealperc: this.detail.advrealperc,
      advexpecteddays: this.detail.advexpecteddays,
      advrealdays: this.detail.advrealdays,
      daysconsumed: this.detail.daysconsumed,
      varadvplannedperc: this.detail.varadvplannedperc,
      efectivityperc: this.detail.efectivityperc
    };
    this.progressFormVisible = true;
  }

  submitProgressForm(): void {
    if (!this.currentSeq) return;
    this.projectService.registerProgress(this.currentSeq, this.progressForm).subscribe({
      next: (detail) => {
        this.detail = detail;
        this.progressFormVisible = false;
        this.messageService.add({ severity: 'success', summary: 'Avance registrado', detail: detail.projectName });
      },
      error: () => {
        this.messageService.add({ severity: 'error', summary: 'Error', detail: 'No se pudo registrar el avance' });
      }
    });
  }

  // ---- Equipo de trabajo ----

  reloadTeam(): void {
    if (!this.currentSeq) return;
    this.projectService.listTeam(this.currentSeq).subscribe((data) => (this.team = data));
  }

  addTeamMember(): void {
    if (!this.currentSeq || !this.newTeamMember.memberuser || !this.newTeamMember.projectrol || !this.newTeamMember.assignmentdate) {
      return;
    }
    this.projectService.addTeamMember(this.currentSeq, this.newTeamMember).subscribe(() => {
      this.newTeamMember = emptyTeamRequest();
      this.reloadTeam();
    });
  }

  removeTeamMember(member: TeamMemberView): void {
    if (!this.currentSeq) return;
    this.confirmationService.confirm({
      message: `¿Quitar a ${member.memberName} del proyecto?`,
      header: 'Confirmar',
      acceptLabel: 'Quitar',
      rejectLabel: 'Cancelar',
      accept: () => {
        this.projectService.removeTeamMember(this.currentSeq!, member.seqteam).subscribe(() => this.reloadTeam());
      }
    });
  }

  // ---- Schedule ----

  reloadSchedule(): void {
    if (!this.currentSeq) return;
    this.projectService.listSchedule(this.currentSeq).subscribe((data) => {
      this.schedule = data;
      this.computeScheduleLevels();
    });
  }

  private computeScheduleLevels(): void {
    this.scheduleLevels = new Map<number, number>();
    const bySeq = new Map<number, ScheduleView>();
    this.schedule.forEach((s) => bySeq.set(s.seqschedule, s));
    const levelOf = (row: ScheduleView): number => {
      if (this.scheduleLevels.has(row.seqschedule)) {
        return this.scheduleLevels.get(row.seqschedule)!;
      }
      let level = 0;
      let parentSeq = row.seqscheduleparent;
      const seen = new Set<number>();
      while (parentSeq != null && bySeq.has(parentSeq) && !seen.has(parentSeq)) {
        level++;
        seen.add(parentSeq);
        parentSeq = bySeq.get(parentSeq)!.seqscheduleparent;
      }
      this.scheduleLevels.set(row.seqschedule, level);
      return level;
    };
    this.schedule.forEach((row) => levelOf(row));
  }

  indentFor(row: ScheduleView): string {
    return (this.scheduleLevels.get(row.seqschedule) || 0) * 1.5 + 'rem';
  }

  openScheduleForm(): void {
    this.scheduleForm = emptyScheduleForm();
    this.scheduleFormVisible = true;
  }

  submitScheduleForm(): void {
    if (!this.currentSeq || !this.scheduleForm.shortactivitydesc || !this.scheduleForm.activitydesc) {
      return;
    }
    const needsReference = this.scheduleForm.hierarchyType !== 'PADRE';
    if (needsReference && !this.scheduleForm.referenceSeqschedule) {
      this.messageService.add({ severity: 'warn', summary: 'Falta la actividad de referencia', detail: 'Seleccione la actividad de referencia' });
      return;
    }
    const request: ScheduleCreateRequest = { ...this.scheduleForm };
    this.projectService.createSchedule(this.currentSeq, request).subscribe(() => {
      this.scheduleFormVisible = false;
      this.reloadSchedule();
    });
  }

  startEditSchedule(row: ScheduleView): void {
    this.editingScheduleSeq = row.seqschedule;
    this.editingScheduleForm = {
      shortactivitydesc: row.shortactivitydesc,
      activitydesc: row.activitydesc,
      memberuser: row.memberuser,
      basedays: row.basedays,
      baseadicional: row.baseadicional,
      baseStartDate: row.baseStartDate,
      baseEndDate: row.baseEndDate
    };
  }

  saveEditSchedule(): void {
    if (!this.currentSeq || this.editingScheduleSeq == null) return;
    this.projectService.updateSchedule(this.currentSeq, this.editingScheduleSeq, this.editingScheduleForm).subscribe(() => {
      this.editingScheduleSeq = null;
      this.reloadSchedule();
    });
  }

  cancelEditSchedule(): void {
    this.editingScheduleSeq = null;
  }

  deleteSchedule(row: ScheduleView): void {
    if (!this.currentSeq) return;
    this.confirmationService.confirm({
      message: `¿Eliminar la actividad "${row.shortactivitydesc}"?`,
      header: 'Confirmar eliminación',
      acceptLabel: 'Eliminar',
      rejectLabel: 'Cancelar',
      accept: () => {
        this.projectService.deleteSchedule(this.currentSeq!, row.seqschedule).subscribe({
          next: () => this.reloadSchedule(),
          error: (err) => {
            const detail = err?.error?.message || 'No se pudo eliminar la actividad';
            this.messageService.add({ severity: 'error', summary: 'Error', detail });
          }
        });
      }
    });
  }

  // ---- Riesgos ----

  reloadRisks(): void {
    if (!this.currentSeq) return;
    this.projectService.listRisks(this.currentSeq).subscribe((data) => (this.risks = data));
  }

  addRisk(): void {
    if (!this.currentSeq || !this.newRisk.riskdate || !this.newRisk.risktypeimpact || !this.newRisk.riskdescimpact) {
      return;
    }
    this.projectService.createRisk(this.currentSeq, this.newRisk).subscribe(() => {
      this.newRisk = emptyRiskRequest();
      this.reloadRisks();
    });
  }

  startEditRisk(row: RiskView): void {
    this.editingRiskSeq = row.seqrisk;
    this.editingRiskForm = {
      riskdate: row.riskdate, risktypeimpact: row.risktypeimpact, riskdescimpact: row.riskdescimpact,
      personincharge: row.personincharge, company: row.company, solution: row.solution,
      probabilityperc: row.probabilityperc, riskstatus: row.riskstatus
    };
  }

  saveEditRisk(): void {
    if (!this.currentSeq || this.editingRiskSeq == null) return;
    this.projectService.updateRisk(this.currentSeq, this.editingRiskSeq, this.editingRiskForm).subscribe(() => {
      this.editingRiskSeq = null;
      this.reloadRisks();
    });
  }

  cancelEditRisk(): void {
    this.editingRiskSeq = null;
  }

  deleteRisk(row: RiskView): void {
    if (!this.currentSeq) return;
    this.confirmationService.confirm({
      message: '¿Eliminar este riesgo?',
      header: 'Confirmar eliminación',
      acceptLabel: 'Eliminar',
      rejectLabel: 'Cancelar',
      accept: () => {
        this.projectService.deleteRisk(this.currentSeq!, row.seqrisk).subscribe(() => this.reloadRisks());
      }
    });
  }

  // ---- Novedades ----

  reloadNews(): void {
    if (!this.currentSeq) return;
    this.projectService.listNews(this.currentSeq).subscribe((data) => (this.news = data));
  }

  addNews(): void {
    if (!this.currentSeq || !this.newNews.datenewarrival || !this.newNews.newstypeimpact || !this.newNews.descriptionnews) {
      return;
    }
    this.projectService.createNews(this.currentSeq, this.newNews).subscribe(() => {
      this.newNews = emptyNewsRequest();
      this.reloadNews();
    });
  }

  startEditNews(row: NewsView): void {
    this.editingNewsSeq = row.seqNews;
    this.editingNewsForm = {
      datenewarrival: row.datenewarrival, newstypeimpact: row.newstypeimpact, descriptionnews: row.descriptionnews,
      personreporting: row.personreporting, affectation: row.affectation, personincharge: row.personincharge,
      company: row.company, solution: row.solution, datesolution: row.datesolution,
      daterealsolution: row.daterealsolution, newstatus: row.newstatus
    };
  }

  saveEditNews(): void {
    if (!this.currentSeq || this.editingNewsSeq == null) return;
    this.projectService.updateNews(this.currentSeq, this.editingNewsSeq, this.editingNewsForm).subscribe(() => {
      this.editingNewsSeq = null;
      this.reloadNews();
    });
  }

  cancelEditNews(): void {
    this.editingNewsSeq = null;
  }

  deleteNews(row: NewsView): void {
    if (!this.currentSeq) return;
    this.confirmationService.confirm({
      message: '¿Eliminar esta novedad?',
      header: 'Confirmar eliminación',
      acceptLabel: 'Eliminar',
      rejectLabel: 'Cancelar',
      accept: () => {
        this.projectService.deleteNews(this.currentSeq!, row.seqNews).subscribe(() => this.reloadNews());
      }
    });
  }
}
