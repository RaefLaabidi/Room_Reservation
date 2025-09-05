import { Event } from './Event';

export enum ConflictType {
  ROOM = 'ROOM',
  TEACHER = 'TEACHER',
  CAPACITY = 'CAPACITY'
}

export interface Conflict {
  id: number;
  conflictType: ConflictType;
  event1: Event;
  event2?: Event;
  description: string;
}
