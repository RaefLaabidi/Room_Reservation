export interface ScheduleTemplate {
  id?: number;
  name: string;
  weekStartDate: string; // ISO date string
  weekEndDate: string;
  createdBy: {
    id: number;
    name: string;
  };
  status: 'DRAFT' | 'PUBLISHED' | 'ARCHIVED';
  courseAssignments?: TemplateCourseAssignment[];
}

export interface TemplateCourseAssignment {
  id?: number;
  template?: ScheduleTemplate;
  course: {
    id: number;
    name: string;
    subject: string;
    durationHours: number;
    sessionsPerWeek: number;
    minCapacity: number;
  };
  assignedTeacher?: {
    id: number;
    name: string;
    email: string;
  };
  preferredTimeStart?: string;
  preferredTimeEnd?: string;
  preferredDays?: string; // "1,3,5" for Mon,Wed,Fri
  studentCount?: number;
  priority: number;
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
  assignment: TemplateCourseAssignment;
  teacher: {
    id: number;
    name: string;
    email: string;
  };
  room: {
    id: number;
    name: string;
    capacity: number;
    location: string;
  };
  startDateTime: string;
  endDateTime: string;
}

export interface SchedulingConflict {
  assignment: TemplateCourseAssignment;
  reason: string;
}
