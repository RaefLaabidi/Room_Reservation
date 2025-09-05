import React from 'react';
import { useNavigate } from 'react-router-dom';

const Navbar: React.FC = () => {
  const navigate = useNavigate();
  const currentUser = JSON.parse(localStorage.getItem('user') || '{}');

  const handleLogout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    navigate('/login');
  };

  // Different navigation items based on user role
  const getNavItems = () => {
    switch (currentUser.role) {
      case 'ADMIN':
        return [
          { name: 'Dashboard', path: '/dashboard' },
          { name: 'Schedule', path: '/schedule' },
          { name: 'Conflicts', path: '/conflicts' },
          { name: 'Courses', path: '/courses' },
          { name: 'Schedule Templates', path: '/schedule-templates' },
          { name: 'Weekly Schedule', path: '/weekly-schedule' },
          { name: '📊 Analytics', path: '/analytics' },
          { name: '📅 Calendar', path: '/calendar' },
        ];
      case 'TEACHER':
        return [
          { name: 'My Dashboard', path: '/teacher-dashboard' },
          { name: '📅 My Schedule', path: `/calendar/professor/${currentUser.email}` },
        ];
      case 'STUDENT':
        return [
          { name: 'My Dashboard', path: '/student-dashboard' },
          { name: '📅 My Classes', path: `/calendar/student/${currentUser.email}` },
        ];
      default:
        return [];
    }
  };

  const navItems = getNavItems();

  return (
    <nav className="bg-white shadow-lg">
      <div className="container mx-auto px-4">
        <div className="flex justify-between items-center py-4">
          <div className="flex items-center space-x-8">
            <div className="text-xl font-bold text-gray-800">
              Reservation System
            </div>
            <div className="flex space-x-4">
              {navItems.map((item) => (
                <button
                  key={item.name}
                  onClick={() => navigate(item.path)}
                  className={`px-3 py-2 rounded-md text-sm font-medium transition-colors ${
                    window.location.pathname === item.path
                      ? 'bg-indigo-100 text-indigo-700'
                      : 'text-gray-600 hover:text-gray-900 hover:bg-gray-100'
                  }`}
                >
                  {item.name}
                </button>
              ))}
            </div>
          </div>
          
          <div className="flex items-center space-x-4">
            <div className="text-sm">
              <span className="text-gray-600">Welcome, </span>
              <span className="font-medium text-gray-900">{currentUser.name}</span>
              <span className={`ml-2 px-2 py-1 text-xs font-semibold rounded-full ${
                currentUser.role === 'ADMIN' ? 'bg-red-100 text-red-800' :
                currentUser.role === 'TEACHER' ? 'bg-blue-100 text-blue-800' :
                'bg-green-100 text-green-800'
              }`}>
                {currentUser.role}
              </span>
            </div>
            <button
              onClick={handleLogout}
              className="bg-red-600 text-white px-4 py-2 rounded text-sm hover:bg-red-700 transition-colors"
            >
              Logout
            </button>
          </div>
        </div>
      </div>
    </nav>
  );
};

export default Navbar;
