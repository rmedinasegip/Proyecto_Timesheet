import { ChangeView, NewsView, RiskView } from '../../projects/models/project.model';

export interface PhaseProgressRow {
  seqschedule: number | null;
  description: string;
  contractedDays: number | null;
  addendumDays: number | null;
  totalProjectDays: number | null;
  expectedAdvanceDays: number | null;
  realAdvanceDays: number | null;
  daysToInvest: number | null;
  advanceVariationPerc: number | null;
  daysConsumedTs: number | null;
  balanceDaysTs: number | null;
  plannedAssignmentVariationPerc: number | null;
  currentAdvancePerc: number | null;
  realAdvancePerc: number | null;
  effectivenessPerc: number | null;
  total: boolean;
}

export interface MilestoneRow {
  seqschedule: number;
  parentSeqschedule: number | null;
  parentDescription: string | null;
  description: string;
  memberName: string | null;
  baseDays: number | null;
  addendumDays: number | null;
  totalDays: number | null;
  expectedAdvanceDays: number | null;
  realAdvanceDays: number | null;
  plannedAssignmentVariationPerc: number | null;
  realAdvancePerc: number | null;
  effectivenessPerc: number | null;
  daysConsumedTs: number | null;
  baseStartDate: string | null;
  baseEndDate: string | null;
}

export interface ProjectProgressReport {
  seqproject: number;
  projectName: string;
  customerName: string;
  statusName: string;
  leaderName: string;
  startDate: string;
  reportDate: string;

  plannedStartDate: string;
  plannedEndDate: string;
  realStartDate: string | null;
  realEndDate: string | null;

  phases: PhaseProgressRow[];
  milestones: MilestoneRow[];

  risks: RiskView[];
  news: NewsView[];
  changes: ChangeView[];
}
