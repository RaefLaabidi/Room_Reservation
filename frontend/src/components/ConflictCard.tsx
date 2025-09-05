import React from 'react';
import { Conflict, ConflictType } from '../types/index.js';

interface ConflictCardProps {
  conflict: Conflict;
}

const ConflictCard: React.FC<ConflictCardProps> = ({ conflict }) => {
  const getConflictColor = (type: ConflictType) => {
    switch (type) {
      case ConflictType.ROOM:
        return 'bg-red-50 border-red-200';
      case ConflictType.TEACHER:
        return 'bg-orange-50 border-orange-200';
      case ConflictType.CAPACITY:
        return 'bg-yellow-50 border-yellow-200';
      default:
        return 'bg-gray-50 border-gray-200';
    }
  };

  const getConflictBadgeColor = (type: ConflictType) => {
    switch (type) {
      case ConflictType.ROOM:
        return 'bg-red-100 text-red-800';
      case ConflictType.TEACHER:
        return 'bg-orange-100 text-orange-800';
      case ConflictType.CAPACITY:
        return 'bg-yellow-100 text-yellow-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  };

  return (
    <div className={`border rounded-lg p-4 ${getConflictColor(conflict.conflictType)}`}>
      <div className="flex items-start justify-between mb-3">
        <span className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${getConflictBadgeColor(conflict.conflictType)}`}>
          {conflict.conflictType} CONFLICT
        </span>
      </div>

      <p className="text-gray-800 mb-4">{conflict.description}</p>

      <div className="space-y-3">
        {/* Event 1 */}
        <div className="bg-white rounded p-3 border">
          <h4 className="font-semibold text-gray-900 mb-2">
            {conflict.event1.title || conflict.event1.type}
          </h4>
          <div className="grid grid-cols-2 gap-4 text-sm">
            <div>
              <span className="text-gray-600">Date & Time:</span>
              <p className="font-medium">
                {conflict.event1.date} | {conflict.event1.startTime} - {conflict.event1.endTime}
              </p>
            </div>
            <div>
              <span className="text-gray-600">Teacher:</span>
              <p className="font-medium">{conflict.event1.teacher.name}</p>
            </div>
            {conflict.event1.room && (
              <div>
                <span className="text-gray-600">Room:</span>
                <p className="font-medium">{conflict.event1.room.name}</p>
              </div>
            )}
            {conflict.event1.expectedParticipants && (
              <div>
                <span className="text-gray-600">Participants:</span>
                <p className="font-medium">{conflict.event1.expectedParticipants}</p>
              </div>
            )}
          </div>
        </div>

        {/* Event 2 (if exists) */}
        {conflict.event2 && (
          <div className="bg-white rounded p-3 border">
            <h4 className="font-semibold text-gray-900 mb-2">
              {conflict.event2.title || conflict.event2.type}
            </h4>
            <div className="grid grid-cols-2 gap-4 text-sm">
              <div>
                <span className="text-gray-600">Date & Time:</span>
                <p className="font-medium">
                  {conflict.event2.date} | {conflict.event2.startTime} - {conflict.event2.endTime}
                </p>
              </div>
              <div>
                <span className="text-gray-600">Teacher:</span>
                <p className="font-medium">{conflict.event2.teacher.name}</p>
              </div>
              {conflict.event2.room && (
                <div>
                  <span className="text-gray-600">Room:</span>
                  <p className="font-medium">{conflict.event2.room.name}</p>
                </div>
              )}
              {conflict.event2.expectedParticipants && (
                <div>
                  <span className="text-gray-600">Participants:</span>
                  <p className="font-medium">{conflict.event2.expectedParticipants}</p>
                </div>
              )}
            </div>
          </div>
        )}
      </div>

      {/* Action buttons could be added here */}
      <div className="mt-4 flex justify-end space-x-2">
        <button className="px-3 py-1 text-sm bg-blue-100 text-blue-700 rounded hover:bg-blue-200">
          Reschedule
        </button>
        <button className="px-3 py-1 text-sm bg-green-100 text-green-700 rounded hover:bg-green-200">
          Change Room
        </button>
      </div>
    </div>
  );
};

export default ConflictCard;
