export interface WeeklyProgressActivity {
  seqproject: number;
  projectName: string | null;
  seqschedule: number;
  shortactivitydesc: string;
  activitydesc: string;
  memberuser: number | null;
  memberName: string | null;
  currentAdvancePerc: number | null;
  seqtsweek: number | null;
  day1Perc: number | null;
  day2Perc: number | null;
  day3Perc: number | null;
  day4Perc: number | null;
  day5Perc: number | null;
  day6Perc: number | null;
  day7Perc: number | null;
  status: string | null;
  statusName: string | null;
  reviewedby: number | null;
  reviewedbyName: string | null;
}

export interface WeeklyProgressFilters {
  weekStart: string | null;
  seqproject: number | null;
  memberuser: number | null;
  status: string | null;
}

export interface WeeklyProgressWeekRequest {
  seqschedule: number;
  weekStart: string;
  day1Perc: number | null;
  day2Perc: number | null;
  day3Perc: number | null;
  day4Perc: number | null;
  day5Perc: number | null;
  day6Perc: number | null;
  day7Perc: number | null;
}

export interface WeeklyProgressReviewRequest {
  status: 'APR' | 'REC';
}
