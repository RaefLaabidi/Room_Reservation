import React, { useState, useEffect } from 'react';

interface EnhancedEvent {
  id: number;
  title: string;
  description: string;
  date: string;
  startTime: string;
  endTime: string;
  type: string;
  status: string;
  expectedParticipants?: number;
  teacher?: {
    id: number;
    name: string;
    email: string;
  };
  room?: {
    id: number;
    name: string;
    location: string;
    capacity: number;
  };
  course?: {
    id: number;
    name: string;
    subject: string;
    durationHours: number;
    sessionsPerWeek: number;
    minCapacity: number;
    department: string;
  };
}

interface EventStatistics {
  totalEvents: number;
  courseEvents: number;
  nonCourseEvents: number;
  eventsByType: Record<string, number>;
  eventsByStatus: Record<string, number>;
  courseEventsBySubject: Record<string, number>;
}

const EventsAnalysis: React.FC = () => {
  const [events, setEvents] = useState<EnhancedEvent[]>([]);
  const [statistics, setStatistics] = useState<EventStatistics | null>(null);
  const [loading, setLoading] = useState(true);
  const [selectedView, setSelectedView] = useState<'all' | 'course' | 'non-course'>('all');
  const [selectedSubject, setSelectedSubject] = useState<string>('All');

  useEffect(() => {
    fetchEventsAndStatistics();
  }, []);

  const fetchEventsAndStatistics = async () => {
    try {
      setLoading(true);
      
      // Fetch events
      const eventsResponse = await fetch('http://localhost:8080/api/events');
      const eventsData = await eventsResponse.json();
      setEvents(eventsData);
      
      // Fetch statistics
      const statsResponse = await fetch('http://localhost:8080/api/events/statistics');
      const statsData = await statsResponse.json();
      setStatistics(statsData);
      
    } catch (error) {
      console.error('Error fetching events:', error);
    } finally {
      setLoading(false);
    }
  };

  const getFilteredEvents = () => {
    let filtered = events;
    
    // Filter by view type
    if (selectedView === 'course') {
      filtered = filtered.filter(event => event.course != null);
    } else if (selectedView === 'non-course') {
      filtered = filtered.filter(event => event.course == null);
    }
    
    // Filter by subject
    if (selectedSubject !== 'All') {
      filtered = filtered.filter(event => event.course?.subject === selectedSubject);
    }
    
    return filtered;
  };

  const getAvailableSubjects = () => {
    const subjects = [...new Set(events.filter(e => e.course).map(e => e.course!.subject))];
    return subjects.sort();
  };

  if (loading) {
    return (
      <div className="flex justify-center items-center h-64">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  return (
    <div className="bg-white rounded-lg shadow-md p-6">
      <div className="flex justify-between items-center mb-6">
        <h2 className="text-3xl font-bold text-gray-800">📊 Enhanced Events Analysis</h2>
        <button
          onClick={fetchEventsAndStatistics}
          className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 flex items-center"
        >
          🔄 Refresh
        </button>
      </div>

      {/* Statistics Dashboard */}
      {statistics && (
        <div className="grid grid-cols-1 md:grid-cols-4 gap-4 mb-6">
          <div className="bg-gradient-to-r from-blue-50 to-blue-100 p-4 rounded-lg border border-blue-200">
            <h3 className="font-semibold text-blue-800 text-sm">Total Events</h3>
            <p className="text-2xl font-bold text-blue-600">{statistics.totalEvents}</p>
          </div>
          <div className="bg-gradient-to-r from-green-50 to-green-100 p-4 rounded-lg border border-green-200">
            <h3 className="font-semibold text-green-800 text-sm">Course Events</h3>
            <p className="text-2xl font-bold text-green-600">{statistics.courseEvents}</p>
            <p className="text-xs text-green-500">
              {statistics.totalEvents > 0 ? Math.round((statistics.courseEvents / statistics.totalEvents) * 100) : 0}% of total
            </p>
          </div>
          <div className="bg-gradient-to-r from-purple-50 to-purple-100 p-4 rounded-lg border border-purple-200">
            <h3 className="font-semibold text-purple-800 text-sm">Other Events</h3>
            <p className="text-2xl font-bold text-purple-600">{statistics.nonCourseEvents}</p>
            <p className="text-xs text-purple-500">Meetings, exams, etc.</p>
          </div>
          <div className="bg-gradient-to-r from-orange-50 to-orange-100 p-4 rounded-lg border border-orange-200">
            <h3 className="font-semibold text-orange-800 text-sm">Subjects</h3>
            <p className="text-2xl font-bold text-orange-600">
              {Object.keys(statistics.courseEventsBySubject || {}).length}
            </p>
            <p className="text-xs text-orange-500">Different subjects</p>
          </div>
        </div>
      )}

      {/* Subject Distribution */}
      {statistics && Object.keys(statistics.courseEventsBySubject || {}).length > 0 && (
        <div className="bg-gray-50 rounded-lg p-4 mb-6">
          <h3 className="font-semibold text-gray-800 mb-3">📚 Events by Subject</h3>
          <div className="grid grid-cols-2 md:grid-cols-4 lg:grid-cols-6 gap-2">
            {Object.entries(statistics.courseEventsBySubject).map(([subject, count]) => (
              <div key={subject} className="bg-white rounded p-2 text-center border">
                <div className="text-xs font-medium text-gray-600 truncate" title={subject}>
                  {subject}
                </div>
                <div className="text-lg font-bold text-blue-600">{count}</div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Filters */}
      <div className="flex flex-wrap gap-4 mb-6">
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">View Type</label>
          <select
            value={selectedView}
            onChange={(e) => setSelectedView(e.target.value as any)}
            className="px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
          >
            <option value="all">All Events ({events.length})</option>
            <option value="course">Course Events ({events.filter(e => e.course).length})</option>
            <option value="non-course">Other Events ({events.filter(e => !e.course).length})</option>
          </select>
        </div>

        {selectedView !== 'non-course' && (
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Subject Filter</label>
            <select
              value={selectedSubject}
              onChange={(e) => setSelectedSubject(e.target.value)}
              className="px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
              <option value="All">All Subjects</option>
              {getAvailableSubjects().map(subject => (
                <option key={subject} value={subject}>{subject}</option>
              ))}
            </select>
          </div>
        )}
      </div>

      {/* Events List */}
      <div className="space-y-3">
        <h3 className="text-lg font-semibold text-gray-800">
          📅 Events ({getFilteredEvents().length})
        </h3>
        
        {getFilteredEvents().length === 0 ? (
          <div className="text-center py-8 text-gray-500">
            <div className="text-4xl mb-2">📅</div>
            <div>No events match your current filters</div>
          </div>
        ) : (
          getFilteredEvents().map((event) => (
            <div
              key={event.id}
              className={`border-l-4 rounded-lg p-4 ${
                event.course 
                  ? 'border-l-blue-500 bg-blue-50' 
                  : 'border-l-gray-500 bg-gray-50'
              }`}
            >
              <div className="flex justify-between items-start">
                <div className="flex-1">
                  <div className="flex items-center space-x-2 mb-2">
                    <h4 className="font-semibold text-gray-800">{event.title}</h4>
                    <span className={`px-2 py-1 rounded-full text-xs font-medium ${
                      event.course 
                        ? 'bg-blue-100 text-blue-800' 
                        : 'bg-gray-100 text-gray-800'
                    }`}>
                      {event.type}
                    </span>
                    <span className={`px-2 py-1 rounded-full text-xs font-medium ${
                      event.status === 'SCHEDULED' 
                        ? 'bg-green-100 text-green-800' 
                        : 'bg-yellow-100 text-yellow-800'
                    }`}>
                      {event.status}
                    </span>
                  </div>
                  
                  <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 text-sm text-gray-600">
                    <div>
                      <strong>📅 Date & Time:</strong><br />
                      {event.date} | {event.startTime} - {event.endTime}
                    </div>
                    
                    {event.teacher && (
                      <div>
                        <strong>👨‍🏫 Teacher:</strong><br />
                        {event.teacher.name}
                      </div>
                    )}
                    
                    {event.room && (
                      <div>
                        <strong>🏛️ Room:</strong><br />
                        {event.room.name} ({event.room.location})
                      </div>
                    )}
                    
                    {event.expectedParticipants && (
                      <div>
                        <strong>👥 Participants:</strong><br />
                        {event.expectedParticipants} expected
                      </div>
                    )}
                  </div>

                  {/* Course Information */}
                  {event.course && (
                    <div className="mt-3 p-3 bg-white rounded border border-blue-200">
                      <div className="grid grid-cols-1 md:grid-cols-3 gap-4 text-sm">
                        <div>
                          <strong className="text-blue-800">📚 Course:</strong><br />
                          <span className="text-blue-600">{event.course.name}</span>
                        </div>
                        <div>
                          <strong className="text-blue-800">🏫 Subject & Department:</strong><br />
                          <span className="text-blue-600">{event.course.subject}</span>
                          {event.course.department && <span className="text-blue-500"> ({event.course.department})</span>}
                        </div>
                        <div>
                          <strong className="text-blue-800">⏱️ Course Details:</strong><br />
                          <span className="text-blue-600">
                            {event.course.durationHours}h | {event.course.sessionsPerWeek}/week | {event.course.minCapacity}+ students
                          </span>
                        </div>
                      </div>
                    </div>
                  )}
                  
                  {event.description && (
                    <div className="mt-2 text-sm text-gray-600">
                      <strong>📝 Description:</strong> {event.description}
                    </div>
                  )}
                </div>
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  );
};

export default EventsAnalysis;
