import { Component, OnInit } from '@angular/core';
import { ConfirmationService, MessageService } from 'primeng/api';
import { ChangeRequest, ChangeView, ProjectListItem } from '../../projects/models/project.model';
import { ProjectService } from '../../projects/services/project.service';
import { ProjectProgressReport } from '../models/progress.model';
import { ProgressService } from '../services/progress.service';

function emptyChangeRequest(): ChangeRequest {
  return {
    changedate: null, phase: null, deliverable: null, reason: null, consequence: null,
    approvedby: null, company: null, daysvariation: null, plannedapplydate: null
  };
}

@Component({
  selector: 'app-progress-report',
  templateUrl: './progress-report.component.html',
  styleUrls: ['./progress-report.component.css'],
  providers: [ConfirmationService, MessageService]
})
export class ProgressReportComponent implements OnInit {
  projects: ProjectListItem[] = [];
  seqproject: number | null = null;
  cutoffDate: string | null = null;

  report: ProjectProgressReport | null = null;
  loading = false;
  exporting = false;

  newChange: ChangeRequest = emptyChangeRequest();
  editingChangeSeq: number | null = null;
  editingChangeForm: ChangeRequest = emptyChangeRequest();

  constructor(
    private projectService: ProjectService,
    private progressService: ProgressService,
    private messageService: MessageService,
    private confirmationService: ConfirmationService
  ) {}

  ngOnInit(): void {
    this.projectService.list().subscribe((data) => {
      this.projects = data;
      if (data.length > 0 && !this.seqproject) {
        this.seqproject = data[0].seq;
        this.loadReport();
      }
    });
  }

  onProjectChange(): void {
    this.loadReport();
  }

  onCutoffChange(): void {
    this.loadReport();
  }

  loadReport(): void {
    if (!this.seqproject) return;
    this.loading = true;
    this.progressService.getReport(this.seqproject, this.cutoffDate).subscribe({
      next: (report) => {
        this.report = report;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
        this.messageService.add({ severity: 'error', summary: 'Error', detail: 'No se pudo cargar el reporte de avance' });
      }
    });
  }

  downloadExcel(): void {
    if (!this.seqproject) return;
    this.exporting = true;
    this.progressService.downloadExcel(this.seqproject, this.cutoffDate).subscribe({
      next: (blob) => {
        this.exporting = false;
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = `avance-proyecto-${this.seqproject}.xlsx`;
        link.click();
        window.URL.revokeObjectURL(url);
      },
      error: () => {
        this.exporting = false;
        this.messageService.add({ severity: 'error', summary: 'Error', detail: 'No se pudo descargar el Excel' });
      }
    });
  }

  // ---- Cambios Aprobados ----

  addChange(): void {
    if (!this.seqproject || !this.newChange.changedate || !this.newChange.phase || !this.newChange.deliverable
      || !this.newChange.reason || !this.newChange.approvedby || !this.newChange.company || this.newChange.daysvariation == null) {
      this.messageService.add({ severity: 'warn', summary: 'Datos incompletos', detail: 'Complete todos los campos obligatorios' });
      return;
    }
    this.projectService.createChange(this.seqproject, this.newChange).subscribe(() => {
      this.newChange = emptyChangeRequest();
      this.loadReport();
    });
  }

  startEditChange(row: ChangeView): void {
    this.editingChangeSeq = row.seqchange;
    this.editingChangeForm = {
      changedate: row.changedate, phase: row.phase, deliverable: row.deliverable, reason: row.reason,
      consequence: row.consequence, approvedby: row.approvedby, company: row.company,
      daysvariation: row.daysvariation, plannedapplydate: row.plannedapplydate
    };
  }

  saveEditChange(): void {
    if (!this.seqproject || this.editingChangeSeq == null) return;
    this.projectService.updateChange(this.seqproject, this.editingChangeSeq, this.editingChangeForm).subscribe(() => {
      this.editingChangeSeq = null;
      this.loadReport();
    });
  }

  cancelEditChange(): void {
    this.editingChangeSeq = null;
  }

  deleteChange(row: ChangeView): void {
    if (!this.seqproject) return;
    this.confirmationService.confirm({
      message: '¿Eliminar este cambio aprobado?',
      header: 'Confirmar eliminación',
      acceptLabel: 'Eliminar',
      rejectLabel: 'Cancelar',
      accept: () => {
        this.projectService.deleteChange(this.seqproject!, row.seqchange).subscribe(() => this.loadReport());
      }
    });
  }
}
