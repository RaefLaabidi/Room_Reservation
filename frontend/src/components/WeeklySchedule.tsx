import React, { useState, useEffect } from 'react';
import './WeeklySchedule.css';

interface ScheduleEvent {
  id: string;
  title: string;
  startTime: string;
  endTime: string;
  room?: string;
  course?: string;
  location?: string;
  source: 'google_calendar' | 'reservation_system';
}

interface WeeklyScheduleProps {
  userType: 'professor' | 'student' | 'admin';
  userEmail?: string;
}

const WeeklySchedule: React.FC<WeeklyScheduleProps> = ({ userType, userEmail }) => {
  const [schedule, setSchedule] = useState<ScheduleEvent[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [currentWeek, setCurrentWeek] = useState(new Date());

  useEffect(() => {
    loadWeeklySchedule();
  }, [userType, userEmail, currentWeek]);

  const loadWeeklySchedule = async () => {
    setLoading(true);
    setError(null);

    try {
      let endpoint = '';
      
      if (userType === 'professor' && userEmail) {
        endpoint = `/api/calendar/professor/${userEmail}/weekly`;
      } else if (userType === 'student' && userEmail) {
        endpoint = `/api/calendar/student/${userEmail}/weekly`;
      } else {
        endpoint = '/api/calendar/weekly';
      }

      const response = await fetch(endpoint);
      const data = await response.json();

      if (response.ok) {
        setSchedule(data.weeklySchedule || []);
      } else {
        throw new Error(data.error || 'Failed to load schedule');
      }
    } catch (err) {
      console.error('Error loading weekly schedule:', err);
      setError(err instanceof Error ? err.message : 'Failed to load schedule');
    } finally {
      setLoading(false);
    }
  };

  const formatTime = (timeString: string) => {
    return new Date(timeString).toLocaleTimeString('en-US', {
      hour: '2-digit',
      minute: '2-digit',
      hour12: true
    });
  };

  const formatDate = (timeString: string) => {
    return new Date(timeString).toLocaleDateString('en-US', {
      weekday: 'long',
      month: 'short',
      day: 'numeric'
    });
  };

  const groupEventsByDay = (events: ScheduleEvent[]) => {
    const grouped: { [key: string]: ScheduleEvent[] } = {};
    
    events.forEach(event => {
      const date = new Date(event.startTime).toDateString();
      if (!grouped[date]) {
        grouped[date] = [];
      }
      grouped[date].push(event);
    });

    // Sort events within each day by start time
    Object.keys(grouped).forEach(date => {
      grouped[date].sort((a, b) => 
        new Date(a.startTime).getTime() - new Date(b.startTime).getTime()
      );
    });

    return grouped;
  };

  const getWeekDates = () => {
    const startOfWeek = new Date(currentWeek);
    const day = startOfWeek.getDay();
    const diff = startOfWeek.getDate() - day + (day === 0 ? -6 : 1); // Adjust when day is Sunday
    startOfWeek.setDate(diff);

    const weekDates = [];
    for (let i = 0; i < 7; i++) {
      const date = new Date(startOfWeek);
      date.setDate(startOfWeek.getDate() + i);
      weekDates.push(date);
    }
    return weekDates;
  };

  const navigateWeek = (direction: 'prev' | 'next') => {
    const newWeek = new Date(currentWeek);
    newWeek.setDate(currentWeek.getDate() + (direction === 'next' ? 7 : -7));
    setCurrentWeek(newWeek);
  };

  const syncCalendar = async () => {
    try {
      setLoading(true);
      const response = await fetch('/api/calendar/sync/primary', {
        method: 'POST'
      });
      
      if (response.ok) {
        await loadWeeklySchedule();
        alert('Calendar synchronized successfully!');
      } else {
        throw new Error('Failed to sync calendar');
      }
    } catch (err) {
      alert('Error syncing calendar: ' + (err instanceof Error ? err.message : 'Unknown error'));
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="weekly-schedule-container">
        <div className="loading">
          <div className="loading-spinner"></div>
          <p>Loading your weekly schedule...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="weekly-schedule-container">
        <div className="error">
          <p>❌ {error}</p>
          <button onClick={loadWeeklySchedule} className="retry-button">
            🔄 Retry
          </button>
        </div>
      </div>
    );
  }

  const groupedEvents = groupEventsByDay(schedule);
  const weekDates = getWeekDates();

  return (
    <div className="weekly-schedule-container">
      <div className="schedule-header">
        <div className="schedule-title">
          <h2>
            📅 Weekly Schedule
            {userType === 'professor' && ' - Teaching Classes'}
            {userType === 'student' && ' - My Classes'}
          </h2>
          <p className="user-info">
            {userEmail && `👤 ${userEmail}`}
          </p>
        </div>
        
        <div className="schedule-controls">
          <button onClick={() => navigateWeek('prev')} className="nav-button">
            ⬅️ Previous Week
          </button>
          <button onClick={() => setCurrentWeek(new Date())} className="today-button">
            📍 This Week
          </button>
          <button onClick={() => navigateWeek('next')} className="nav-button">
            Next Week ➡️
          </button>
          <button onClick={syncCalendar} className="sync-button">
            🔄 Sync Google Calendar
          </button>
        </div>
      </div>

      <div className="week-grid">
        {weekDates.map(date => {
          const dateString = date.toDateString();
          const dayEvents = groupedEvents[dateString] || [];
          const isToday = date.toDateString() === new Date().toDateString();

          return (
            <div key={dateString} className={`day-column ${isToday ? 'today' : ''}`}>
              <div className="day-header">
                <h3>{date.toLocaleDateString('en-US', { weekday: 'short' })}</h3>
                <p>{date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' })}</p>
                {isToday && <span className="today-badge">Today</span>}
              </div>
              
              <div className="day-events">
                {dayEvents.length === 0 ? (
                  <div className="no-events">
                    <p>🆓 Free day</p>
                  </div>
                ) : (
                  dayEvents.map(event => (
                    <div key={event.id} className={`event-card ${event.source}`}>
                      <div className="event-time">
                        {formatTime(event.startTime)} - {formatTime(event.endTime)}
                      </div>
                      <div className="event-title">{event.title}</div>
                      {event.room && (
                        <div className="event-location">📍 {event.room}</div>
                      )}
                      {event.course && (
                        <div className="event-course">📚 {event.course}</div>
                      )}
                      <div className="event-source">
                        {event.source === 'google_calendar' ? '🟢 Google Calendar' : '🔵 System'}
                      </div>
                    </div>
                  ))
                )}
              </div>
            </div>
          );
        })}
      </div>

      <div className="schedule-summary">
        <div className="summary-stats">
          <div className="stat-item">
            <strong>{schedule.length}</strong>
            <span>Total Events This Week</span>
          </div>
          <div className="stat-item">
            <strong>{Object.keys(groupedEvents).length}</strong>
            <span>Days with Classes</span>
          </div>
          <div className="stat-item">
            <strong>{7 - Object.keys(groupedEvents).length}</strong>
            <span>Free Days</span>
          </div>
        </div>
        
        <div className="integration-status">
          <p>
            🔗 <strong>Google Calendar Integration:</strong> 
            {schedule.some(e => e.source === 'google_calendar') ? 
              ' ✅ Connected and syncing' : 
              ' ⚠️ Set up Google Calendar for automatic sync'
            }
          </p>
        </div>
      </div>
    </div>
  );
};

export default WeeklySchedule;
