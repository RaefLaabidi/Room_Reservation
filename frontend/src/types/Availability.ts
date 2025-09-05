import { User } from './User';

export interface Availability {
  id: number;
  teacher: User;
  availableDate: string; // LocalDate as ISO string
  startTime: string; // LocalTime as string (HH:mm)
  endTime: string; // LocalTime as string (HH:mm)
}

export interface AvailabilityCreateRequest {
  teacherId: number;
  availableDate: string;
  startTime: string;
  endTime: string;
}
