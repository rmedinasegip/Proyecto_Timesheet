export interface Customer {
  code: number;
  name: string;
}

export interface Company {
  id: { codeinstance: string; code: string };
  name: string;
}

export interface UserOption {
  code: number;
  name: string;
}

export interface CatalogItem {
  id: { codeinstance: string; codecat: string; codeitem: string };
  name: string;
}

export interface ProjectListItem {
  seq: number;
  requestDate: string;
  projectCode: string;
  projectName: string;
  customerName: string;
  codeuserPm: number;
  pmName: string;
  baseDurationDays: number;
  baseStartDate: string;
  baseEndDate: string;
  plannedDurationDays: number;
  plannedStartDate: string;
  plannedEndDate: string;
  statusName: string;
}

export interface ProjectDetail {
  seq: number;
  contractNumber: string | null;
  projectCode: string;
  projectName: string;
  projectDescription: string;
  codecustomer: number;
  customerName: string;
  requestDate: string;
  codeuserPm: number;
  pmName: string;

  projectDuration: number;
  baseStartDate: string;
  baseEndDate: string;
  plannedStartDate: string;
  plannedEndDate: string;

  realStartDate: string | null;
  realEndDate: string | null;
  statuscat: string;
  status: string;
  statusName: string;

  lastcutoffdate: string | null;
  advexpectedperc: number | null;
  advrealperc: number | null;
  advexpecteddays: number | null;
  advrealdays: number | null;
  daysconsumed: number | null;
  varadvplannedperc: number | null;
  efectivityperc: number | null;

  usercreate: number;
  usercreateName: string;
  datecreate: string;
  userlastmodify: number;
  userlastmodifyName: string;
  datemodify: string;
}

export interface ProjectSaveRequest {
  contractNumber: string | null;
  projectCode: string;
  projectName: string;
  projectDescription: string;
  codecustomer: number | null;
  codeuserPm: number | null;
  requestDate: string | null;

  projectDuration: number | null;
  baseStartDate: string | null;
  baseEndDate: string | null;
  plannedStartDate: string | null;
  plannedEndDate: string | null;

  realStartDate: string | null;
  realEndDate: string | null;
  statuscat: string | null;
  status: string | null;
}

export interface ProgressRequest {
  lastcutoffdate: string | null;
  advexpectedperc: number | null;
  advrealperc: number | null;
  advexpecteddays: number | null;
  advrealdays: number | null;
  daysconsumed: number | null;
  varadvplannedperc: number | null;
  efectivityperc: number | null;
}

export interface TeamMemberView {
  seqteam: number;
  memberuser: number;
  memberName: string;
  projectrol: string;
  assignmentdate: string;
}

export interface TeamMemberRequest {
  memberuser: number | null;
  projectrol: string | null;
  assignmentdate: string | null;
}

export type HierarchyType = 'PADRE' | 'HERMANO' | 'HIJO';

export interface ScheduleView {
  seqschedule: number;
  seqscheduleparent: number | null;
  shortactivitydesc: string;
  activitydesc: string;
  memberuser: number | null;
  memberName: string | null;
  basedays: number | null;
  baseadicional: number | null;
  basedaystotal: number | null;
  baseStartDate: string | null;
  baseEndDate: string | null;
  efectivityperc: number | null;
  advrealperc: number | null;
  padre: boolean;
}

export interface ScheduleCreateRequest {
  hierarchyType: HierarchyType;
  referenceSeqschedule: number | null;
  shortactivitydesc: string;
  activitydesc: string;
  memberuser: number | null;
  basedays: number | null;
  baseadicional: number | null;
  baseStartDate: string | null;
  baseEndDate: string | null;
}

export interface ScheduleUpdateRequest {
  shortactivitydesc: string;
  activitydesc: string;
  memberuser: number | null;
  basedays: number | null;
  baseadicional: number | null;
  baseStartDate: string | null;
  baseEndDate: string | null;
}

export interface RiskView {
  seqrisk: number;
  riskdate: string;
  risktypeimpactcat: string;
  risktypeimpact: string;
  risktypeimpactName: string;
  riskdescimpact: string;
  personincharge: string;
  company: string;
  solution: string | null;
  probabilityperc: number;
  riskstatuscat: string;
  riskstatus: string;
  riskstatusName: string;
}

export interface RiskRequest {
  riskdate: string | null;
  risktypeimpact: string | null;
  riskdescimpact: string | null;
  personincharge: string | null;
  company: string | null;
  solution: string | null;
  probabilityperc: number | null;
  riskstatus: string | null;
}

export interface NewsView {
  seqNews: number;
  datenewarrival: string;
  newstypeimpactcat: string;
  newstypeimpact: string;
  newstypeimpactName: string;
  descriptionnews: string;
  personreporting: string;
  affectation: string;
  personincharge: string;
  company: string;
  solution: string | null;
  datesolution: string | null;
  daterealsolution: string | null;
  newstatuscat: string;
  newstatus: string;
  newstatusName: string;
}

export interface NewsRequest {
  datenewarrival: string | null;
  newstypeimpact: string | null;
  descriptionnews: string | null;
  personreporting: string | null;
  affectation: string | null;
  personincharge: string | null;
  company: string | null;
  solution: string | null;
  datesolution: string | null;
  daterealsolution: string | null;
  newstatus: string | null;
}

export interface ChangeView {
  seqchange: number;
  changedate: string;
  phase: string;
  deliverable: string;
  reason: string;
  consequence: string | null;
  approvedby: string;
  company: string;
  daysvariation: number;
  plannedapplydate: string | null;
}

export interface ChangeRequest {
  changedate: string | null;
  phase: string | null;
  deliverable: string | null;
  reason: string | null;
  consequence: string | null;
  approvedby: string | null;
  company: string | null;
  daysvariation: number | null;
  plannedapplydate: string | null;
}
