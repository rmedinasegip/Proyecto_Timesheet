export interface TimesheetView {
  seqts: number;
  memberuser: number;
  memberName: string;
  codecompanyconsultant: string;
  codecompanyconsultantName: string;
  codecustomer: number;
  customerName: string;
  seqproject: number | null;
  projectName: string | null;
  system: string;
  systemName: string;
  module: string;
  moduleName: string;
  sprint: string | null;
  incidentref: string | null;
  activitytype: string;
  activitytypeName: string;
  activitydesc: string;
  tsdate: string;
  starttime: string;
  endtime: string;
  hoursconsumed: number;
  status: string;
  statusName: string;
  reviewedby: number | null;
  reviewedbyName: string | null;
  seqschedule: number | null;
}

export interface TimesheetRequest {
  memberuser: number | null;
  codecompanyconsultant: string | null;
  codecustomer: number | null;
  seqproject: number | null;
  system: string | null;
  module: string | null;
  sprint: string | null;
  incidentref: string | null;
  activitytype: string | null;
  activitydesc: string | null;
  tsdate: string | null;
  starttime: string | null;
  endtime: string | null;
  seqschedule: number | null;
}

export interface TimesheetReviewRequest {
  status: 'APR' | 'REC';
  reviewedby: number | null;
}

export interface TimesheetFilters {
  memberuser: number | null;
  dateFrom: string | null;
  dateTo: string | null;
  status: string | null;
  seqproject: number | null;
}
