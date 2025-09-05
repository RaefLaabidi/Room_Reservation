import React, { useState, useEffect } from 'react';

interface AnalyticsData {
  totalScheduledEvents: number;
  totalUniqueCourses: number;
  totalRoomsUsed: number;
  roomUtilization: Record<string, number>;
  timeDistribution: Record<string, number>;
  dayDistribution: Record<string, number>;
  courseDistribution: Record<string, number>;
  conflictAnalysis?: {
    totalConflicts: number;
    resolvedConflicts: number;
    pendingConflicts: number;
  };
  utilizationMetrics?: {
    averageRoomUtilization: number;
    peakHours: string[];
    lowUtilizationRooms: string[];
  };
}

interface RoomUtilizationData {
  roomUsage: Record<string, number>;
  roomCapacity: Record<string, number>;
  utilizationPercentage: Record<string, number>;
  totalRooms: number;
  averageUtilization: number;
}

interface TimeDistributionData {
  hourlyDistribution: Record<string, number>;
  dailyDistribution: Record<string, number>;
  peakHour: string;
  peakDay: string;
  totalEvents: number;
}

interface SubjectRoomMatchingData {
  courseRoomCount: Record<string, number>;
  roomCourseCount: Record<string, number>;
  mostVersatileRoom: string;
  mostMobileCourse: string;
  totalUniqueCombinations: number;
}

// StatCard component
const StatCard: React.FC<{
  title: string;
  value: string | number;
  subtitle?: string;
  color?: 'blue' | 'green' | 'yellow' | 'purple' | 'red';
}> = ({ title, value, subtitle, color = 'blue' }) => {
  const colorClasses = {
    blue: 'border-blue-200 bg-blue-50',
    green: 'border-green-200 bg-green-50',
    yellow: 'border-yellow-200 bg-yellow-50',
    purple: 'border-purple-200 bg-purple-50',
    red: 'border-red-200 bg-red-50',
  };

  return (
    <div className={`p-4 rounded-lg border-2 ${colorClasses[color]}`}>
      <h3 className="text-sm font-medium text-gray-600 mb-1">{title}</h3>
      <p className="text-2xl font-bold text-gray-900">{value}</p>
      {subtitle && <p className="text-xs text-gray-500 mt-1">{subtitle}</p>}
    </div>
  );
};

