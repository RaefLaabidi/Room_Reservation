import axios from 'axios';
import { 
  User, LoginRequest, LoginResponse, UserCreateRequest, RegisterRequest,
  Room, RoomCreateRequest,
  Event, EventCreateRequest, ScheduleGenerationRequest, ScheduleGenerationResponse, RescheduleEventRequest, ChangeRoomRequest,
  Availability, AvailabilityCreateRequest,
  Conflict
} from '../types/index.js';
import { Course } from '../types/Course.js';
import { ScheduleTemplate, TemplateCourseAssignment, SchedulingResult } from '../types/ScheduleTemplate.js';
import { WeeklyScheduleRequest } from '../types/WeeklySchedule.js';

// Create axios instance with base URL
const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  headers: {
    'Content-Type': 'application/json',
  },
});

// Add request interceptor to include auth token
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Add response interceptor for error handling
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

// Auth API
export const authAPI = {
  login: (credentials: LoginRequest): Promise<LoginResponse> =>
    api.post('/auth/login', credentials).then(res => res.data),
  
  register: (userData: RegisterRequest): Promise<User> =>
    api.post('/auth/register', userData).then(res => res.data),
};

// Users API
export const usersAPI = {
  getAll: (): Promise<User[]> =>
    api.get('/users').then(res => res.data),
  
  getById: (id: number): Promise<User> =>
    api.get(`/users/${id}`).then(res => res.data),
  
  getByRole: (role: string): Promise<User[]> =>
    api.get(`/users/role/${role}`).then(res => res.data),
  
  create: (user: UserCreateRequest): Promise<User> =>
    api.post('/users', user).then(res => res.data),
};

// Rooms API
export const roomsAPI = {
  getAll: (): Promise<Room[]> =>
    api.get('/rooms').then(res => res.data),
  
  getById: (id: number): Promise<Room> =>
    api.get(`/rooms/${id}`).then(res => res.data),
  
  getByMinCapacity: (capacity: number): Promise<Room[]> =>
    api.get(`/rooms/capacity/${capacity}`).then(res => res.data),
  
  create: (room: RoomCreateRequest): Promise<Room> =>
    api.post('/rooms', room).then(res => res.data),
  
  delete: (id: number): Promise<void> =>
    api.delete(`/rooms/${id}`).then(res => res.data),
};

// Events API
export const eventsAPI = {
  getAll: (): Promise<Event[]> =>
    api.get('/events').then(res => res.data),
  
  getById: (id: number): Promise<Event> =>
    api.get(`/events/${id}`).then(res => res.data),
  
  create: (event: EventCreateRequest): Promise<Event> =>
    api.post('/events', event).then(res => res.data),
  
  update: (id: number, event: EventCreateRequest): Promise<Event> =>
    api.put(`/events/${id}`, event).then(res => res.data),
  
  delete: (id: number): Promise<void> =>
    api.delete(`/events/${id}`).then(res => res.data),
  
  generateSchedule: (request: ScheduleGenerationRequest): Promise<ScheduleGenerationResponse> =>
    api.post('/events/generate', request).then(res => res.data),
  
  reschedule: (id: number, request: RescheduleEventRequest): Promise<Event> =>
    api.put(`/events/${id}/reschedule`, request).then(res => res.data),
  
  changeRoom: (id: number, request: ChangeRoomRequest): Promise<Event> =>
    api.put(`/events/${id}/change-room`, request).then(res => res.data),
};

// Availabilities API
export const availabilitiesAPI = {
  getAll: (): Promise<Availability[]> =>
    api.get('/availabilities').then(res => res.data),
  
  getById: (id: number): Promise<Availability> =>
    api.get(`/availabilities/${id}`).then(res => res.data),
  
  getByTeacher: (teacherId: number): Promise<Availability[]> =>
    api.get(`/availabilities/teacher/${teacherId}`).then(res => res.data),
  
  getByDate: (date: string): Promise<Availability[]> =>
    api.get(`/availabilities/date/${date}`).then(res => res.data),
  
  create: (availability: AvailabilityCreateRequest): Promise<Availability> =>
    api.post('/availabilities', availability).then(res => res.data),
  
  delete: (id: number): Promise<void> =>
    api.delete(`/availabilities/${id}`).then(res => res.data),
};

// Conflicts API
export const conflictsAPI = {
  getAll: (): Promise<Conflict[]> =>
    api.get('/conflicts').then(res => res.data),
  
  detect: (): Promise<Conflict[]> =>
    api.post('/conflicts/detect').then(res => res.data),
    
  preview: (): Promise<Conflict[]> =>
    api.get('/conflicts/preview').then(res => res.data),
};

// Courses API
export const coursesAPI = {
  getAll: (): Promise<Course[]> =>
    api.get('/courses').then(res => res.data),
    
  getById: (id: number): Promise<Course> =>
    api.get(`/courses/${id}`).then(res => res.data),
    
  create: (course: Omit<Course, 'id'>): Promise<Course> =>
    api.post('/courses', course).then(res => res.data),
    
  update: (id: number, course: Omit<Course, 'id'>): Promise<Course> =>
    api.put(`/courses/${id}`, course).then(res => res.data),
    
  delete: (id: number): Promise<void> =>
    api.delete(`/courses/${id}`).then(res => res.data),
    
  getBySubject: (subject: string): Promise<Course[]> =>
    api.get(`/courses/by-subject/${subject}`).then(res => res.data),
    
  getByDepartment: (department: string): Promise<Course[]> =>
    api.get(`/courses/by-department/${department}`).then(res => res.data),
};

// Schedule Templates API
export const scheduleTemplatesAPI = {
  getAll: (): Promise<ScheduleTemplate[]> =>
    api.get('/schedule-templates').then(res => res.data),
    
  getById: (id: number): Promise<ScheduleTemplate> =>
    api.get(`/schedule-templates/${id}`).then(res => res.data),
    
  create: (template: Omit<ScheduleTemplate, 'id'>): Promise<ScheduleTemplate> =>
    api.post('/schedule-templates', template).then(res => res.data),
    
  update: (id: number, template: Omit<ScheduleTemplate, 'id'>): Promise<ScheduleTemplate> =>
    api.put(`/schedule-templates/${id}`, template).then(res => res.data),
    
  delete: (id: number): Promise<void> =>
    api.delete(`/schedule-templates/${id}`).then(res => res.data),
    
  getByStatus: (status: 'DRAFT' | 'PUBLISHED' | 'ARCHIVED'): Promise<ScheduleTemplate[]> =>
    api.get(`/schedule-templates/by-status/${status}`).then(res => res.data),
};

// Auto-Scheduling API
export const autoSchedulingAPI = {
  generateSchedule: (templateId: number): Promise<SchedulingResult> =>
    api.post(`/scheduling/generate/${templateId}`).then(res => res.data),
};

// Weekly Schedule API
export const weeklyScheduleAPI = {
  create: (request: WeeklyScheduleRequest): Promise<WeeklyScheduleRequest> =>
    api.post('/weekly-schedule/create', request).then(res => res.data),
    
  getAll: (): Promise<ScheduleTemplate[]> =>
    api.get('/weekly-schedule').then(res => res.data),
    
  getById: (templateId: number): Promise<ScheduleTemplate> =>
    api.get(`/weekly-schedule/${templateId}`).then(res => res.data),
    
  delete: (templateId: number): Promise<void> =>
    api.delete(`/weekly-schedule/${templateId}`).then(res => res.data),
};

export default api;
