import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ProjectListComponent } from './projects/project-list/project-list.component';
import { TimesheetListComponent } from './timesheet/timesheet-list/timesheet-list.component';
import { ProgressReportComponent } from './progress/progress-report/progress-report.component';
import { WeeklyProgressListComponent } from './weekly-progress/weekly-progress-list/weekly-progress-list.component';
import { LoginComponent } from './auth/login/login.component';
import { AuthGuard } from './auth/guards/auth.guard';

const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'projects' },
  { path: 'login', component: LoginComponent },
  { path: 'projects', component: ProjectListComponent, canActivate: [AuthGuard] },
  { path: 'timesheets', component: TimesheetListComponent, canActivate: [AuthGuard] },
  { path: 'progress', component: ProgressReportComponent, canActivate: [AuthGuard] },
  { path: 'weekly-progress', component: WeeklyProgressListComponent, canActivate: [AuthGuard] }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