// SimpleBarChart component
const SimpleBarChart: React.FC<{
  data: Record<string, number>;
  title: string;
  color?: string;
}> = ({ data, title, color = '#3b82f6' }) => {
  if (!data || Object.keys(data).length === 0) {
    return (
      <div className="bg-white p-4 rounded-lg shadow">
        <h3 className="text-lg font-semibold mb-4">{title}</h3>
        <p className="text-gray-500 text-center py-8">No data available</p>
      </div>
    );
  }

  const maxValue = Math.max(...Object.values(data));
  
  return (
    <div className="bg-white p-4 rounded-lg shadow">
      <h3 className="text-lg font-semibold mb-4">{title}</h3>
      <div className="space-y-2 max-h-64 overflow-y-auto">
        {Object.entries(data)
          .sort(([,a], [,b]) => b - a) // Sort by value descending
          .slice(0, 10) // Limit to top 10 items
          .map(([key, value]) => (
          <div key={key} className="flex items-center">
            <div className="w-24 text-sm text-right mr-2 text-gray-600 truncate">
              {key}
            </div>
            <div className="flex-1 bg-gray-200 rounded-full h-6 relative">
              <div
                className="h-6 rounded-full flex items-center justify-end pr-2"
                style={{
                  width: `${maxValue > 0 ? (value / maxValue) * 100 : 0}%`,
                  backgroundColor: color,
                  minWidth: value > 0 ? '20px' : '0px'
                }}
              >
                <span className="text-white text-xs font-medium">
                  {value}
                </span>
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};

const SchedulingAnalyticsDashboard: React.FC = () => {
  const [activeTab, setActiveTab] = useState('overview');
  const [analyticsData, setAnalyticsData] = useState<AnalyticsData | null>(null);
  const [roomUtilization, setRoomUtilization] = useState<RoomUtilizationData | null>(null);
  const [timeDistribution, setTimeDistribution] = useState<TimeDistributionData | null>(null);
  const [subjectRoomMatching, setSubjectRoomMatching] = useState<SubjectRoomMatchingData | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchAnalyticsData = async () => {
    try {
      setLoading(true);
      setError(null);
      const token = localStorage.getItem('token');
      
      const headers: HeadersInit = {
        'Content-Type': 'application/json',
      };

      if (token) {
        headers['Authorization'] = `Bearer ${token}`;
      }

      console.log('Fetching analytics data...');

      // Try different base URLs to find the working one
      const baseUrls = [
        '/api/weekly-schedule',
        'http://localhost:8080/api/weekly-schedule'
      ];

      let workingBaseUrl = '';

      // Test connectivity first
      for (const baseUrl of baseUrls) {
        try {
          console.log(`Testing URL: ${baseUrl}/analytics/test`);
          const testRes = await fetch(`${baseUrl}/analytics/test`, { headers });
          if (testRes.ok) {
            const testData = await testRes.json();
            console.log(`SUCCESS with ${baseUrl}:`, testData);
            
            // Display database counts for debugging
            if (testData.database) {
              console.log(`📊 Database Stats:`, testData.database);
              console.log(`📊 Events in DB: ${testData.database.eventCount}`);
              console.log(`📊 Rooms in DB: ${testData.database.roomCount}`);
              console.log(`📊 Courses in DB: ${testData.database.courseCount}`);
            }
            
            workingBaseUrl = baseUrl;
            break;
          } else {
            console.log(`Failed ${baseUrl} - Status: ${testRes.status}`);
          }
        } catch (error) {
          console.log(`Error testing ${baseUrl}:`, error);
        }
      }

      if (!workingBaseUrl) {
        throw new Error('Unable to connect to analytics service');
      }

      console.log(`Using base URL: ${workingBaseUrl}`);

      // Fetch analytics data with individual error handling
      let overview, room, time, matching;

      // Fetch overview analytics
      try {
        console.log('Fetching overview analytics...');
        const overviewRes = await fetch(`${workingBaseUrl}/analytics`, { headers });
        if (overviewRes.ok) {
          overview = await overviewRes.json();
          console.log('Overview analytics loaded successfully');
        } else {
          const errorText = await overviewRes.text();
          console.error(`Overview analytics failed: ${overviewRes.status} - ${errorText}`);
          overview = { totalScheduledEvents: 0, totalUniqueCourses: 0, totalRoomsUsed: 0, roomUtilization: {}, timeDistribution: {}, dayDistribution: {}, courseDistribution: {} };
        }
      } catch (error) {
        console.error('Overview analytics error:', error);
        overview = { totalScheduledEvents: 0, totalUniqueCourses: 0, totalRoomsUsed: 0, roomUtilization: {}, timeDistribution: {}, dayDistribution: {}, courseDistribution: {} };
      }

      // Fetch room analytics
      try {
        console.log('Fetching room analytics...');
        const roomRes = await fetch(`${workingBaseUrl}/analytics/room-utilization`, { headers });
        if (roomRes.ok) {
          room = await roomRes.json();
          console.log('Room analytics loaded successfully');
        } else {
          const errorText = await roomRes.text();
          console.error(`Room analytics failed: ${roomRes.status} - ${errorText}`);
          room = { roomUsage: {}, roomCapacity: {}, utilizationPercentage: {}, totalRooms: 0, averageUtilization: 0 };
        }
      } catch (error) {
        console.error('Room analytics error:', error);
        room = { roomUsage: {}, roomCapacity: {}, utilizationPercentage: {}, totalRooms: 0, averageUtilization: 0 };
      }

      // Fetch time analytics
      try {
        console.log('Fetching time analytics...');
        const timeRes = await fetch(`${workingBaseUrl}/analytics/time-distribution`, { headers });
        if (timeRes.ok) {
          time = await timeRes.json();
          console.log('Time analytics loaded successfully');
        } else {
          const errorText = await timeRes.text();
          console.error(`Time analytics failed: ${timeRes.status} - ${errorText}`);
          time = { hourlyDistribution: {}, dailyDistribution: {}, peakHour: 'N/A', peakDay: 'N/A', totalEvents: 0 };
        }
      } catch (error) {
        console.error('Time analytics error:', error);
        time = { hourlyDistribution: {}, dailyDistribution: {}, peakHour: 'N/A', peakDay: 'N/A', totalEvents: 0 };
      }

      // Fetch matching analytics
      try {
        console.log('Fetching matching analytics...');
        const matchingRes = await fetch(`${workingBaseUrl}/analytics/subject-room-matching`, { headers });
        if (matchingRes.ok) {
          matching = await matchingRes.json();
          console.log('Matching analytics loaded successfully');
        } else {
          const errorText = await matchingRes.text();
          console.error(`Matching analytics failed: ${matchingRes.status} - ${errorText}`);
          matching = { courseRoomCount: {}, roomCourseCount: {}, mostVersatileRoom: 'N/A', mostMobileCourse: 'N/A', totalUniqueCombinations: 0 };
        }
      } catch (error) {
        console.error('Matching analytics error:', error);
        matching = { courseRoomCount: {}, roomCourseCount: {}, mostVersatileRoom: 'N/A', mostMobileCourse: 'N/A', totalUniqueCombinations: 0 };
      }

      console.log('Analytics data fetched successfully:', { overview, room, time, matching });

      // Set the data with fallback values
      setAnalyticsData({
        totalScheduledEvents: overview.totalScheduledEvents || 0,
        totalUniqueCourses: overview.totalUniqueCourses || 0,
        totalRoomsUsed: overview.totalRoomsUsed || 0,
        roomUtilization: overview.roomUtilization || {},
        timeDistribution: overview.timeDistribution || {},
        dayDistribution: overview.dayDistribution || {},
        courseDistribution: overview.courseDistribution || {},
        conflictAnalysis: overview.conflictAnalysis,
        utilizationMetrics: overview.utilizationMetrics
      });

      setRoomUtilization({
        roomUsage: room.roomUsage || {},
        roomCapacity: room.roomCapacity || {},
        utilizationPercentage: room.utilizationPercentage || {},
        totalRooms: room.totalRooms || 0,
        averageUtilization: room.averageUtilization || 0
      });

      setTimeDistribution({
        hourlyDistribution: time.hourlyDistribution || {},
        dailyDistribution: time.dailyDistribution || {},
        peakHour: time.peakHour || 'N/A',
        peakDay: time.peakDay || 'N/A',
        totalEvents: time.totalEvents || 0
      });

      setSubjectRoomMatching({
        courseRoomCount: matching.courseRoomCount || {},
        roomCourseCount: matching.roomCourseCount || {},
        mostVersatileRoom: matching.mostVersatileRoom || 'N/A',
        mostMobileCourse: matching.mostMobileCourse || 'N/A',
        totalUniqueCombinations: matching.totalUniqueCombinations || 0
      });

    } catch (err) {
      console.error('Analytics fetch error:', err);
      setError(err instanceof Error ? err.message : 'Failed to fetch analytics data');
      
      // Set default empty data
      setAnalyticsData({
        totalScheduledEvents: 0,
        totalUniqueCourses: 0,
        totalRoomsUsed: 0,
        roomUtilization: {},
        timeDistribution: {},
        dayDistribution: {},
        courseDistribution: {}
      });
      
      setRoomUtilization({
        roomUsage: {},
        roomCapacity: {},
        utilizationPercentage: {},
        totalRooms: 0,
        averageUtilization: 0
      });
      
      setTimeDistribution({
        hourlyDistribution: {},
        dailyDistribution: {},
        peakHour: 'N/A',
        peakDay: 'N/A',
        totalEvents: 0
      });
      
      setSubjectRoomMatching({
        courseRoomCount: {},
        roomCourseCount: {},
        mostVersatileRoom: 'N/A',
        mostMobileCourse: 'N/A',
        totalUniqueCombinations: 0
      });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchAnalyticsData();
  }, []);

  if (loading) {
    return (
      <div className="flex justify-center items-center h-64">
        <div className="text-lg text-gray-600">Loading analytics...</div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="bg-red-50 border border-red-200 rounded-lg p-4">
        <div className="text-red-800 mb-2">Error: {error}</div>
        <button 
          onClick={fetchAnalyticsData}
          className="px-4 py-2 bg-red-600 text-white rounded hover:bg-red-700"
        >
          Retry
        </button>
      </div>
    );
  }

  const tabs = [
    { id: 'overview', name: 'Overview', icon: '📊' },
    { id: 'rooms', name: 'Room Analysis', icon: '🏛️' },
    { id: 'time', name: 'Time Analysis', icon: '⏰' },
    { id: 'matching', name: 'Course-Room Matching', icon: '🔗' },
  ];

  return (
    <div className="p-6 max-w-7xl mx-auto">
      <div className="mb-6">
        <h1 className="text-3xl font-bold text-gray-900 mb-2">📊 Scheduling Analytics Dashboard</h1>
        <p className="text-gray-600">Comprehensive analysis of your scheduling data</p>
      </div>

      {/* Tab Navigation */}
      <div className="mb-6">
        <div className="border-b border-gray-200">
          <nav className="-mb-px flex space-x-8">
            {tabs.map((tab) => (
              <button
                key={tab.id}
                onClick={() => setActiveTab(tab.id)}
                className={`py-2 px-1 border-b-2 font-medium text-sm whitespace-nowrap ${
                  activeTab === tab.id
                    ? 'border-blue-500 text-blue-600'
                    : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                }`}
              >
                <span className="mr-2">{tab.icon}</span>
                {tab.name}
              </button>
            ))}
          </nav>
        </div>
      </div>

      {/* Tab Content */}
      {activeTab === 'overview' && analyticsData && (
        <div className="space-y-6">
          {/* Key Metrics */}
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <StatCard
              title="Total Scheduled Events"
              value={analyticsData.totalScheduledEvents}
              color="blue"
            />
            <StatCard
              title="Unique Courses"
              value={analyticsData.totalUniqueCourses}
              color="green"
            />
            <StatCard
              title="Rooms in Use"
              value={analyticsData.totalRoomsUsed}
              color="purple"
            />
          </div>

          {/* Charts */}
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            <SimpleBarChart
              data={analyticsData.roomUtilization}
              title="Room Utilization"
              color="#10b981"
            />
            <SimpleBarChart
              data={analyticsData.timeDistribution}
              title="Time Distribution"
              color="#f59e0b"
            />
          </div>

          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            <SimpleBarChart
              data={analyticsData.dayDistribution}
              title="Daily Distribution"
              color="#8b5cf6"
            />
            <SimpleBarChart
              data={analyticsData.courseDistribution}
              title="Course Frequency"
              color="#ef4444"
            />
          </div>

          {/* Additional Metrics */}
          {analyticsData.conflictAnalysis && (
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
              <StatCard
                title="Total Conflicts"
                value={analyticsData.conflictAnalysis.totalConflicts}
                color="red"
              />
              <StatCard
                title="Resolved Conflicts"
                value={analyticsData.conflictAnalysis.resolvedConflicts}
                color="green"
              />
              <StatCard
                title="Pending Conflicts"
                value={analyticsData.conflictAnalysis.pendingConflicts}
                color="yellow"
              />
            </div>
          )}
        </div>
      )}

      {activeTab === 'rooms' && roomUtilization && (
        <div className="space-y-6">
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <StatCard
              title="Total Rooms"
              value={roomUtilization.totalRooms}
              color="blue"
            />
            <StatCard
              title="Average Utilization"
              value={`${roomUtilization.averageUtilization.toFixed(1)}%`}
              color="green"
            />
            <StatCard
              title="Most Used Rooms"
              value={Object.keys(roomUtilization.roomUsage).length}
              subtitle="rooms in use"
              color="purple"
            />
          </div>

          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            <SimpleBarChart
              data={roomUtilization.roomUsage}
              title="Room Usage Count"
              color="#3b82f6"
            />
            <SimpleBarChart
              data={roomUtilization.utilizationPercentage}
              title="Utilization Percentage"
              color="#10b981"
            />
          </div>

          <SimpleBarChart
            data={roomUtilization.roomCapacity}
            title="Room Capacity"
            color="#f59e0b"
          />
        </div>
      )}

      {activeTab === 'time' && timeDistribution && (
        <div className="space-y-6">
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <StatCard
              title="Peak Hour"
              value={timeDistribution.peakHour}
              color="blue"
            />
            <StatCard
              title="Peak Day"
              value={timeDistribution.peakDay}
              color="green"
            />
            <StatCard
              title="Total Events"
              value={timeDistribution.totalEvents}
              color="purple"
            />
          </div>

          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            <SimpleBarChart
              data={timeDistribution.hourlyDistribution}
              title="Hourly Distribution"
              color="#3b82f6"
            />
            <SimpleBarChart
              data={timeDistribution.dailyDistribution}
              title="Daily Distribution"
              color="#10b981"
            />
          </div>
        </div>
      )}

      {activeTab === 'matching' && subjectRoomMatching && (
        <div className="space-y-6">
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <StatCard
              title="Most Versatile Room"
              value={subjectRoomMatching.mostVersatileRoom}
              subtitle="hosts most courses"
              color="blue"
            />
            <StatCard
              title="Most Mobile Course"
              value={subjectRoomMatching.mostMobileCourse}
              subtitle="uses most rooms"
              color="green"
            />
            <StatCard
              title="Unique Combinations"
              value={subjectRoomMatching.totalUniqueCombinations}
              color="purple"
            />
          </div>

          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            <SimpleBarChart
              data={subjectRoomMatching.courseRoomCount}
              title="Courses by Room Count"
              color="#3b82f6"
            />
            <SimpleBarChart
              data={subjectRoomMatching.roomCourseCount}
              title="Rooms by Course Count"
              color="#10b981"
            />
          </div>
        </div>
      )}
    </div>
  );
};

export default SchedulingAnalyticsDashboard;
