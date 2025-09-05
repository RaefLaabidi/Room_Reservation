import React, { useState, useEffect } from 'react';
import { Course } from '../types/Course';
import { coursesAPI } from '../services/api';

interface EnhancedWeeklyScheduleCreatorProps {
  onSuccess?: () => void;
}

interface CourseSelection {
  course: Course;
  selected: boolean;
  courseId: number;
  priority: number;
  studentCount: number;
}

interface ScheduleRequest {
  weekStartDate: string;
  courses: {
    courseId: number;
    priority: number;
    studentCount: number;
  }[];
}

interface ScheduledEvent {
  courseId: number;
  courseName: string;
  teacher: {
    id: number;
    name: string;
    email: string;
  };
  room: {
    id: number;
    name: string;
    location: string;
    capacity: number;
  };
  startDateTime: string;
  endDateTime: string;
  studentCount: number;
  priority: number;
  sessionNumber: number;
}

interface ScheduleResult {
  weekStartDate: string;
  weekEndDate: string;
  totalCourses: number;
  successfulCourses: number;
  failedCourses: number;
  success: boolean;
  scheduledEvents: ScheduledEvent[];
  errors: string[];
}

const EnhancedWeeklyScheduleCreator: React.FC<EnhancedWeeklyScheduleCreatorProps> = ({ onSuccess }) => {
  const [courseSelections, setCourseSelections] = useState<CourseSelection[]>([]);
  const [weekStartDate, setWeekStartDate] = useState('');
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState<ScheduleResult | null>(null);
  const [showResult, setShowResult] = useState(false);

  useEffect(() => {
    fetchCourses();
  }, []);

  const fetchCourses = async () => {
    try {
      const coursesData = await coursesAPI.getAll();
      const selections: CourseSelection[] = coursesData.map(course => ({
        course,
        selected: false,
        courseId: course.id!,
        priority: 1,
        studentCount: course.minCapacity || 20
      }));
      setCourseSelections(selections);
    } catch (error) {
      console.error('Error fetching courses:', error);
    }
  };

  const updateCourseSelection = (courseId: number, field: keyof CourseSelection, value: any) => {
    setCourseSelections(prev =>
      prev.map(selection =>
        selection.courseId === courseId
          ? { ...selection, [field]: value }
          : selection
      )
    );
  };

  const getSelectedCourses = () => {
    return courseSelections.filter(selection => selection.selected);
  };

  const handleCreateSchedule = async () => {
    if (!weekStartDate) {
      alert('Please select a week start date');
      return;
    }

    const selectedCourses = getSelectedCourses();
    if (selectedCourses.length === 0) {
      alert('Please select at least one course');
      return;
    }

    // Check for duplicate priorities
    const priorities = selectedCourses.map(s => s.priority);
    const uniquePriorities = new Set(priorities);
    if (priorities.length !== uniquePriorities.size) {
      alert('Each course must have a unique priority number');
      return;
    }

    setLoading(true);
    try {
      const request: ScheduleRequest = {
        weekStartDate,
        courses: selectedCourses.map(selection => ({
          courseId: selection.courseId,
          priority: selection.priority,
          studentCount: selection.studentCount
        }))
      };

      const response = await fetch('http://localhost:8080/api/weekly-schedule/create', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(request)
      });

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      const scheduleResult: ScheduleResult = await response.json();
      setResult(scheduleResult);
      setShowResult(true);
      
      if (scheduleResult.success) {
        alert(`Schedule created successfully! ${scheduleResult.successfulCourses} courses scheduled.`);
        onSuccess?.();
      } else {
        alert(`Schedule created with some conflicts. ${scheduleResult.failedCourses} courses could not be scheduled.`);
      }
    } catch (error) {
      console.error('Error creating schedule:', error);
      alert('Failed to create schedule. Please check your input and try again.');
    } finally {
      setLoading(false);
    }
  };

  const formatDateTime = (dateTimeString: string) => {
    const date = new Date(dateTimeString);
    const dayName = date.toLocaleDateString('en-US', { weekday: 'long' });
    const time = date.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit', hour12: false });
    return `${dayName} ${time}`;
  };

  if (showResult && result) {
    return (
      <div className="bg-white rounded-lg shadow-md p-6">
        <div className="flex justify-between items-center mb-6">
          <h2 className="text-2xl font-bold text-gray-800">Enhanced Auto-Schedule Result</h2>
          <button
            onClick={() => setShowResult(false)}
            className="px-4 py-2 bg-gray-500 text-white rounded hover:bg-gray-600"
          >
            Create Another Schedule
          </button>
        </div>

        {/* Summary Cards */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6">
          <div className="bg-green-100 p-4 rounded-lg">
            <h3 className="font-semibold text-green-800">Total Courses</h3>
            <p className="text-2xl font-bold text-green-600">{result.totalCourses}</p>
          </div>
          <div className="bg-blue-100 p-4 rounded-lg">
            <h3 className="font-semibold text-blue-800">Successfully Scheduled</h3>
            <p className="text-2xl font-bold text-blue-600">{result.successfulCourses}</p>
          </div>
          <div className="bg-red-100 p-4 rounded-lg">
            <h3 className="font-semibold text-red-800">Failed to Schedule</h3>
            <p className="text-2xl font-bold text-red-600">{result.failedCourses}</p>
          </div>
        </div>

        {/* University Rules Info */}
        <div className="bg-blue-50 border border-blue-200 rounded-lg p-4 mb-6">
          <h3 className="font-semibold text-blue-800 mb-2">University Scheduling Rules Applied:</h3>
          <ul className="list-disc pl-5 text-blue-700 text-sm">
            <li>Morning Sessions: 9:00 AM - 12:15 PM</li>
            <li>Afternoon Sessions: 1:30 PM - 4:45 PM</li>
            <li>Wednesday & Saturday: Morning sessions only</li>
            <li>Sunday: No classes scheduled</li>
            <li>Automatic teacher & room assignment based on expertise and availability</li>
            <li>Priority-based scheduling (lower numbers = higher priority)</li>
          </ul>
        </div>

        {/* Scheduled Events */}
        {result.scheduledEvents.length > 0 && (
          <div className="mb-6">
            <h3 className="font-semibold text-gray-800 mb-4">Scheduled Events ({result.scheduledEvents.length}):</h3>
            <div className="space-y-3">
              {result.scheduledEvents
                .sort((a, b) => new Date(a.startDateTime).getTime() - new Date(b.startDateTime).getTime())
                .map((event, index) => (
                  <div key={index} className="border border-gray-200 rounded-lg p-4 bg-gray-50">
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                      <div>
                        <h4 className="font-semibold text-lg text-gray-800">{event.courseName}</h4>
                        <p className="text-sm text-gray-600">Session {event.sessionNumber} | Priority {event.priority}</p>
                        <p className="text-sm text-blue-600 mt-1">
                          {formatDateTime(event.startDateTime)} - {new Date(event.endDateTime).toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit', hour12: false })}
                        </p>
                      </div>
                      <div>
                        <p className="text-sm"><strong>Teacher:</strong> {event.teacher.name}</p>
                        <p className="text-sm"><strong>Room:</strong> {event.room.name} ({event.room.location})</p>
                        <p className="text-sm"><strong>Students:</strong> {event.studentCount} / {event.room.capacity} capacity</p>
                      </div>
                    </div>
                  </div>
                ))}
            </div>
          </div>
        )}

        {/* Errors */}
        {result.errors && result.errors.length > 0 && (
          <div className="bg-red-50 border border-red-200 rounded-lg p-4">
            <h3 className="font-semibold text-red-800 mb-2">Scheduling Issues:</h3>
            <ul className="list-disc pl-5">
              {result.errors.map((error, index) => (
                <li key={index} className="text-red-700">{error}</li>
              ))}
            </ul>
          </div>
        )}
      </div>
    );
  }

  return (
    <div className="bg-white rounded-lg shadow-md p-6">
      <h2 className="text-2xl font-bold text-gray-800 mb-6">Enhanced Auto-Scheduling</h2>
      
      {/* University Rules Information */}
      <div className="bg-blue-50 border border-blue-200 rounded-lg p-4 mb-6">
        <h3 className="font-semibold text-blue-800 mb-2">🏫 University Scheduling Rules</h3>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4 text-sm text-blue-700">
          <div>
            <h4 className="font-medium">⏰ Time Slots:</h4>
            <ul className="list-disc pl-4">
              <li>Morning: 9:00 AM - 12:15 PM</li>
              <li>Afternoon: 1:30 PM - 4:45 PM</li>
            </ul>
          </div>
          <div>
            <h4 className="font-medium">📅 Day Rules:</h4>
            <ul className="list-disc pl-4">
              <li>Wednesday & Saturday: Morning only</li>
              <li>Sunday: No classes</li>
              <li>Other days: Morning & afternoon</li>
            </ul>
          </div>
        </div>
        <p className="text-xs text-blue-600 mt-2">
          🤖 The system automatically assigns qualified teachers and appropriate rooms based on course requirements and availability.
        </p>
      </div>

      {/* Week Selection */}
      <div className="mb-6">
        <label className="block text-sm font-medium text-gray-700 mb-2">
          Week Start Date *
        </label>
        <input
          type="date"
          value={weekStartDate}
          onChange={(e) => setWeekStartDate(e.target.value)}
          className="w-full md:w-auto px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
        />
      </div>

      {/* Course Selection */}
      <div className="mb-6">
        <h3 className="text-lg font-semibold text-gray-800 mb-4">Select Courses and Set Priorities</h3>
        <div className="bg-yellow-50 border border-yellow-200 rounded-lg p-4 mb-4">
          <p className="text-sm text-yellow-800">
            <strong>Instructions:</strong>
            <br />• Select courses you want to schedule for this week
            <br />• Set priorities: 1 = highest priority (scheduled first), higher numbers = lower priority
            <br />• Each course must have a unique priority number
            <br />• The system will automatically follow university time constraints and assign teachers/rooms
          </p>
        </div>
        
        <div className="space-y-4">
          {courseSelections.map((selection) => (
            <div
              key={selection.courseId}
              className={`border rounded-lg p-4 ${
                selection.selected ? 'border-blue-500 bg-blue-50' : 'border-gray-200'
              }`}
            >
              <div className="flex items-center mb-3">
                <input
                  type="checkbox"
                  id={`course-${selection.courseId}`}
                  checked={selection.selected}
                  onChange={(e) => updateCourseSelection(selection.courseId, 'selected', e.target.checked)}
                  className="mr-3 h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
                />
                <label htmlFor={`course-${selection.courseId}`} className="flex-1">
                  <div className="font-semibold text-gray-800">{selection.course.name}</div>
                  <div className="text-sm text-gray-600">
                    Subject: {selection.course.subject} | Duration: {selection.course.durationHours}h | 
                    Sessions/week: {selection.course.sessionsPerWeek} | Min capacity: {selection.course.minCapacity}
                  </div>
                </label>
              </div>

              {selection.selected && (
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mt-3 pt-3 border-t border-gray-200">
                  <div>
                    <label className="block text-xs font-medium text-gray-700 mb-1">Priority (1 = highest) *</label>
                    <input
                      type="number"
                      min="1"
                      max="100"
                      value={selection.priority}
                      onChange={(e) => updateCourseSelection(selection.courseId, 'priority', parseInt(e.target.value))}
                      className="w-full px-2 py-1 text-sm border border-gray-300 rounded focus:outline-none focus:ring-1 focus:ring-blue-500"
                    />
                  </div>
                  <div>
                    <label className="block text-xs font-medium text-gray-700 mb-1">Expected Students</label>
                    <input
                      type="number"
                      min="1"
                      value={selection.studentCount}
                      onChange={(e) => updateCourseSelection(selection.courseId, 'studentCount', parseInt(e.target.value))}
                      className="w-full px-2 py-1 text-sm border border-gray-300 rounded focus:outline-none focus:ring-1 focus:ring-blue-500"
                    />
                  </div>
                </div>
              )}
            </div>
          ))}
        </div>
      </div>

      {/* Selected Courses Summary */}
      {getSelectedCourses().length > 0 && (
        <div className="mb-6 bg-green-50 border border-green-200 rounded-lg p-4">
          <h4 className="font-semibold text-green-800 mb-2">Selected Courses Summary:</h4>
          <div className="space-y-1">
            {getSelectedCourses()
              .sort((a, b) => a.priority - b.priority)
              .map((selection) => (
                <div key={selection.courseId} className="text-sm text-green-700">
                  Priority {selection.priority}: {selection.course.name} ({selection.studentCount} students)
                </div>
              ))}
          </div>
        </div>
      )}

      {/* Action Buttons */}
      <div className="flex justify-end space-x-4">
        <button
          onClick={() => {
            setWeekStartDate('');
            setCourseSelections(prev => prev.map(s => ({ ...s, selected: false, priority: 1 })));
          }}
          className="px-6 py-2 border border-gray-300 text-gray-700 rounded-md hover:bg-gray-50"
          disabled={loading}
        >
          Reset
        </button>
        <button
          onClick={handleCreateSchedule}
          disabled={loading || getSelectedCourses().length === 0}
          className="px-6 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 disabled:bg-gray-400 disabled:cursor-not-allowed flex items-center"
        >
          {loading && (
            <svg className="animate-spin -ml-1 mr-3 h-5 w-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
              <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
              <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
            </svg>
          )}
          {loading ? 'Creating Auto-Schedule...' : '🚀 Create Enhanced Auto-Schedule'}
        </button>
      </div>
    </div>
  );
};

export default EnhancedWeeklyScheduleCreator;
