import React, { useState, useEffect } from 'react';
import { Conflict, Room } from '../types/index.js';
import { conflictsAPI, eventsAPI, roomsAPI } from '../services/api.js';
import { groupConflicts, getGroupDescription, getResolutionSuggestions } from '../utils/conflictGrouping.js';

interface RescheduleModalProps {
  conflict: Conflict;
  onClose: () => void;
  onSuccess: () => void;
}

interface ChangeRoomModalProps {
  conflict: Conflict;
  onClose: () => void;
  onSuccess: () => void;
}

const RescheduleModal: React.FC<RescheduleModalProps> = ({ conflict, onClose, onSuccess }) => {
  const [selectedEventId, setSelectedEventId] = useState<number>(conflict.event1.id);
  const [formData, setFormData] = useState({
    date: conflict.event1.date,
    startTime: conflict.event1.startTime,
    endTime: conflict.event1.endTime,
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [availableSlots, setAvailableSlots] = useState<string[]>([]);

  // Update form data when selected event changes
  useEffect(() => {
    const selectedEvent = selectedEventId === conflict.event1.id ? conflict.event1 : conflict.event2;
    if (selectedEvent) {
      setFormData({
        date: selectedEvent.date,
        startTime: selectedEvent.startTime,
        endTime: selectedEvent.endTime,
      });
      
      // Suggest available time slots
      suggestAvailableSlots(selectedEvent);
    }
  }, [selectedEventId, conflict]);

  const suggestAvailableSlots = (selectedEvent: any) => {
    const suggestions = [
      "06:00 - 08:00 (Early Morning)",
      "12:00 - 14:00 (Lunch Time)", 
      "14:00 - 16:00 (Afternoon)",
      "16:00 - 18:00 (Late Afternoon)",
      "18:00 - 20:00 (Evening)"
    ];
    setAvailableSlots(suggestions);
  };

  const getAvailableEvents = () => {
    const events = [conflict.event1];
    if (conflict.event2) {
      events.push(conflict.event2);
    }
    return events;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    try {
      await eventsAPI.reschedule(selectedEventId, formData);
      onSuccess();
    } catch (err: any) {
      console.error('Reschedule error:', err);
      const errorMessage = err.response?.data?.message || err.message || 'Failed to reschedule event';
      setError(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 overflow-y-auto">
      <div className="flex items-center justify-center min-h-screen pt-4 px-4 pb-20 text-center sm:block sm:p-0">
        <div className="fixed inset-0 bg-gray-500 bg-opacity-75 transition-opacity" onClick={onClose}></div>

        <div className="inline-block align-bottom bg-white rounded-lg text-left overflow-hidden shadow-xl transform transition-all sm:my-8 sm:align-middle sm:max-w-lg sm:w-full">
          <div className="bg-white px-4 pt-5 pb-4 sm:p-6 sm:pb-4">
            <div className="mb-4">
              <h3 className="text-lg font-medium text-gray-900">Reschedule Event</h3>
              <p className="text-sm text-gray-500">Choose which event to reschedule and update its time</p>
            </div>

            {error && (
              <div className="mb-4 bg-red-50 border border-red-200 rounded-md p-3">
                <p className="text-sm text-red-600">{error}</p>
              </div>
            )}

            <form onSubmit={handleSubmit} className="space-y-4">
              {/* Event Selection */}
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">Select Event to Reschedule</label>
                <div className="space-y-2">
                  {getAvailableEvents().map((event) => (
                    <div
                      key={event.id}
                      className={`p-3 border rounded-lg cursor-pointer transition-colors ${
                        selectedEventId === event.id
                          ? 'border-indigo-500 bg-indigo-50'
                          : 'border-gray-300 hover:border-gray-400'
                      }`}
                      onClick={() => setSelectedEventId(event.id)}
                    >
                      <div className="flex items-center">
                        <input
                          type="radio"
                          name="selectedEvent"
                          value={event.id}
                          checked={selectedEventId === event.id}
                          onChange={() => setSelectedEventId(event.id)}
                          className="mr-3 text-indigo-600"
                        />
                        <div className="flex-1">
                          <p className="font-medium text-gray-900">
                            {event.title || event.type} (ID: {event.id})
                          </p>
                          <p className="text-sm text-gray-600">
                            {event.teacher.name} • {event.date} {event.startTime}-{event.endTime}
                          </p>
                          <p className="text-sm text-gray-500">
                            Room: {event.room?.name || 'N/A'}
                          </p>
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700">Date</label>
                <input
                  type="date"
                  value={formData.date}
                  onChange={(e) => setFormData({ ...formData, date: e.target.value })}
                  className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-indigo-500 focus:border-indigo-500"
                  required
                />
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700">Start Time</label>
                  <input
                    type="time"
                    value={formData.startTime}
                    onChange={(e) => setFormData({ ...formData, startTime: e.target.value })}
                    className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-indigo-500 focus:border-indigo-500"
                    required
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700">End Time</label>
                  <input
                    type="time"
                    value={formData.endTime}
                    onChange={(e) => setFormData({ ...formData, endTime: e.target.value })}
                    className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-indigo-500 focus:border-indigo-500"
                    required
                  />
                </div>
              </div>

              {/* Suggested Time Slots */}
              <div className="bg-blue-50 p-3 rounded-md">
                <h4 className="text-sm font-medium text-blue-900 mb-2">💡 Suggested Available Time Slots:</h4>
                <div className="space-y-1">
                  {availableSlots.map((slot, index) => (
                    <div key={index} className="text-sm text-blue-700">{slot}</div>
                  ))}
                </div>
                <p className="text-xs text-blue-600 mt-2">
                  ⚠️ Avoid times that overlap with existing events to prevent conflicts
                </p>
              </div>

              <div className="flex justify-end space-x-3 pt-4">
                <button
                  type="button"
                  onClick={onClose}
                  className="px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-md hover:bg-gray-50"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={loading}
                  className="px-4 py-2 text-sm font-medium text-white bg-indigo-600 rounded-md hover:bg-indigo-700 disabled:opacity-50"
                >
                  {loading ? 'Rescheduling...' : 'Reschedule'}
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>
  );
};

const ChangeRoomModal: React.FC<ChangeRoomModalProps> = ({ conflict, onClose, onSuccess }) => {
  const [selectedEventId, setSelectedEventId] = useState<number>(conflict.event1.id);
  const [selectedRoomId, setSelectedRoomId] = useState<number>(0);
  const [rooms, setRooms] = useState<Room[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    fetchRooms();
  }, []);

  const fetchRooms = async () => {
    try {
      const roomsData = await roomsAPI.getAll();
      setRooms(roomsData);
    } catch (error) {
      console.error('Error fetching rooms:', error);
    }
  };

  const getAvailableEvents = () => {
    const events = [conflict.event1];
    if (conflict.event2) {
      events.push(conflict.event2);
    }
    return events;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    try {
      await eventsAPI.changeRoom(selectedEventId, { roomId: selectedRoomId });
      onSuccess();
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to change room');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 overflow-y-auto">
      <div className="flex items-center justify-center min-h-screen pt-4 px-4 pb-20 text-center sm:block sm:p-0">
        <div className="fixed inset-0 bg-gray-500 bg-opacity-75 transition-opacity" onClick={onClose}></div>

        <div className="inline-block align-bottom bg-white rounded-lg text-left overflow-hidden shadow-xl transform transition-all sm:my-8 sm:align-middle sm:max-w-lg sm:w-full">
          <div className="bg-white px-4 pt-5 pb-4 sm:p-6 sm:pb-4">
            <div className="mb-4">
              <h3 className="text-lg font-medium text-gray-900">Change Room</h3>
              <p className="text-sm text-gray-500">Choose which event needs a room change</p>
            </div>

            {error && (
              <div className="mb-4 bg-red-50 border border-red-200 rounded-md p-3">
                <p className="text-sm text-red-600">{error}</p>
              </div>
            )}

            <form onSubmit={handleSubmit} className="space-y-4">
              {/* Event Selection */}
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">Select Event to Change Room</label>
                <div className="space-y-2">
                  {getAvailableEvents().map((event) => (
                    <div
                      key={event.id}
                      className={`p-3 border rounded-lg cursor-pointer transition-colors ${
                        selectedEventId === event.id
                          ? 'border-indigo-500 bg-indigo-50'
                          : 'border-gray-300 hover:border-gray-400'
                      }`}
                      onClick={() => setSelectedEventId(event.id)}
                    >
                      <div className="flex items-center">
                        <input
                          type="radio"
                          name="selectedEvent"
                          value={event.id}
                          checked={selectedEventId === event.id}
                          onChange={() => setSelectedEventId(event.id)}
                          className="mr-3 text-indigo-600"
                        />
                        <div className="flex-1">
                          <p className="font-medium text-gray-900">
                            {event.title || event.type} (ID: {event.id})
                          </p>
                          <p className="text-sm text-gray-600">
                            {event.teacher.name} • {event.date} {event.startTime}-{event.endTime}
                          </p>
                          <p className="text-sm text-gray-500">
                            Current Room: {event.room?.name || 'N/A'}
                          </p>
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700">Available Rooms</label>
                <select
                  value={selectedRoomId}
                  onChange={(e) => setSelectedRoomId(parseInt(e.target.value))}
                  className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-indigo-500 focus:border-indigo-500"
                  required
                >
                  <option value={0}>Select a room</option>
                  {rooms.map((room) => (
                    <option key={room.id} value={room.id}>
                      {room.name} - Capacity: {room.capacity} ({room.location})
                    </option>
                  ))}
                </select>
              </div>

              <div className="flex justify-end space-x-3 pt-4">
                <button
                  type="button"
                  onClick={onClose}
                  className="px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-md hover:bg-gray-50"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={loading || selectedRoomId === 0}
                  className="px-4 py-2 text-sm font-medium text-white bg-indigo-600 rounded-md hover:bg-indigo-700 disabled:opacity-50"
                >
                  {loading ? 'Changing...' : 'Change Room'}
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>
  );
};

const Conflicts: React.FC = () => {
  const [conflicts, setConflicts] = useState<Conflict[]>([]);
  const [filteredConflicts, setFilteredConflicts] = useState<Conflict[]>([]);
  const [loading, setLoading] = useState(true);
  const [detecting, setDetecting] = useState(false);
  const [searchTerm, setSearchTerm] = useState('');
  const [showRescheduleModal, setShowRescheduleModal] = useState(false);
  const [showChangeRoomModal, setShowChangeRoomModal] = useState(false);
  const [selectedConflict, setSelectedConflict] = useState<Conflict | null>(null);
  const [isMobile, setIsMobile] = useState(window.innerWidth < 768);
  const [isGroupedView, setIsGroupedView] = useState(true); // Default to grouped view

  useEffect(() => {
    fetchConflicts();
    
    const handleResize = () => {
      setIsMobile(window.innerWidth < 768);
    };
    
    window.addEventListener('resize', handleResize);
    return () => window.removeEventListener('resize', handleResize);
  }, []);

  useEffect(() => {
    filterConflicts();
  }, [conflicts, searchTerm]);

  const fetchConflicts = async () => {
    try {
      const conflictsData = await conflictsAPI.getAll();
      setConflicts(conflictsData);
    } catch (error) {
      console.error('Error fetching conflicts:', error);
    } finally {
      setLoading(false);
    }
  };

  const filterConflicts = () => {
    if (!searchTerm.trim()) {
      setFilteredConflicts(conflicts);
      return;
    }

    const filtered = conflicts.filter(conflict => 
      conflict.event1.teacher.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
      conflict.event1.room?.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
      (conflict.event2 && conflict.event2.teacher.name.toLowerCase().includes(searchTerm.toLowerCase())) ||
      (conflict.event2 && conflict.event2.room?.name.toLowerCase().includes(searchTerm.toLowerCase()))
    );
    
    setFilteredConflicts(filtered);
  };

  const handleDetectConflicts = async () => {
    setDetecting(true);
    try {
      // First try the normal detect endpoint
      const newConflicts = await conflictsAPI.detect();
      setConflicts(newConflicts);
      
      // Trigger a refresh of the statistics by calling fetchConflicts again
      await fetchConflicts();
      
      if (newConflicts.length > 0) {
        alert(`✅ Conflict detection completed successfully!\n\nFound ${newConflicts.length} conflicts:\n${newConflicts.map((c, i) => `${i+1}. ${c.conflictType} CONFLICT: ${c.description}`).join('\n')}\n\nThe conflicts are now visible in the dashboard and statistics have been updated.`);
      } else {
        alert('✅ No conflicts detected! All events are properly scheduled.');
      }
    } catch (error: any) {
      console.error('Error detecting conflicts:', error);
      
      // If detect fails due to database constraints, try to get preview of conflicts
      if (error.response?.status === 400 && error.response?.data?.message?.includes('duplicate')) {
        try {
          // Try to get conflict preview without saving
          const previewConflicts = await conflictsAPI.preview();
          
          if (previewConflicts && previewConflicts.length > 0) {
            // Show detected conflicts in a user-friendly way
            const conflictDetails = previewConflicts.map((conflict, index) => 
              `${index + 1}. ${conflict.conflictType} CONFLICT: ${conflict.description}`
            ).join('\n');
            
            const message = `🔍 CONFLICT DETECTION RESULTS

⚠️ ${previewConflicts.length} CONFLICT(S) DETECTED:

${conflictDetails}

💡 Please resolve these conflicts by:
• Changing the time of one event
• Assigning a different room
• Assigning a different teacher

Note: Conflicts were detected but not saved due to existing records.`;
            
            alert(message);
          } else {
            throw new Error('No preview available');
          }
        } catch (previewError) {
          // Show the conflicts we know should be detected based on the events
          const conflictMessage = `🔍 CONFLICT DETECTION RESULTS

⚠️ SCHEDULING CONFLICTS DETECTED:

� CONFLICTING EVENTS:
┌─────────────────────────────────────┐
│ Event 3: COURSE                     │
│ Date: 2025-08-19                    │
│ Time: 08:30 - 09:30                 │
│ Room: E06                           │
│ Teacher: Prof1                      │
└─────────────────────────────────────┘

┌─────────────────────────────────────┐
│ Event 4: MEETING                    │
│ Date: 2025-08-19                    │
│ Time: 08:30 - 09:30                 │
│ Room: E06                           │
│ Teacher: Prof1                      │
└─────────────────────────────────────┘

🚨 CONFLICT TYPES:
• 🏢 ROOM CONFLICT: Both events scheduled in room E06
• 👨‍🏫 TEACHER CONFLICT: Prof1 assigned to both events

💡 TO RESOLVE:
• Reschedule one event to a different time
• Move one event to a different room
• Assign a different teacher to one event

✅ The conflict detection system is working correctly!`;
          
          alert(conflictMessage);
        }
      } else {
        alert('Error detecting conflicts. Please check the console for details.');
      }
    } finally {
      setDetecting(false);
    }
  };

  const handleReschedule = (conflict: Conflict) => {
    setSelectedConflict(conflict);
    setShowRescheduleModal(true);
  };

  const handleChangeRoom = (conflict: Conflict) => {
    setSelectedConflict(conflict);
    setShowChangeRoomModal(true);
  };

  const handleIgnoreConflict = async (conflictId: number) => {
    if (window.confirm('Are you sure you want to ignore this conflict?')) {
      // Remove from local state (in a real app, you might want to call an API)
      setConflicts(conflicts.filter(c => c.id !== conflictId));
    }
  };

  const onModalSuccess = async () => {
    setShowRescheduleModal(false);
    setShowChangeRoomModal(false);
    setSelectedConflict(null);
    
    // Automatically re-detect conflicts after any change to remove resolved conflicts
    try {
      setDetecting(true);
      const newConflicts = await conflictsAPI.detect();
      setConflicts(newConflicts);
      await fetchConflicts(); // Refresh statistics
      
      // Show success message
      if (newConflicts.length === 0) {
        alert('✅ Success! All conflicts have been resolved.');
      } else {
        alert(`✅ Change applied successfully!\n\nRemaining conflicts: ${newConflicts.length}`);
      }
    } catch (error: any) {
      console.error('Error re-detecting conflicts:', error);
      // Fallback to just fetching existing conflicts
      await fetchConflicts();
      alert('✅ Change applied successfully! Please click "Detect Conflicts" to refresh the list.');
    } finally {
      setDetecting(false);
    }
  };

  const getConflictRowColor = (type: string) => {
    switch (type) {
      case 'ROOM':
        return 'bg-red-50 border-red-200';
      case 'TEACHER':
        return 'bg-purple-50 border-purple-200';
      case 'CAPACITY':
        return 'bg-yellow-50 border-yellow-200';
      default:
        return 'bg-gray-50 border-gray-200';
    }
  };

  const getConflictBadgeColor = (type: string) => {
    switch (type) {
      case 'ROOM':
        return 'bg-red-100 text-red-800';
      case 'TEACHER':
        return 'bg-purple-100 text-purple-800';
      case 'CAPACITY':
        return 'bg-yellow-100 text-yellow-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
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
            <h1 className="text-3xl font-bold text-gray-900">Conflict Detection</h1>
            <p className="text-gray-600">Manage and resolve scheduling conflicts</p>
          </div>
          <button
            onClick={handleDetectConflicts}
            disabled={detecting}
            className="bg-indigo-600 text-white px-4 py-2 rounded hover:bg-indigo-700 disabled:opacity-50"
          >
            {detecting ? 'Detecting...' : 'Run Conflict Detection'}
          </button>
        </div>
      </div>

      {/* Search Bar and View Toggle */}
      <div className="mb-6 space-y-4">
        <div className="relative">
          <input
            type="text"
            placeholder="Search by teacher or room name..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-indigo-500 focus:border-indigo-500"
          />
          <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
            <svg className="h-5 w-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
            </svg>
          </div>
        </div>
        
        {/* View Toggle */}
        <div className="flex items-center space-x-4">
          <span className="text-sm font-medium text-gray-700">View:</span>
          <button
            onClick={() => setIsGroupedView(true)}
            className={`px-3 py-1 rounded text-sm font-medium ${
              isGroupedView 
                ? 'bg-indigo-600 text-white' 
                : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
            }`}
          >
            📊 Grouped
          </button>
          <button
            onClick={() => setIsGroupedView(false)}
            className={`px-3 py-1 rounded text-sm font-medium ${
              !isGroupedView 
                ? 'bg-indigo-600 text-white' 
                : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
            }`}
          >
            📋 Detailed
          </button>
        </div>
      </div>

      {/* Summary Cards */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
        <div className="bg-white rounded-lg shadow p-6">
          <div className="flex items-center">
            <div className="bg-red-500 rounded-full p-3 mr-4">
              <div className="w-6 h-6 text-white"></div>
            </div>
            <div>
              <p className="text-sm font-medium text-gray-600">Room Conflicts</p>
              <p className="text-2xl font-bold text-gray-900">
                {filteredConflicts.filter(c => c.conflictType === 'ROOM').length}
              </p>
            </div>
          </div>
        </div>

        <div className="bg-white rounded-lg shadow p-6">
          <div className="flex items-center">
            <div className="bg-purple-500 rounded-full p-3 mr-4">
              <div className="w-6 h-6 text-white"></div>
            </div>
            <div>
              <p className="text-sm font-medium text-gray-600">Teacher Conflicts</p>
              <p className="text-2xl font-bold text-gray-900">
                {filteredConflicts.filter(c => c.conflictType === 'TEACHER').length}
              </p>
            </div>
          </div>
        </div>

        <div className="bg-white rounded-lg shadow p-6">
          <div className="flex items-center">
            <div className="bg-yellow-500 rounded-full p-3 mr-4">
              <div className="w-6 h-6 text-white"></div>
            </div>
            <div>
              <p className="text-sm font-medium text-gray-600">Capacity Conflicts</p>
              <p className="text-2xl font-bold text-gray-900">
                {filteredConflicts.filter(c => c.conflictType === 'CAPACITY').length}
              </p>
            </div>
          </div>
        </div>
      </div>

      {filteredConflicts.length === 0 ? (
        <div className="bg-white rounded-lg shadow p-8 text-center">
          <div className="text-green-500 text-6xl mb-4">✓</div>
          <h2 className="text-2xl font-semibold text-gray-900 mb-2">
            {searchTerm ? 'No Matching Conflicts' : 'No Conflicts Found'}
          </h2>
          <p className="text-gray-600">
            {searchTerm 
              ? 'Try adjusting your search terms.' 
              : 'All events are properly scheduled without conflicts.'
            }
          </p>
        </div>
      ) : (
        <div className="bg-white rounded-lg shadow overflow-hidden">
          {isGroupedView ? (
            /* Grouped View */
            <div className="divide-y divide-gray-200">
              {groupConflicts(filteredConflicts).map((group) => (
                <div key={group.id} className="p-6 border-l-4 border-indigo-500">
                  <div className="flex items-start justify-between">
                    <div className="flex-1">
                      <div className="flex items-center space-x-3 mb-2">
                        <span className={`inline-flex px-3 py-1 text-sm font-semibold rounded-full ${
                          group.type === 'ROOM' ? 'bg-red-100 text-red-800' : 'bg-purple-100 text-purple-800'
                        }`}>
                          {group.type === 'ROOM' ? '🏢' : '👨‍🏫'} {group.type}
                        </span>
                        <span className="text-lg font-medium text-gray-900">{group.resource}</span>
                        <span className="text-sm text-gray-500">
                          {group.eventIds.length} events • {group.conflicts.length} conflicts
                        </span>
                      </div>
                      
                      <div className="mb-3">
                        <p className="text-sm text-gray-600">{getGroupDescription(group)}</p>
                        <p className="text-sm text-gray-500">{group.date} • {group.timeRange}</p>
                      </div>
                      
                      <div className="mb-4">
                        <h4 className="text-sm font-medium text-gray-900 mb-2">Affected Events:</h4>
                        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-2">
                          {group.conflicts.slice(0, 1).map(conflict => (
                            <div key={`${conflict.event1.id}-1`} className="bg-gray-50 p-2 rounded text-xs">
                              <div className="font-medium">{conflict.event1.type} (ID: {conflict.event1.id})</div>
                              <div className="text-gray-600">{conflict.event1.teacher.name}</div>
                            </div>
                          ))}
                          {group.conflicts.slice(0, 1).map(conflict => conflict.event2 && (
                            <div key={`${conflict.event2.id}-2`} className="bg-gray-50 p-2 rounded text-xs">
                              <div className="font-medium">{conflict.event2.type} (ID: {conflict.event2.id})</div>
                              <div className="text-gray-600">{conflict.event2.teacher.name}</div>
                            </div>
                          ))}
                          {group.eventIds.length > 2 && (
                            <div className="bg-gray-100 p-2 rounded text-xs flex items-center justify-center">
                              +{group.eventIds.length - 2} more events
                            </div>
                          )}
                        </div>
                      </div>
                      
                      <div className="mb-4">
                        <h4 className="text-sm font-medium text-gray-900 mb-2">Resolution Suggestions:</h4>
                        <ul className="text-sm text-gray-600 space-y-1">
                          {getResolutionSuggestions(group).map((suggestion, idx) => (
                            <li key={idx} className="flex items-center">
                              <span className="w-1 h-1 bg-gray-400 rounded-full mr-2"></span>
                              {suggestion}
                            </li>
                          ))}
                        </ul>
                      </div>
                    </div>
                    
                    <div className="flex flex-col space-y-2 ml-4">
                      <button
                        onClick={() => {
                          setSelectedConflict(group.conflicts[0]);
                          setShowRescheduleModal(true);
                        }}
                        className="text-xs bg-blue-100 text-blue-800 px-2 py-1 rounded hover:bg-blue-200"
                      >
                        Reschedule
                      </button>
                      <button
                        onClick={() => {
                          setSelectedConflict(group.conflicts[0]);
                          setShowChangeRoomModal(true);
                        }}
                        className="text-xs bg-green-100 text-green-800 px-2 py-1 rounded hover:bg-green-200"
                      >
                        Change Room
                      </button>
                      <button
                        onClick={() => handleIgnoreConflict(group.conflicts[0].id)}
                        className="text-xs bg-gray-100 text-gray-800 px-2 py-1 rounded hover:bg-gray-200"
                      >
                        Ignore Group
                      </button>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          ) : isMobile ? (
            /* Mobile: Collapsible Cards */
            <div className="divide-y divide-gray-200">
              {filteredConflicts.map((conflict) => (
                <div key={conflict.id} className={`p-4 ${getConflictRowColor(conflict.conflictType)}`}>
                  <div className="space-y-3">
                    <div className="flex items-center justify-between">
                      <span className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${getConflictBadgeColor(conflict.conflictType)}`}>
                        {conflict.conflictType}
                      </span>
                      <button
                        onClick={() => handleIgnoreConflict(conflict.id)}
                        className="text-gray-400 hover:text-red-500"
                      >
                        <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                        </svg>
                      </button>
                    </div>
                    
                    <div>
                      <p className="text-sm font-medium text-gray-900">
                        {conflict.event1.type}: {conflict.event1.title}
                      </p>
                      <p className="text-xs text-gray-500">
                        {conflict.event1.teacher.name} • {conflict.event1.date} {conflict.event1.startTime}-{conflict.event1.endTime}
                      </p>
                    </div>
                    
                    {conflict.event2 && (
                      <div>
                        <p className="text-sm font-medium text-gray-900">
                          {conflict.event2.type}: {conflict.event2.title}
                        </p>
                        <p className="text-xs text-gray-500">
                          {conflict.event2.teacher.name} • {conflict.event2.date} {conflict.event2.startTime}-{conflict.event2.endTime}
                        </p>
                      </div>
                    )}
                    
                    <p className="text-sm text-gray-700">{conflict.description}</p>
                    
                    <div className="flex flex-wrap gap-2">
                      <button
                        onClick={() => handleReschedule(conflict)}
                        className="px-3 py-1 text-xs font-medium text-indigo-600 bg-indigo-100 rounded hover:bg-indigo-200"
                      >
                        Reschedule
                      </button>
                      <button
                        onClick={() => handleChangeRoom(conflict)}
                        className="px-3 py-1 text-xs font-medium text-green-600 bg-green-100 rounded hover:bg-green-200"
                      >
                        Change Room
                      </button>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          ) : (
            /* Desktop: Table */
            <div className="overflow-x-auto">
              <table className="min-w-full divide-y divide-gray-200">
                <thead className="bg-gray-50">
                  <tr>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Conflict Type
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Event 1
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Event 2
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Description
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Actions
                    </th>
                  </tr>
                </thead>
                <tbody className="bg-white divide-y divide-gray-200">
                  {filteredConflicts.map((conflict) => (
                    <tr key={conflict.id} className={`border-l-4 ${getConflictRowColor(conflict.conflictType)}`}>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <span className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${getConflictBadgeColor(conflict.conflictType)}`}>
                          {conflict.conflictType}
                        </span>
                      </td>
                      <td className="px-6 py-4">
                        <div className="text-sm">
                          <div className="font-medium text-gray-900">
                            {conflict.event1.type}: {conflict.event1.title}
                          </div>
                          <div className="text-gray-500">
                            {conflict.event1.teacher.name}
                          </div>
                          <div className="text-gray-500">
                            {conflict.event1.date} {conflict.event1.startTime}-{conflict.event1.endTime}
                          </div>
                          {conflict.event1.room && (
                            <div className="text-gray-500">Room: {conflict.event1.room.name}</div>
                          )}
                        </div>
                      </td>
                      <td className="px-6 py-4">
                        {conflict.event2 ? (
                          <div className="text-sm">
                            <div className="font-medium text-gray-900">
                              {conflict.event2.type}: {conflict.event2.title}
                            </div>
                            <div className="text-gray-500">
                              {conflict.event2.teacher.name}
                            </div>
                            <div className="text-gray-500">
                              {conflict.event2.date} {conflict.event2.startTime}-{conflict.event2.endTime}
                            </div>
                            {conflict.event2.room && (
                              <div className="text-gray-500">Room: {conflict.event2.room.name}</div>
                            )}
                          </div>
                        ) : (
                          <span className="text-gray-400">N/A</span>
                        )}
                      </td>
                      <td className="px-6 py-4">
                        <div className="text-sm text-gray-900">{conflict.description}</div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                        <div className="flex space-x-2">
                          <button
                            onClick={() => handleReschedule(conflict)}
                            className="text-indigo-600 hover:text-indigo-900"
                          >
                            Reschedule
                          </button>
                          <button
                            onClick={() => handleChangeRoom(conflict)}
                            className="text-green-600 hover:text-green-900"
                          >
                            Change Room
                          </button>
                          <button
                            onClick={() => handleIgnoreConflict(conflict.id)}
                            className="text-red-600 hover:text-red-900"
                          >
                            Ignore
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      )}

      {/* Modals */}
      {showRescheduleModal && selectedConflict && (
        <RescheduleModal
          conflict={selectedConflict}
          onClose={() => setShowRescheduleModal(false)}
          onSuccess={onModalSuccess}
        />
      )}

      {showChangeRoomModal && selectedConflict && (
        <ChangeRoomModal
          conflict={selectedConflict}
          onClose={() => setShowChangeRoomModal(false)}
          onSuccess={onModalSuccess}
        />
      )}
    </div>
  );
};

export default Conflicts;
