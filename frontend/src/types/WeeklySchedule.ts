export interface WeeklyScheduleRequest {
  templateId?: number;
  templateName: string;
  weekStartDate: string;
  createdByUserId: number;
  courseAssignments: CourseAssignmentRequest[];
  schedulingResult?: SchedulingResult;
}

export interface CourseAssignmentRequest {
  courseId: number;
  priority: number; // 1 = highest priority
  studentCount: number;
  preferredTimeStart?: string;
  preferredTimeEnd?: string;
  preferredDays?: string; // e.g., "1,3,5" for Mon,Wed,Fri
}

export interface SchedulingResult {
  templateId: number;
  totalCourses: number;
  scheduledCourses: number;
  failedCourses: number;
  success: boolean;
  scheduledEvents: ScheduledEvent[];
  conflicts: SchedulingConflict[];
}

export interface ScheduledEvent {
  assignment: any;
  teacher: any;
  room: any;
  startDateTime: string;
  endDateTime: string;
}

export interface SchedulingConflict {
  assignment: any;
  reason: string;
}
