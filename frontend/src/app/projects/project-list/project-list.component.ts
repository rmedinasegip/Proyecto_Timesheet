import { Component, OnInit } from '@angular/core';
import { ConfirmationService, MessageService } from 'primeng/api';
import { ProjectListItem } from '../models/project.model';
import { ProjectService } from '../services/project.service';

@Component({
  selector: 'app-project-list',
  templateUrl: './project-list.component.html',
  styleUrls: ['./project-list.component.css'],
  providers: [ConfirmationService, MessageService]
})
export class ProjectListComponent implements OnInit {
  projects: ProjectListItem[] = [];
  loading = false;

  showForm = false;
  selectedSeq: number | null = null;

  constructor(
    private projectService: ProjectService,
    private confirmationService: ConfirmationService,
    private messageService: MessageService
  ) {}

  ngOnInit(): void {
    this.reload();
  }

  reload(): void {
    this.loading = true;
    this.projectService.list().subscribe({
      next: (data) => {
        this.projects = data;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
        this.messageService.add({ severity: 'error', summary: 'Error', detail: 'No se pudo cargar la lista de proyectos' });
      }
    });
  }

  openNew(): void {
    this.selectedSeq = null;
    this.showForm = true;
  }

  openEdit(project: ProjectListItem): void {
    this.selectedSeq = project.seq;
    this.showForm = true;
  }

  confirmDelete(project: ProjectListItem): void {
    this.confirmationService.confirm({
      message: `¿Eliminar el proyecto "${project.projectName}" (${project.projectCode})? Esta acción no se puede deshacer.`,
      header: 'Confirmar eliminación',
      acceptLabel: 'Eliminar',
      rejectLabel: 'Cancelar',
      accept: () => this.doDelete(project)
    });
  }

  private doDelete(project: ProjectListItem): void {
    this.projectService.delete(project.seq).subscribe({
      next: () => {
        this.messageService.add({ severity: 'success', summary: 'Proyecto eliminado', detail: project.projectName });
        this.reload();
      },
      error: () => {
        this.messageService.add({ severity: 'error', summary: 'Error', detail: 'No se pudo eliminar el proyecto' });
      }
    });
  }

  onFormClosed(saved: boolean): void {
    this.showForm = false;
    this.selectedSeq = null;
    if (saved) {
      this.reload();
    }
  }
}
