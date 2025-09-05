import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import Login from './pages/Login.js';
import Register from './pages/Register.js';
import Dashboard from './pages/Dashboard.js';
import TeacherDashboard from './pages/TeacherDashboard.js';
import StudentDashboard from './pages/StudentDashboard.js';
import Schedule from './pages/Schedule.js';
import Conflicts from './pages/Conflicts.js';
import Navbar from './components/Navbar';
import CoursesManagement from './components/CoursesManagement';
import ScheduleTemplatesManagement from './components/ScheduleTemplatesManagement';
import WeeklyScheduleCreator from './components/WeeklyScheduleCreator';
import SchedulingAnalyticsDashboard from './components/SchedulingAnalyticsDashboard';
import WeeklySchedule from './components/WeeklySchedule';
import AIChatbot from './components/AIChatbot';

const ProtectedRoute = ({ children }: { children: React.ReactNode }) => {
  const token = localStorage.getItem('token');
  return token ? <>{children}</> : <Navigate to="/login" />;
};

const AdminRoute = ({ children }: { children: React.ReactNode }) => {
  const token = localStorage.getItem('token');
  const user = localStorage.getItem('user');
  
  if (!token) {
    return <Navigate to="/login" />;
  }
  
  if (user) {
    const currentUser = JSON.parse(user);
    if (currentUser.role !== 'ADMIN') {
      // Redirect non-admin users to their appropriate dashboard
      switch (currentUser.role) {
        case 'TEACHER':
          return <Navigate to="/teacher-dashboard" />;
        case 'STUDENT':
          return <Navigate to="/student-dashboard" />;
        default:
          return <Navigate to="/login" />;
      }
    }
  }
  
  return <>{children}</>;
};

const RoleBasedRedirect = () => {
  const user = localStorage.getItem('user');
  
  if (user) {
    const currentUser = JSON.parse(user);
    switch (currentUser.role) {
      case 'ADMIN':
        return <Navigate to="/dashboard" />;
      case 'TEACHER':
        return <Navigate to="/teacher-dashboard" />;
      case 'STUDENT':
        return <Navigate to="/student-dashboard" />;
      default:
        return <Navigate to="/login" />;
    }
  }
  
  return <Navigate to="/login" />;
};

function App() {
  const token = localStorage.getItem('token');

  return (
    <Router>
      <div className="min-h-screen bg-gray-100">
        {token && <Navbar />}
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
          <Route
            path="/dashboard"
            element={
              <AdminRoute>
                <Dashboard />
              </AdminRoute>
            }
          />
          <Route
            path="/teacher-dashboard"
            element={
              <ProtectedRoute>
                <TeacherDashboard />
              </ProtectedRoute>
            }
          />
          <Route
            path="/student-dashboard"
            element={
              <ProtectedRoute>
                <StudentDashboard />
              </ProtectedRoute>
            }
          />
          <Route
            path="/schedule"
            element={
              <AdminRoute>
                <Schedule />
              </AdminRoute>
            }
          />
          <Route
            path="/conflicts"
            element={
              <AdminRoute>
                <Conflicts />
              </AdminRoute>
            }
          />
          <Route
            path="/courses"
            element={
              <AdminRoute>
                <CoursesManagement />
              </AdminRoute>
            }
          />
          <Route
            path="/schedule-templates"
            element={
              <AdminRoute>
                <ScheduleTemplatesManagement />
              </AdminRoute>
            }
          />
          <Route
            path="/weekly-schedule"
            element={
              <AdminRoute>
                <WeeklyScheduleCreator />
              </AdminRoute>
            }
          />
          <Route
            path="/analytics"
            element={
              <AdminRoute>
                <SchedulingAnalyticsDashboard />
              </AdminRoute>
            }
          />
          <Route
            path="/calendar"
            element={
              <ProtectedRoute>
                <WeeklySchedule userType="admin" />
              </ProtectedRoute>
            }
          />
          <Route
            path="/calendar/professor/:email"
            element={
              <ProtectedRoute>
                <WeeklySchedule userType="professor" />
              </ProtectedRoute>
            }
          />
          <Route
            path="/calendar/student/:email"
            element={
              <ProtectedRoute>
                <WeeklySchedule userType="student" />
              </ProtectedRoute>
            }
          />
          <Route path="/" element={<RoleBasedRedirect />} />
        </Routes>
        
        {/* AI Chatbot - Available on all authenticated pages */}
        <ProtectedRoute>
          <AIChatbot />
        </ProtectedRoute>
      </div>
    </Router>
  );
}

export default App;
