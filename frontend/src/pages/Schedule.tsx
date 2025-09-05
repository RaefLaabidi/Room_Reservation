import React, { useState, useEffect } from 'react';
import { Calendar, dateFnsLocalizer, View } from 'react-big-calendar';
import { format, parse, startOfWeek, getDay } from 'date-fns';
import { enUS } from 'date-fns/locale';
import { Event, EventType, Conflict, UnscheduledEventRequest } from '../types/index.js';
import { eventsAPI, conflictsAPI } from '../services/api.js';
import EventForm from '../components/EventForm.js';
import 'react-big-calendar/lib/css/react-big-calendar.css';

const locales = {
  'en-US': enUS,
};

const localizer = dateFnsLocalizer({
  format,
  parse,
  startOfWeek,
  getDay,
  locales,
});

interface CalendarEvent {
  id: number;
  title: string;
  start: Date;
  end: Date;
  resource: Event;
  style?: React.CSSProperties;
}

const Schedule: React.FC = () => {
  const [events, setEvents] = useState<Event[]>([]);
  const [conflicts, setConflicts] = useState<Conflict[]>([]);
  const [loading, setLoading] = useState(true);
  const [generating, setGenerating] = useState(false);
  const [selectedEvent, setSelectedEvent] = useState<Event | null>(null);
  const [showEventForm, setShowEventForm] = useState(false);
  const [editingEvent, setEditingEvent] = useState<Event | null>(null);
  const [sideDrawerOpen, setSideDrawerOpen] = useState(false);
  const [currentView, setCurrentView] = useState<View>('month');

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    try {
      const [eventsData, conflictsData] = await Promise.all([
        eventsAPI.getAll(),
        conflictsAPI.getAll(),
      ]);
      setEvents(eventsData);
      setConflicts(conflictsData);
    } catch (error) {
      console.error('Error fetching data:', error);
    } finally {
      setLoading(false);
    }
  };

  const getEventColor = (eventType: EventType): string => {
    switch (eventType) {
      case EventType.COURSE:
        return '#3B82F6'; // blue
      case EventType.DEFENSE:
        return '#F97316'; // orange
      case EventType.MEETING:
        return '#10B981'; // green
      default:
        return '#6B7280'; // gray
    }
  };

  const hasConflict = (eventId: number): boolean => {
    return conflicts.some(conflict => 
      conflict.event1.id === eventId || 
      (conflict.event2 && conflict.event2.id === eventId)
    );
  };

  const calendarEvents: CalendarEvent[] = events.map((event) => {
    const baseStyle = {
      backgroundColor: getEventColor(event.type),
      borderRadius: '4px',
      opacity: 0.8,
      color: 'white',
      border: '2px solid transparent',
    };

    if (hasConflict(event.id)) {
      baseStyle.border = '2px solid #EF4444'; // red border for conflicts
    }

    return {
      id: event.id,
      title: event.title || event.type,
      start: new Date(`${event.date}T${event.startTime}`),
      end: new Date(`${event.date}T${event.endTime}`),
      resource: event,
      style: baseStyle,
    };
  });

  const handleGenerateSchedule = async () => {
    setGenerating(true);
    try {
      // Create sample unscheduled events for demonstration
      const unscheduledEvents: UnscheduledEventRequest[] = [
        {
          type: EventType.COURSE,
          teacherId: 1,
          title: 'Mathematics Course',
          expectedParticipants: 30,
          preferredDates: [new Date().toISOString().split('T')[0]],
        }
      ];

      await eventsAPI.generateSchedule({ events: unscheduledEvents });
      await fetchData(); // Refresh data
    } catch (error) {
      console.error('Error generating schedule:', error);
    } finally {
      setGenerating(false);
    }
  };

  const handleEventSelect = (event: CalendarEvent) => {
    setSelectedEvent(event.resource);
    setSideDrawerOpen(true);
  };

  const handleEditEvent = () => {
    setEditingEvent(selectedEvent);
    setShowEventForm(true);
    setSideDrawerOpen(false);
  };

  const handleDeleteEvent = async () => {
    if (selectedEvent && window.confirm('Are you sure you want to delete this event?')) {
      try {
        await eventsAPI.delete(selectedEvent.id);
        setSideDrawerOpen(false);
        setSelectedEvent(null);
        await fetchData();
      } catch (error) {
        console.error('Error deleting event:', error);
      }
    }
  };

  const handleSyncGoogleCalendar = async () => {
    try {
      setLoading(true);
      const response = await fetch('/api/calendar/sync/admin', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        }
      });
      
      if (response.ok) {
        await fetchData(); // Refresh the schedule data
        alert('✅ Admin schedule successfully synchronized with Google Calendar! All student emails have been added as attendees.');
      } else {
        const errorData = await response.json();
        throw new Error(errorData.error || 'Failed to sync calendar');
      }
    } catch (error) {
      console.error('Error syncing Google Calendar:', error);
      alert('❌ Error syncing with Google Calendar: ' + (error instanceof Error ? error.message : 'Unknown error'));
    } finally {
      setLoading(false);
    }
  };

  const handleClearAndResyncGoogleCalendar = async () => {
    if (!window.confirm('This will clear existing Google Calendar sync data and re-create all events with proper titles. Continue?')) {
      return;
    }
    
    try {
      setLoading(true);
      const response = await fetch('/api/calendar/sync/admin/clear-and-resync', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        }
      });
      
      if (response.ok) {
        await fetchData(); // Refresh the schedule data
        alert('✅ Calendar cleared and re-synchronized! Events now have proper titles and dates.');
      } else {
        const errorData = await response.json();
        throw new Error(errorData.error || 'Failed to clear and re-sync calendar');
      }
    } catch (error) {
      console.error('Error clearing and re-syncing Google Calendar:', error);
      alert('❌ Error clearing and re-syncing: ' + (error instanceof Error ? error.message : 'Unknown error'));
    } finally {
      setLoading(false);
    }
  };

  const eventStyleGetter = (event: CalendarEvent) => {
    return {
      style: event.style || {}
    };
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="animate-spin rounded-full h-32 w-32 border-b-2 border-indigo-500"></div>
      </div>
    );
  }

  return (
    <div className="container mx-auto px-4 py-8">
      <div className="mb-8">
        <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
          <div>
            <h1 className="text-3xl font-bold text-gray-900">Schedule</h1>
            <p className="text-gray-600">Manage your events and schedule</p>
          </div>
          <div className="flex flex-col sm:flex-row gap-4">
            <button
              onClick={() => setShowEventForm(true)}
              className="bg-green-600 text-white px-4 py-2 rounded hover:bg-green-700"
            >
              Add Event
            </button>
            <button
              onClick={handleGenerateSchedule}
              disabled={generating}
              className="bg-indigo-600 text-white px-4 py-2 rounded hover:bg-indigo-700 disabled:opacity-50"
            >
              {generating ? 'Generating...' : 'Generate Schedule'}
            </button>
            <button
              onClick={handleSyncGoogleCalendar}
              disabled={loading}
              className="bg-orange-600 text-white px-4 py-2 rounded hover:bg-orange-700 disabled:opacity-50 flex items-center gap-2"
            >
              {loading ? '🔄 Syncing...' : '📅 Sync Google Calendar'}
            </button>
            <button
              onClick={handleClearAndResyncGoogleCalendar}
              disabled={loading}
              className="bg-red-600 text-white px-4 py-2 rounded hover:bg-red-700 disabled:opacity-50 flex items-center gap-2"
            >
              {loading ? '🧹 Clearing...' : '🧹 Clear & Re-sync'}
            </button>
          </div>
        </div>
      </div>

      {/* Legend */}
      <div className="bg-white rounded-lg shadow p-4 mb-6">
        <h3 className="text-lg font-semibold mb-3">Event Types</h3>
        <div className="flex flex-wrap gap-4">
          <div className="flex items-center gap-2">
            <div className="w-4 h-4 bg-blue-500 rounded"></div>
            <span className="text-sm">Course</span>
          </div>
          <div className="flex items-center gap-2">
            <div className="w-4 h-4 bg-orange-500 rounded"></div>
            <span className="text-sm">Defense</span>
          </div>
          <div className="flex items-center gap-2">
            <div className="w-4 h-4 bg-green-500 rounded"></div>
            <span className="text-sm">Meeting</span>
          </div>
          <div className="flex items-center gap-2">
            <div className="w-4 h-4 bg-gray-400 rounded border-2 border-red-500"></div>
            <span className="text-sm">Has Conflict</span>
          </div>
        </div>
      </div>

      {/* View Toggle */}
      <div className="bg-white rounded-lg shadow p-4 mb-6">
        <div className="flex flex-wrap gap-2">
          {(['month', 'week', 'day'] as View[]).map((view) => (
            <button
              key={view}
              onClick={() => setCurrentView(view)}
              className={`px-4 py-2 rounded capitalize ${
                currentView === view
                  ? 'bg-indigo-600 text-white'
                  : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
              }`}
            >
              {view}
            </button>
          ))}
        </div>
      </div>

      {/* Calendar */}
      <div className="bg-white rounded-lg shadow p-6">
        <div style={{ height: '600px' }}>
          <Calendar
            localizer={localizer}
            events={calendarEvents}
            startAccessor="start"
            endAccessor="end"
            style={{ height: '100%' }}
            views={['month', 'week', 'day']}
            view={currentView}
            onView={setCurrentView}
            popup
            selectable
            onSelectEvent={handleEventSelect}
            eventPropGetter={eventStyleGetter}
          />
        </div>
      </div>

      {/* Side Drawer */}
      {sideDrawerOpen && selectedEvent && (
        <div className="fixed inset-0 z-50 overflow-hidden">
          <div className="absolute inset-0 bg-black bg-opacity-50" onClick={() => setSideDrawerOpen(false)}></div>
          <div className="absolute right-0 top-0 h-full w-full max-w-md bg-white shadow-xl">
            <div className="flex flex-col h-full">
              <div className="flex items-center justify-between p-6 border-b">
                <h2 className="text-xl font-semibold">Event Details</h2>
                <button
                  onClick={() => setSideDrawerOpen(false)}
                  className="text-gray-400 hover:text-gray-600"
                >
                  <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                  </svg>
                </button>
              </div>
              
              <div className="flex-1 p-6 overflow-y-auto">
                <div className="space-y-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">Title</label>
                    <p className="text-gray-900">{selectedEvent.title || selectedEvent.type}</p>
                  </div>
                  
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">Type</label>
                    <span className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${
                      selectedEvent.type === 'COURSE' ? 'bg-blue-100 text-blue-800' :
                      selectedEvent.type === 'DEFENSE' ? 'bg-orange-100 text-orange-800' :
                      'bg-green-100 text-green-800'
                    }`}>
                      {selectedEvent.type}
                    </span>
                  </div>
                  
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">Date & Time</label>
                    <p className="text-gray-900">{selectedEvent.date}</p>
                    <p className="text-gray-600 text-sm">{selectedEvent.startTime} - {selectedEvent.endTime}</p>
                  </div>
                  
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">Room</label>
                    <p className="text-gray-900">{selectedEvent.room?.name || 'Not assigned'}</p>
                  </div>
                  
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">Teacher</label>
                    <p className="text-gray-900">{selectedEvent.teacher.name}</p>
                  </div>
                  
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">Status</label>
                    <span className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${
                      selectedEvent.status === 'SCHEDULED' ? 'bg-green-100 text-green-800' :
                      selectedEvent.status === 'COMPLETED' ? 'bg-blue-100 text-blue-800' :
                      'bg-red-100 text-red-800'
                    }`}>
                      {selectedEvent.status}
                    </span>
                  </div>
                  
                  {selectedEvent.description && (
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1">Description</label>
                      <p className="text-gray-900 text-sm">{selectedEvent.description}</p>
                    </div>
                  )}
                  
                  {hasConflict(selectedEvent.id) && (
                    <div className="bg-red-50 border border-red-200 rounded-md p-3">
                      <div className="flex">
                        <div className="flex-shrink-0">
                          <svg className="h-5 w-5 text-red-400" viewBox="0 0 20 20" fill="currentColor">
                            <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clipRule="evenodd" />
                          </svg>
                        </div>
                        <div className="ml-3">
                          <h3 className="text-sm font-medium text-red-800">Conflict Detected</h3>
                          <p className="text-sm text-red-700">This event has scheduling conflicts.</p>
                        </div>
                      </div>
                    </div>
                  )}
                </div>
              </div>
              
              <div className="border-t p-6">
                <div className="flex gap-3">
                  <button
                    onClick={handleEditEvent}
                    className="flex-1 bg-indigo-600 text-white px-4 py-2 rounded hover:bg-indigo-700"
                  >
                    Edit
                  </button>
                  <button
                    onClick={handleDeleteEvent}
                    className="flex-1 bg-red-600 text-white px-4 py-2 rounded hover:bg-red-700"
                  >
                    Delete
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Event Form Modal */}
      {showEventForm && (
        <EventForm
          event={editingEvent}
          onClose={() => {
            setShowEventForm(false);
            setEditingEvent(null);
          }}
          onSuccess={() => {
            setShowEventForm(false);
            setEditingEvent(null);
            fetchData();
          }}
        />
      )}
    </div>
  );
};

export default Schedule;
