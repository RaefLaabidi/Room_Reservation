export interface Course {
  id?: number;
  name: string;
  subject: string;
  durationHours: number;
  sessionsPerWeek: number;
  minCapacity: number;
  maxCapacity?: number;
  preferredRoomType?: string;
  department?: string;
}

export interface TeacherSubject {
  id?: number;
  teacher: {
    id: number;
    name: string;
    email: string;
  };
  subject: string;
  expertiseLevel: number;
}

export interface TeacherAvailability {
  id?: number;
  teacher: {
    id: number;
    name: string;
  };
  dayOfWeek: number; // 1=Monday, 7=Sunday
  startTime: string;
  endTime: string;
  isAvailable: boolean;
}
