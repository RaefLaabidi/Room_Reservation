import { User } from './User';
import { Room } from './Room';

export enum EventType {
  COURSE = 'COURSE',
  DEFENSE = 'DEFENSE',
  MEETING = 'MEETING'
}

export enum EventStatus {
  SCHEDULED = 'SCHEDULED',
  COMPLETED = 'COMPLETED',
  CANCELLED = 'CANCELLED'
}

export interface Event {
  id: number;
  type: EventType;
  date: string; // LocalDate as ISO string
  startTime: string; // LocalTime as string (HH:mm)
  endTime: string; // LocalTime as string (HH:mm)
  room?: Room;
  teacher: User;
  status: EventStatus;
  title?: string;
  description?: string;
  expectedParticipants?: number;
  preferredDates?: string[];
}

export interface EventCreateRequest {
  type: EventType;
  date: string;
  startTime: string;
  endTime: string;
  roomId: number;
  teacherId: number;
  status: EventStatus;
  title?: string;
  description?: string;
  expectedParticipants?: number;
}

export interface UnscheduledEventRequest {
  type: EventType;
  teacherId: number;
  title?: string;
  description?: string;
  expectedParticipants?: number;
  preferredDates?: string[];
  preferredStartTime?: string;
  preferredEndTime?: string;
  status?: EventStatus;
}

export interface ScheduleGenerationRequest {
  events: UnscheduledEventRequest[];
}

export interface UnscheduledEventResponse {
  type: EventType;
  teacher: User;
  title?: string;
  description?: string;
  expectedParticipants?: number;
  preferredDates?: string[];
  preferredStartTime?: string;
  preferredEndTime?: string;
  reason: string;
}

export interface ScheduleGenerationResponse {
  scheduledEvents: Event[];
  unscheduledEvents: UnscheduledEventResponse[];
  message: string;
}

export interface RescheduleEventRequest {
  date: string;
  startTime: string;
  endTime: string;
}

export interface ChangeRoomRequest {
  roomId: number;
}
