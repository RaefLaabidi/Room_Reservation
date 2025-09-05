import React, { useState, useEffect } from 'react';
import { Course } from '../types/Course';
import { coursesAPI } from '../services/api';

interface WeeklyScheduleCreatorProps {
  onSuccess?: () => void;
}

interface CourseSelection {
  course: Course;
  selected: boolean;
  courseId: number;
  priority: number;
  studentCount: number;
}

interface EnhancedScheduleRequest {
  weekStartDate: string;
  courses: {
    courseId: number;
    priority: number;
    studentCount: number;
  }[];
}

interface ScheduleFilters {
  subject: string;
  minCapacity: number;
  maxCapacity: number;
  durationHours: string;
  sessionsPerWeek: string;
  searchTerm: string;
}

interface SchedulePreset {
  name: string;
  description: string;
  courses: number[];
}

const WeeklyScheduleCreator: React.FC<WeeklyScheduleCreatorProps> = ({ onSuccess }) => {
  const [courseSelections, setCourseSelections] = useState<CourseSelection[]>([]);
  const [weekStartDate, setWeekStartDate] = useState('');
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState<any>(null);
  const [showResult, setShowResult] = useState(false);
  
  // Advanced filters and features
  const [filters, setFilters] = useState<ScheduleFilters>({
    subject: 'All',
    minCapacity: 0,
    maxCapacity: 100,
    durationHours: 'All',
    sessionsPerWeek: 'All',
    searchTerm: ''
  });
  
  const [viewMode, setViewMode] = useState<'simple' | 'advanced'>('simple');
  const [availableSubjects, setAvailableSubjects] = useState<string[]>([]);
  const [selectedPreset, setSelectedPreset] = useState<string>('');
  const [bulkPriority, setBulkPriority] = useState<number>(1);
  const [showConflictAnalysis, setShowConflictAnalysis] = useState(false);
  const [useProfessionalAlgorithm, setUseProfessionalAlgorithm] = useState(true);
  
  // Predefined schedule presets for common scenarios
  const schedulePresets: SchedulePreset[] = [
    {
      name: 'Computer Science Intensive',
      description: 'Core CS courses for intensive week',
      courses: [1, 2, 3, 4, 5] // Will be mapped to actual course IDs
    },
    {
      name: 'Mathematics & Physics',
      description: 'STEM foundation courses',
      courses: [6, 7, 8, 9, 10]
    },
    {
      name: 'Business Essentials',
      description: 'Core business administration courses',
      courses: [11, 12, 13, 14, 15]
    },
    {
      name: 'Liberal Arts Mix',
      description: 'Diverse humanities and social sciences',
      courses: [16, 17, 18, 19, 20]
    }
  ];

  useEffect(() => {
    fetchCourses();
  }, []);

  const fetchCourses = async () => {
    try {
      const coursesData = await coursesAPI.getAll();
      // Initialize course selections
      const selections: CourseSelection[] = coursesData.map(course => ({
        course,
        selected: false,
        courseId: course.id!,
        priority: 1,
        studentCount: course.minCapacity || 20
      }));
      setCourseSelections(selections);
      
      // Extract unique subjects for filtering
      const subjects = [...new Set(coursesData.map(course => course.subject))];
      setAvailableSubjects(subjects);
    } catch (error) {
      console.error('Error fetching courses:', error);
    }
  };

  // Filter courses based on current filters
  const getFilteredCourses = () => {
    return courseSelections.filter(selection => {
      const course = selection.course;
      
      if (filters.subject !== 'All' && course.subject !== filters.subject) return false;
      if (course.minCapacity < filters.minCapacity || (course.maxCapacity && course.maxCapacity > filters.maxCapacity)) return false;
      if (filters.durationHours !== 'All' && course.durationHours !== parseInt(filters.durationHours)) return false;
      if (filters.sessionsPerWeek !== 'All' && course.sessionsPerWeek !== parseInt(filters.sessionsPerWeek)) return false;
      if (filters.searchTerm && !course.name.toLowerCase().includes(filters.searchTerm.toLowerCase())) return false;
      
      return true;
    });
  };

  // Bulk operations
  const selectAllFiltered = () => {
    const filteredCourses = getFilteredCourses();
    setCourseSelections(prev =>
      prev.map(selection => {
        if (filteredCourses.some(f => f.courseId === selection.courseId)) {
          return { ...selection, selected: true };
        }
        return selection;
      })
    );
  };

  const deselectAll = () => {
    setCourseSelections(prev => prev.map(s => ({ ...s, selected: false })));
  };

  const applyBulkPriority = () => {
    setCourseSelections(prev =>
      prev.map((selection, index) => 
        selection.selected 
          ? { ...selection, priority: bulkPriority + index }
          : selection
      )
    );
  };

  const shufflePriorities = () => {
    const selectedCourses = courseSelections.filter(s => s.selected);
    const shuffledPriorities = selectedCourses.map((_, index) => index + 1).sort(() => Math.random() - 0.5);
    
    setCourseSelections(prev =>
      prev.map(selection => {
        if (selection.selected) {
          const index = selectedCourses.findIndex(s => s.courseId === selection.courseId);
          return { ...selection, priority: shuffledPriorities[index] };
        }
        return selection;
      })
    );
  };

  const applyPreset = (presetName: string) => {
    const preset = schedulePresets.find(p => p.name === presetName);
    if (!preset) return;

    // Clear current selections
    deselectAll();
    
    // Select courses from preset (first N courses as a demo)
    setCourseSelections(prev =>
      prev.map((selection, index) => {
        if (index < preset.courses.length) {
          return { 
            ...selection, 
            selected: true, 
            priority: index + 1,
            studentCount: selection.course.minCapacity || 20
          };
        }
        return selection;
      })
    );
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
      const request: EnhancedScheduleRequest = {
        weekStartDate,
        courses: selectedCourses.map(selection => ({
          courseId: selection.courseId,
          priority: selection.priority,
          studentCount: selection.studentCount
        }))
      };

      // Use the professional algorithm by default
      const endpoint = useProfessionalAlgorithm 
        ? 'http://localhost:8080/api/weekly-schedule/create-professional'
        : 'http://localhost:8080/api/weekly-schedule/create';

      console.log(`🚀 Using ${useProfessionalAlgorithm ? 'PROFESSIONAL' : 'BASIC'} algorithm...`);
      console.log('📋 Request:', JSON.stringify(request, null, 2));

      let requestBody: any;
      if (useProfessionalAlgorithm) {
        // Professional endpoint expects just an array of course IDs
        requestBody = selectedCourses.map(selection => selection.courseId);
        console.log('📋 Professional request body (course IDs):', requestBody);
      } else {
        // Basic endpoint expects the full request object
        requestBody = request;
      }

      const response = await fetch(endpoint, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(requestBody)
      });

      if (!response.ok) {
        const errorText = await response.text();
        throw new Error(`HTTP error! status: ${response.status}, message: ${errorText}`);
      }

      const responseData = await response.json();
      console.log('✅ Response:', responseData);
      setResult(responseData);
      setShowResult(true);
      
      if (responseData.scheduledEvents && responseData.scheduledEvents.length > 0) {
        const successCount = responseData.successfulCourses || responseData.scheduledEvents.length;
        const totalCount = responseData.totalCourses || selectedCourses.length;
        
        alert(`🎉 ${useProfessionalAlgorithm ? 'Professional' : 'Enhanced'} Schedule Created Successfully!\n\n` +
              `✅ ${successCount}/${totalCount} courses scheduled\n` +
              `📅 ${responseData.scheduledEvents.length} total sessions\n` +
              `🎯 Using ${useProfessionalAlgorithm ? 'Advanced AI Distribution Algorithm' : 'Basic Algorithm'}`);
        onSuccess?.();
      } else {
        alert('❌ Schedule created but no events could be scheduled.\n\n' +
              'Possible issues:\n' +
              '• No available teachers for selected subjects\n' +
              '• No available rooms with sufficient capacity\n' +
              '• Time slot conflicts\n\n' +
              'Try adjusting your course selection or priorities.');
      }
    } catch (error) {
      console.error('💥 Error creating schedule:', error);
      const errorMessage = error instanceof Error ? error.message : 'Unknown error occurred';
      alert(`❌ Failed to create schedule.\n\nError: ${errorMessage}\n\n` +
            'Please check:\n' +
            '• Backend server is running\n' +
            '• Course selection is valid\n' +
            '• Network connection');
    } finally {
      setLoading(false);
    }
  };

  if (showResult && result) {
    return (
      <div className="bg-white rounded-lg shadow-md p-6">
        <div className="flex justify-between items-center mb-6">
          <h2 className="text-2xl font-bold text-gray-800">Schedule Creation Result</h2>
          <button
            onClick={() => setShowResult(false)}
            className="px-4 py-2 bg-gray-500 text-white rounded hover:bg-gray-600"
          >
            Create Another Schedule
          </button>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6">
          <div className="bg-green-100 p-4 rounded-lg">
            <h3 className="font-semibold text-green-800">Total Events Scheduled</h3>
            <p className="text-2xl font-bold text-green-600">{result.scheduledEvents?.length || 0}</p>
          </div>
          <div className="bg-blue-100 p-4 rounded-lg">
            <h3 className="font-semibold text-blue-800">Week Start Date</h3>
            <p className="text-lg font-bold text-blue-600">{result.weekStartDate || 'N/A'}</p>
          </div>
          <div className="bg-purple-100 p-4 rounded-lg">
            <h3 className="font-semibold text-purple-800">University Rules Applied</h3>
            <p className="text-sm font-bold text-purple-600">9AM-12:15PM & 1:30-4:45PM</p>
          </div>
        </div>

        {result.scheduledEvents && result.scheduledEvents.length > 0 && (
          <div className="bg-green-50 border border-green-200 rounded-lg p-4 mb-4">
            <h3 className="font-semibold text-green-800 mb-3">Scheduled Events:</h3>
            <div className="space-y-2">
              {result.scheduledEvents.map((event: any, index: number) => (
                <div key={index} className="bg-white p-3 rounded border">
                  <div className="grid grid-cols-2 md:grid-cols-4 gap-2 text-sm">
                    <div><strong>Course:</strong> {event.courseName}</div>
                    <div><strong>Day:</strong> {event.dayOfWeek}</div>
                    <div><strong>Time:</strong> {event.startTime} - {event.endTime}</div>
                    <div><strong>Room:</strong> {event.room?.name || 'Auto-assigned'}</div>
                    <div><strong>Teacher:</strong> {event.teacher?.firstName} {event.teacher?.lastName}</div>
                    <div><strong>Students:</strong> {event.studentCount}</div>
                    <div><strong>Date:</strong> {event.eventDate}</div>
                    <div><strong>Priority:</strong> {event.priority}</div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}

        {result.conflicts && result.conflicts.length > 0 && (
          <div className="bg-red-50 border border-red-200 rounded-lg p-4">
            <h3 className="font-semibold text-red-800 mb-2">Scheduling Conflicts:</h3>
            <ul className="list-disc pl-5">
              {result.conflicts.map((conflict: string, index: number) => (
                <li key={index} className="text-red-700">{conflict}</li>
              ))}
            </ul>
          </div>
        )}
      </div>
    );
  }

  return (
    <div className="bg-white rounded-lg shadow-md p-6">
      <div className="flex justify-between items-center mb-6">
        <h2 className="text-3xl font-bold text-gray-800">🎯 Professional Auto-Schedule Creator</h2>
        <div className="flex space-x-2">
          <button
            onClick={() => setUseProfessionalAlgorithm(!useProfessionalAlgorithm)}
            className={`px-4 py-2 rounded-md transition-colors ${
              useProfessionalAlgorithm 
                ? 'bg-green-600 text-white' 
                : 'bg-blue-600 text-white'
            }`}
            title={useProfessionalAlgorithm ? 'Using Professional AI Algorithm' : 'Using Basic Algorithm'}
          >
            {useProfessionalAlgorithm ? '🤖 AI Pro' : '📝 Basic'}
          </button>
          <button
            onClick={() => setViewMode(viewMode === 'simple' ? 'advanced' : 'simple')}
            className={`px-4 py-2 rounded-md transition-colors ${
              viewMode === 'advanced' 
                ? 'bg-purple-600 text-white' 
                : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
            }`}
          >
            {viewMode === 'simple' ? '🔧 Advanced Mode' : '📝 Simple Mode'}
          </button>
          <button
            onClick={() => setShowConflictAnalysis(!showConflictAnalysis)}
            className={`px-4 py-2 rounded-md transition-colors ${
              showConflictAnalysis 
                ? 'bg-orange-600 text-white' 
                : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
            }`}
          >
            📊 Analysis
          </button>
        </div>
      </div>
      
      {/* Algorithm Information */}
      <div className={`border rounded-lg p-4 mb-6 ${
        useProfessionalAlgorithm 
          ? 'bg-gradient-to-r from-green-50 to-emerald-50 border-green-200' 
          : 'bg-gradient-to-r from-blue-50 to-indigo-50 border-blue-200'
      }`}>
        <h3 className={`font-semibold mb-2 flex items-center ${
          useProfessionalAlgorithm ? 'text-green-800' : 'text-blue-800'
        }`}>
          {useProfessionalAlgorithm ? '🤖 Professional AI Algorithm' : '📝 Basic Algorithm'}
        </h3>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4 text-sm">
          <div className="bg-white rounded p-3">
            <strong>🎯 Scheduling Approach:</strong>
            <p className="mt-1">
              {useProfessionalAlgorithm 
                ? 'Advanced constraint-based AI with optimization scoring, perfect distribution, and zero duplicates'
                : 'Simple first-available assignment with basic conflict checking'
              }
            </p>
          </div>
          <div className="bg-white rounded p-3">
            <strong>🔄 Resource Distribution:</strong>
            <p className="mt-1">
              {useProfessionalAlgorithm 
                ? 'Smart teacher/room rotation, workload balancing, and expertise matching with scoring'
                : 'Basic availability checking without optimization'
              }
            </p>
          </div>
          <div className="bg-white rounded p-3">
            <strong>✅ Success Rate:</strong>
            <p className="mt-1">
              {useProfessionalAlgorithm 
                ? 'Guarantees ALL courses scheduled or provides detailed failure analysis'
                : 'May fail to schedule courses due to conflicts or resource limitations'
              }
            </p>
          </div>
        </div>
      </div>
      
      {/* University Rules Info */}
      <div className="bg-gradient-to-r from-blue-50 to-indigo-50 border border-blue-200 rounded-lg p-4 mb-6">
        <h3 className="font-semibold text-blue-800 mb-2 flex items-center">
          🏛️ University Scheduling Framework
        </h3>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4 text-sm text-blue-700">
          <div className="bg-white rounded p-3">
            <strong>⏰ Time Slots:</strong>
            <ul className="list-disc pl-4 mt-1">
              <li>Morning: 9:00 AM - 12:15 PM</li>
              <li>Afternoon: 1:30 PM - 4:45 PM</li>
            </ul>
          </div>
          <div className="bg-white rounded p-3">
            <strong>📅 Schedule Rules:</strong>
            <ul className="list-disc pl-4 mt-1">
              <li>Wednesday & Saturday: Morning only</li>
              <li>Sunday: No work</li>
              <li>Automatic conflict resolution</li>
            </ul>
          </div>
          <div className="bg-white rounded p-3">
            <strong>🎯 Smart Features:</strong>
            <ul className="list-disc pl-4 mt-1">
              <li>Priority-based assignment</li>
              <li>Teacher expertise matching</li>
              <li>Room capacity optimization</li>
            </ul>
          </div>
        </div>
      </div>

      {/* Schedule Presets */}
      {viewMode === 'advanced' && (
        <div className="bg-gradient-to-r from-green-50 to-emerald-50 border border-green-200 rounded-lg p-4 mb-6">
          <h3 className="font-semibold text-green-800 mb-3 flex items-center">
            📚 Quick Start Templates
          </h3>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-3">
            {schedulePresets.map((preset) => (
              <button
                key={preset.name}
                onClick={() => applyPreset(preset.name)}
                className="bg-white border border-green-300 rounded-lg p-3 hover:bg-green-50 transition-colors text-left"
              >
                <div className="font-semibold text-green-800 text-sm">{preset.name}</div>
                <div className="text-xs text-green-600 mt-1">{preset.description}</div>
              </button>
            ))}
          </div>
        </div>
      )}
      
      {/* Week Selection and Bulk Operations */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            📅 Week Start Date *
          </label>
          <input
            type="date"
            value={weekStartDate}
            onChange={(e) => setWeekStartDate(e.target.value)}
            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>
        
        {viewMode === 'advanced' && (
          <>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                🔄 Bulk Priority Start
              </label>
              <div className="flex space-x-2">
                <input
                  type="number"
                  min="1"
                  value={bulkPriority}
                  onChange={(e) => setBulkPriority(parseInt(e.target.value))}
                  className="flex-1 px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                />
                <button
                  onClick={applyBulkPriority}
                  className="px-3 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 text-sm"
                >
                  Apply
                </button>
              </div>
            </div>
            
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                🎲 Quick Actions
              </label>
              <div className="flex space-x-1">
                <button
                  onClick={selectAllFiltered}
                  className="flex-1 px-2 py-2 bg-green-600 text-white rounded-md hover:bg-green-700 text-xs"
                >
                  Select All
                </button>
                <button
                  onClick={deselectAll}
                  className="flex-1 px-2 py-2 bg-red-600 text-white rounded-md hover:bg-red-700 text-xs"
                >
                  Clear
                </button>
                <button
                  onClick={shufflePriorities}
                  className="flex-1 px-2 py-2 bg-purple-600 text-white rounded-md hover:bg-purple-700 text-xs"
                >
                  Shuffle
                </button>
              </div>
            </div>
            
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                📊 Statistics
              </label>
              <div className="bg-gray-100 rounded-md p-2 text-sm">
                <div>Selected: {getSelectedCourses().length}</div>
                <div>Total: {courseSelections.length}</div>
                <div>Subjects: {availableSubjects.length}</div>
              </div>
            </div>
          </>
        )}
      </div>

      {/* Advanced Filters */}
      {viewMode === 'advanced' && (
        <div className="bg-gray-50 border border-gray-200 rounded-lg p-4 mb-6">
          <h3 className="font-semibold text-gray-800 mb-3 flex items-center">
            🔍 Advanced Filters
          </h3>
          <div className="grid grid-cols-1 md:grid-cols-3 lg:grid-cols-6 gap-4">
            <div>
              <label className="block text-xs font-medium text-gray-700 mb-1">Search</label>
              <input
                type="text"
                placeholder="Course name..."
                value={filters.searchTerm}
                onChange={(e) => setFilters(prev => ({ ...prev, searchTerm: e.target.value }))}
                className="w-full px-2 py-1 text-sm border border-gray-300 rounded focus:outline-none focus:ring-1 focus:ring-blue-500"
              />
            </div>
            
            <div>
              <label className="block text-xs font-medium text-gray-700 mb-1">Subject</label>
              <select
                value={filters.subject}
                onChange={(e) => setFilters(prev => ({ ...prev, subject: e.target.value }))}
                className="w-full px-2 py-1 text-sm border border-gray-300 rounded focus:outline-none focus:ring-1 focus:ring-blue-500"
              >
                <option value="All">All Subjects</option>
                {availableSubjects.map(subject => (
                  <option key={subject} value={subject}>{subject}</option>
                ))}
              </select>
            </div>
            
            <div>
              <label className="block text-xs font-medium text-gray-700 mb-1">Duration</label>
              <select
                value={filters.durationHours}
                onChange={(e) => setFilters(prev => ({ ...prev, durationHours: e.target.value }))}
                className="w-full px-2 py-1 text-sm border border-gray-300 rounded focus:outline-none focus:ring-1 focus:ring-blue-500"
              >
                <option value="All">All Durations</option>
                <option value="2">2 hours</option>
                <option value="3">3 hours</option>
                <option value="4">4 hours</option>
                <option value="5">5+ hours</option>
              </select>
            </div>
            
            <div>
              <label className="block text-xs font-medium text-gray-700 mb-1">Sessions/Week</label>
              <select
                value={filters.sessionsPerWeek}
                onChange={(e) => setFilters(prev => ({ ...prev, sessionsPerWeek: e.target.value }))}
                className="w-full px-2 py-1 text-sm border border-gray-300 rounded focus:outline-none focus:ring-1 focus:ring-blue-500"
              >
                <option value="All">All Sessions</option>
                <option value="1">1 session</option>
                <option value="2">2 sessions</option>
                <option value="3">3 sessions</option>
              </select>
            </div>
            
            <div>
              <label className="block text-xs font-medium text-gray-700 mb-1">Min Capacity</label>
              <input
                type="number"
                min="0"
                value={filters.minCapacity}
                onChange={(e) => setFilters(prev => ({ ...prev, minCapacity: parseInt(e.target.value) }))}
                className="w-full px-2 py-1 text-sm border border-gray-300 rounded focus:outline-none focus:ring-1 focus:ring-blue-500"
              />
            </div>
            
            <div>
              <label className="block text-xs font-medium text-gray-700 mb-1">Max Capacity</label>
              <input
                type="number"
                min="0"
                value={filters.maxCapacity}
                onChange={(e) => setFilters(prev => ({ ...prev, maxCapacity: parseInt(e.target.value) }))}
                className="w-full px-2 py-1 text-sm border border-gray-300 rounded focus:outline-none focus:ring-1 focus:ring-blue-500"
              />
            </div>
          </div>
        </div>
      )}

      {/* Course Selection */}
      <div className="mb-6">
        <div className="flex justify-between items-center mb-4">
          <h3 className="text-lg font-semibold text-gray-800 flex items-center">
            📚 Course Selection & Prioritization
          </h3>
          <div className="text-sm text-gray-600">
            Showing {getFilteredCourses().length} of {courseSelections.length} courses
          </div>
        </div>
        
        <div className="bg-yellow-50 border border-yellow-200 rounded-lg p-4 mb-4">
          <p className="text-sm text-yellow-800">
            <strong>💡 Pro Tips:</strong>
            <br />• Priority 1 = highest priority (scheduled first), higher numbers = lower priority
            <br />• System automatically assigns optimal teachers and rooms based on expertise and availability
            <br />• {viewMode === 'advanced' ? 'Use filters and bulk operations to manage large course catalogs efficiently' : 'Switch to Advanced Mode for more powerful tools and filtering options'}
            <br />• Conflict analysis helps identify potential scheduling issues before creation
          </p>
        </div>
        
        <div className="space-y-3">
          {getFilteredCourses().map((selection) => (
            <div
              key={selection.courseId}
              className={`border-2 rounded-lg p-4 transition-all duration-200 ${
                selection.selected 
                  ? 'border-blue-500 bg-gradient-to-r from-blue-50 to-indigo-50 shadow-md' 
                  : 'border-gray-200 bg-white hover:border-gray-300 hover:shadow-sm'
              }`}
            >
              <div className="flex items-center mb-3">
                <input
                  type="checkbox"
                  id={`course-${selection.courseId}`}
                  checked={selection.selected}
                  onChange={(e) => updateCourseSelection(selection.courseId, 'selected', e.target.checked)}
                  className="mr-4 h-5 w-5 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
                />
                <label htmlFor={`course-${selection.courseId}`} className="flex-1">
                  <div className="flex justify-between items-start">
                    <div>
                      <div className="font-semibold text-gray-800 text-lg">{selection.course.name}</div>
                      <div className="text-sm text-gray-600 mt-1">
                        <span className="inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-gray-100 text-gray-800 mr-2">
                          📖 {selection.course.subject}
                        </span>
                        <span className="inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-blue-100 text-blue-800 mr-2">
                          ⏱️ {selection.course.durationHours}h
                        </span>
                        <span className="inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-green-100 text-green-800 mr-2">
                          📅 {selection.course.sessionsPerWeek}/week
                        </span>
                        <span className="inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-purple-100 text-purple-800">
                          👥 {selection.course.minCapacity}+ students
                        </span>
                      </div>
                    </div>
                    {selection.selected && (
                      <div className="text-right">
                        <div className="text-sm font-semibold text-blue-600">Priority #{selection.priority}</div>
                        <div className="text-xs text-blue-500">{selection.studentCount} students</div>
                      </div>
                    )}
                  </div>
                </label>
              </div>

              {selection.selected && (
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mt-3 pt-3 border-t border-gray-200">
                  <div>
                    <label className="block text-xs font-medium text-gray-700 mb-1">
                      🎯 Priority Level (1 = highest) *
                    </label>
                    <input
                      type="number"
                      min="1"
                      max="100"
                      value={selection.priority}
                      onChange={(e) => updateCourseSelection(selection.courseId, 'priority', parseInt(e.target.value))}
                      className="w-full px-3 py-2 text-sm border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                    />
                  </div>
                  <div>
                    <label className="block text-xs font-medium text-gray-700 mb-1">
                      👥 Expected Students
                    </label>
                    <input
                      type="number"
                      min="1"
                      max="200"
                      value={selection.studentCount}
                      onChange={(e) => updateCourseSelection(selection.courseId, 'studentCount', parseInt(e.target.value))}
                      className="w-full px-3 py-2 text-sm border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                    />
                  </div>
                </div>
              )}
            </div>
          ))}
        </div>

        {getFilteredCourses().length === 0 && (
          <div className="text-center py-8 text-gray-500">
            <div className="text-4xl mb-2">📚</div>
            <div>No courses match your current filters</div>
            <button
              onClick={() => setFilters({
                subject: 'All',
                minCapacity: 0,
                maxCapacity: 100,
                durationHours: 'All',
                sessionsPerWeek: 'All',
                searchTerm: ''
              })}
              className="mt-2 px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700"
            >
              Clear Filters
            </button>
          </div>
        )}
      </div>

      {/* Selected Courses Summary & Conflict Analysis */}
      {getSelectedCourses().length > 0 && (
        <div className="mb-6 space-y-4">
          {/* Summary */}
          <div className="bg-gradient-to-r from-green-50 to-emerald-50 border border-green-200 rounded-lg p-4">
            <h4 className="font-semibold text-green-800 mb-3 flex items-center">
              ✅ Selected Courses Summary ({getSelectedCourses().length} courses)
            </h4>
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-3">
              {getSelectedCourses()
                .sort((a, b) => a.priority - b.priority)
                .map((selection) => (
                  <div key={selection.courseId} className="bg-white rounded-lg p-3 border border-green-200">
                    <div className="flex justify-between items-start">
                      <div className="flex-1">
                        <div className="font-semibold text-green-800 text-sm">{selection.course.name}</div>
                        <div className="text-xs text-green-600 mt-1">
                          {selection.course.subject} • {selection.course.durationHours}h • {selection.studentCount} students
                        </div>
                      </div>
                      <div className="text-right">
                        <div className="bg-green-600 text-white rounded-full w-6 h-6 flex items-center justify-center text-xs font-bold">
                          {selection.priority}
                        </div>
                      </div>
                    </div>
                  </div>
                ))}
            </div>
          </div>

          {/* Conflict Analysis */}
          {showConflictAnalysis && (
            <div className="bg-gradient-to-r from-orange-50 to-amber-50 border border-orange-200 rounded-lg p-4">
              <h4 className="font-semibold text-orange-800 mb-3 flex items-center">
                📊 Pre-Schedule Analysis
              </h4>
              <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                <div className="bg-white rounded-lg p-3 border border-orange-200">
                  <div className="font-semibold text-orange-800 text-sm">Total Sessions Required</div>
                  <div className="text-2xl font-bold text-orange-600">
                    {getSelectedCourses().reduce((sum, s) => sum + s.course.sessionsPerWeek, 0)}
                  </div>
                  <div className="text-xs text-orange-600">sessions per week</div>
                </div>
                <div className="bg-white rounded-lg p-3 border border-orange-200">
                  <div className="font-semibold text-orange-800 text-sm">Total Students</div>
                  <div className="text-2xl font-bold text-orange-600">
                    {getSelectedCourses().reduce((sum, s) => sum + s.studentCount, 0)}
                  </div>
                  <div className="text-xs text-orange-600">across all courses</div>
                </div>
                <div className="bg-white rounded-lg p-3 border border-orange-200">
                  <div className="font-semibold text-orange-800 text-sm">Subjects Involved</div>
                  <div className="text-2xl font-bold text-orange-600">
                    {new Set(getSelectedCourses().map(s => s.course.subject)).size}
                  </div>
                  <div className="text-xs text-orange-600">different subjects</div>
                </div>
              </div>
              
              {/* Priority Conflicts Check */}
              {(() => {
                const priorities = getSelectedCourses().map(s => s.priority);
                const duplicates = priorities.filter((p, i) => priorities.indexOf(p) !== i);
                return duplicates.length > 0 && (
                  <div className="mt-3 p-3 bg-red-100 border border-red-300 rounded-lg">
                    <div className="text-red-800 font-semibold text-sm">⚠️ Priority Conflicts Detected</div>
                    <div className="text-red-700 text-xs mt-1">
                      Duplicate priorities found: {[...new Set(duplicates)].join(', ')}. Each course must have a unique priority.
                    </div>
                  </div>
                );
              })()}
            </div>
          )}
        </div>
      )}

      {/* Action Buttons */}
      <div className="flex flex-col sm:flex-row justify-between items-center space-y-3 sm:space-y-0 sm:space-x-4">
        <div className="flex space-x-3">
          <button
            onClick={() => {
              setWeekStartDate('');
              setCourseSelections(prev => prev.map(s => ({ ...s, selected: false })));
              setFilters({
                subject: 'All',
                minCapacity: 0,
                maxCapacity: 100,
                durationHours: 'All',
                sessionsPerWeek: 'All',
                searchTerm: ''
              });
            }}
            className="px-6 py-3 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors flex items-center"
            disabled={loading}
          >
            🔄 Reset All
          </button>
          
          {viewMode === 'advanced' && (
            <button
              onClick={() => {
                navigator.clipboard.writeText(JSON.stringify({
                  weekStartDate,
                  selectedCourses: getSelectedCourses().map(s => ({
                    courseId: s.courseId,
                    priority: s.priority,
                    studentCount: s.studentCount
                  }))
                }, null, 2));
                alert('Configuration copied to clipboard!');
              }}
              className="px-6 py-3 border border-blue-300 text-blue-700 rounded-lg hover:bg-blue-50 transition-colors flex items-center"
              disabled={loading || getSelectedCourses().length === 0}
            >
              📋 Copy Config
            </button>
          )}
        </div>
        
        <button
          onClick={handleCreateSchedule}
          disabled={loading || getSelectedCourses().length === 0}
          className="px-8 py-3 bg-gradient-to-r from-blue-600 to-indigo-600 text-white rounded-lg hover:from-blue-700 hover:to-indigo-700 disabled:from-gray-400 disabled:to-gray-400 disabled:cursor-not-allowed flex items-center text-lg font-semibold transition-all duration-200 shadow-lg hover:shadow-xl"
        >
          {loading && (
            <svg className="animate-spin -ml-1 mr-3 h-5 w-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
              <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
              <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
            </svg>
          )}
          {loading ? '🔄 Creating Schedule...' : '🚀 Create Professional Schedule'}
        </button>
      </div>
    </div>
  );
};

export default WeeklyScheduleCreator;
