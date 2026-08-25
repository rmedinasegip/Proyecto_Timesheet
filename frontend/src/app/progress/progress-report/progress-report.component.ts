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

  // Sección "Cambios aprobados" deshabilitada a pedido del cliente hasta
  // nuevo aviso — el resto de la pantalla no depende de esto. Reactivar
  // cambiando este flag a true.
  changesSectionEnabled = false;

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

  /** Fecha de corte con p-calendar (igual al resto de la app) en vez del
   * `<input type="date">` nativo — cutoffDate sigue siendo la fuente de
   * verdad como string "yyyy-MM-dd" (formato que ya espera ProgressService).
   * cutoffDateValue es una propiedad normal (no un getter): un getter que
   * devuelve `new Date(...)` en cada ciclo de detección de cambios le da a
   * p-calendar una referencia distinta todo el tiempo, y el componente la
   * interpreta como "cambió" y se re-renderiza en loop — eso era lo que
   * colgaba el navegador ("La página no responde") al abrir el calendario.
   * Con `[(ngModel)]` normal, la referencia solo cambia cuando el usuario
   * elige una fecha. */
  cutoffDateValue: Date | null = null;

  onCutoffDateSelect(): void {
    this.cutoffDate = this.cutoffDateValue ? this.formatLocalDate(this.cutoffDateValue) : null;
    this.loadReport();
  }

  /** Fecha de corte solo puede elegirse desde la Fecha Inicio Real del
   * proyecto en adelante — antes de esa fecha el proyecto ni siquiera había
   * arrancado en la práctica. `[minDate]` es un @Input de solo lectura para
   * p-calendar (no un ngModel de ida y vuelta como cutoffDateValue), así
   * que un getter acá es seguro: no dispara el loop de re-render que sí
   * causaba el getter de cutoffDateValue (ver comentario arriba) porque no
   * hay ningún ngModelChange/onSelect que vuelva a escribir sobre este
   * valor. Sin fecha real registrada, no hay piso — se usa una fecha muy
   * antigua (mismo criterio que project-form.component.ts). */
  get minCutoffDate(): Date {
    return this.parseLocalDate(this.report?.realStartDate ?? null) ?? new Date(1900, 0, 1);
  }

  private parseLocalDate(value: string | null): Date | null {
    if (!value) {
      return null;
    }
    const [year, month, day] = value.split('-').map(Number);
    return new Date(year, month - 1, day);
  }

  private formatLocalDate(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
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
