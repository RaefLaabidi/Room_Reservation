import { Conflict, ConflictType } from '../types';

interface ConflictGroup {
  id: string;
  type: ConflictType;
  resource: string; // Room name or Teacher name
  date: string;
  timeRange: string;
  eventIds: number[];
  conflicts: Conflict[];
  priority: 'HIGH' | 'MEDIUM' | 'LOW';
}

export const groupConflicts = (conflicts: Conflict[]): ConflictGroup[] => {
  const groups = new Map<string, ConflictGroup>();

  conflicts.forEach(conflict => {
    // Create a key based on type, resource, date, and time
    const key = `${conflict.conflictType}-${getResourceName(conflict)}-${conflict.event1?.date}-${getTimeRange(conflict)}`;
    
    if (!groups.has(key)) {
      groups.set(key, {
        id: key,
        type: conflict.conflictType,
        resource: getResourceName(conflict),
        date: conflict.event1?.date || '',
        timeRange: getTimeRange(conflict),
        eventIds: [],
        conflicts: [],
        priority: 'HIGH'
      });
    }

    const group = groups.get(key)!;
    group.conflicts.push(conflict);
    
    // Add unique event IDs
    if (conflict.event1?.id && !group.eventIds.includes(conflict.event1.id)) {
      group.eventIds.push(conflict.event1.id);
    }
    if (conflict.event2?.id && !group.eventIds.includes(conflict.event2.id)) {
      group.eventIds.push(conflict.event2.id);
    }
  });

  return Array.from(groups.values());
};

const getResourceName = (conflict: Conflict): string => {
  if (conflict.conflictType === 'ROOM') {
    return conflict.event1?.room?.name || 'Unknown Room';
  } else if (conflict.conflictType === 'TEACHER') {
    return conflict.event1?.teacher?.name || 'Unknown Teacher';
  }
  return 'Unknown';
};

const getTimeRange = (conflict: Conflict): string => {
  const start = conflict.event1?.startTime || '';
  const end = conflict.event1?.endTime || '';
  return `${start}-${end}`;
};

export const getGroupDescription = (group: ConflictGroup): string => {
  const eventCount = group.eventIds.length;
  const conflictCount = group.conflicts.length;
  
  if (eventCount === 2) {
    return `${group.resource} conflict between 2 events`;
  } else {
    return `${group.resource} conflicts among ${eventCount} events (${conflictCount} total conflicts)`;
  }
};

export const getResolutionSuggestions = (group: ConflictGroup): string[] => {
  const suggestions = [];
  
  if (group.type === 'ROOM') {
    suggestions.push(`Move ${group.eventIds.length - 1} event(s) to different room(s)`);
    suggestions.push(`Reschedule ${group.eventIds.length - 1} event(s) to different time(s)`);
  } else if (group.type === 'TEACHER') {
    suggestions.push(`Assign different teacher(s) to ${group.eventIds.length - 1} event(s)`);
    suggestions.push(`Reschedule ${group.eventIds.length - 1} event(s) to different time(s)`);
  }
  
  suggestions.push('Contact administrator for assistance');
  
  return suggestions;
};
