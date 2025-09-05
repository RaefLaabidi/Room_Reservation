import React, { useState, useEffect } from 'react';
import { ScheduleTemplate, TemplateCourseAssignment } from '../types/ScheduleTemplate';
import { Course } from '../types/Course';
import { User } from '../types/User';
import { scheduleTemplatesAPI, coursesAPI, usersAPI, autoSchedulingAPI } from '../services/api';

const ScheduleTemplatesManagement: React.FC = () => {
  const [templates, setTemplates] = useState<ScheduleTemplate[]>([]);
  const [courses, setCourses] = useState<Course[]>([]);
  const [teachers, setTeachers] = useState<User[]>([]);
  const [showForm, setShowForm] = useState(false);
  const [editingTemplate, setEditingTemplate] = useState<ScheduleTemplate | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [generatingSchedule, setGeneratingSchedule] = useState<number | null>(null);
  
  const currentUser = JSON.parse(localStorage.getItem('user') || '{}');
  
  const [formData, setFormData] = useState({
    name: '',
    weekStartDate: '',
    weekEndDate: '',
    status: 'DRAFT' as 'DRAFT' | 'PUBLISHED' | 'ARCHIVED'
  });

  const [courseAssignments, setCourseAssignments] = useState<Omit<TemplateCourseAssignment, 'id' | 'template'>[]>([]);

  const fetchData = async () => {
    try {
      setLoading(true);
      const [templatesData, coursesData, teachersData] = await Promise.all([
        scheduleTemplatesAPI.getAll(),
        coursesAPI.getAll(),
        usersAPI.getAll()
      ]);
      setTemplates(templatesData);
      setCourses(coursesData);
      setTeachers(teachersData.filter(user => user.role === 'TEACHER'));
    } catch (err) {
      setError('Failed to fetch data');
      console.error('Error fetching data:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  // Auto-calculate week end date when start date changes
  useEffect(() => {
    if (formData.weekStartDate) {
      const startDate = new Date(formData.weekStartDate);
      const endDate = new Date(startDate);
      endDate.setDate(startDate.getDate() + 6); // Add 6 days for a full week
      setFormData(prev => ({
        ...prev,
        weekEndDate: endDate.toISOString().split('T')[0]
      }));
    }
  }, [formData.weekStartDate]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      const templateData: Omit<ScheduleTemplate, 'id'> = {
        ...formData,
        createdBy: {
          id: currentUser.id,
          name: currentUser.name
        },
        courseAssignments: courseAssignments
      };

      if (editingTemplate) {
        await scheduleTemplatesAPI.update(editingTemplate.id!, templateData);
      } else {
        await scheduleTemplatesAPI.create(templateData);
      }
      await fetchData();
      resetForm();
    } catch (err) {
      setError('Failed to save template');
      console.error('Error saving template:', err);
    }
  };

  const handleEdit = (template: ScheduleTemplate) => {
    setEditingTemplate(template);
    setFormData({
      name: template.name,
      weekStartDate: template.weekStartDate,
      weekEndDate: template.weekEndDate,
      status: template.status
    });
    setCourseAssignments(template.courseAssignments || []);
    setShowForm(true);
  };

  const handleDelete = async (id: number) => {
    if (window.confirm('Are you sure you want to delete this template?')) {
      try {
        await scheduleTemplatesAPI.delete(id);
        await fetchData();
      } catch (err) {
        setError('Failed to delete template');
        console.error('Error deleting template:', err);
      }
    }
  };

  const handleGenerateSchedule = async (templateId: number) => {
    try {
      setGeneratingSchedule(templateId);
      const result = await autoSchedulingAPI.generateSchedule(templateId);
      
      if (result.success) {
        alert(`Schedule generated successfully! 
        Scheduled: ${result.scheduledCourses}/${result.totalCourses} courses
        ${result.failedCourses > 0 ? `Failed: ${result.failedCourses} courses` : ''}`);
      } else {
        alert(`Schedule generation completed with issues:
        Scheduled: ${result.scheduledCourses}/${result.totalCourses} courses
        Failed: ${result.failedCourses} courses
        Please check the conflicts and try again.`);
      }
    } catch (err) {
      setError('Failed to generate schedule');
      console.error('Error generating schedule:', err);
    } finally {
      setGeneratingSchedule(null);
    }
  };

  const addCourseAssignment = () => {
    if (courses.length === 0) return;
    
    setCourseAssignments([
      ...courseAssignments,
      {
        course: {
          id: courses[0].id!,
          name: courses[0].name,
          subject: courses[0].subject,
          durationHours: courses[0].durationHours,
          sessionsPerWeek: courses[0].sessionsPerWeek,
          minCapacity: courses[0].minCapacity
        },
        priority: 1,
        studentCount: 20,
        preferredDays: '1,2,3,4,5' // Weekdays by default
      }
    ]);
  };

  const updateCourseAssignment = (index: number, updates: Partial<Omit<TemplateCourseAssignment, 'id' | 'template'>>) => {
    const updated = [...courseAssignments];
    updated[index] = { ...updated[index], ...updates };
    setCourseAssignments(updated);
  };

  const removeCourseAssignment = (index: number) => {
    setCourseAssignments(courseAssignments.filter((_, i) => i !== index));
  };

  const resetForm = () => {
    setFormData({
      name: '',
      weekStartDate: '',
      weekEndDate: '',
      status: 'DRAFT'
    });
    setCourseAssignments([]);
    setEditingTemplate(null);
    setShowForm(false);
    setError(null);
  };

  const getWeekDayName = (day: string) => {
    const days = ['', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];
    return days[parseInt(day)] || day;
  };

  if (loading) {
    return (
      <div className="flex justify-center items-center h-64">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  return (
    <div className="max-w-7xl mx-auto p-6">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-3xl font-bold text-gray-900">Schedule Templates</h1>
        <button
          onClick={() => setShowForm(true)}
          className="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 transition duration-200"
        >
          Create New Template
        </button>
      </div>

      {error && (
        <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
          {error}
        </div>
      )}

      {/* Template Form Modal */}
      {showForm && (
        <div className="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full w-full z-50">
          <div className="relative top-10 mx-auto p-5 border w-4/5 max-w-4xl shadow-lg rounded-md bg-white">
            <div className="mt-3">
              <h3 className="text-lg font-medium text-gray-900 mb-4">
                {editingTemplate ? 'Edit Template' : 'Create New Template'}
              </h3>
              <form onSubmit={handleSubmit} className="space-y-6">
                {/* Basic Template Info */}
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-700">Template Name</label>
                    <input
                      type="text"
                      required
                      value={formData.name}
                      onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                      className="mt-1 block w-full border border-gray-300 rounded-md px-3 py-2 focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                      placeholder="e.g., Week of September 1-7, 2025"
                    />
                  </div>
                  
                  <div>
                    <label className="block text-sm font-medium text-gray-700">Status</label>
                    <select
                      value={formData.status}
                      onChange={(e) => setFormData({ ...formData, status: e.target.value as any })}
                      className="mt-1 block w-full border border-gray-300 rounded-md px-3 py-2 focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                    >
                      <option value="DRAFT">Draft</option>
                      <option value="PUBLISHED">Published</option>
                      <option value="ARCHIVED">Archived</option>
                    </select>
                  </div>
                </div>

                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-700">Week Start Date</label>
                    <input
                      type="date"
                      required
                      value={formData.weekStartDate}
                      onChange={(e) => setFormData({ ...formData, weekStartDate: e.target.value })}
                      className="mt-1 block w-full border border-gray-300 rounded-md px-3 py-2 focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                    />
                  </div>
                  
                  <div>
                    <label className="block text-sm font-medium text-gray-700">Week End Date</label>
                    <input
                      type="date"
                      required
                      value={formData.weekEndDate}
                      onChange={(e) => setFormData({ ...formData, weekEndDate: e.target.value })}
                      className="mt-1 block w-full border border-gray-300 rounded-md px-3 py-2 focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                    />
                  </div>
                </div>

                {/* Course Assignments */}
                <div>
                  <div className="flex justify-between items-center mb-4">
                    <h4 className="text-md font-medium text-gray-900">Course Assignments</h4>
                    <button
                      type="button"
                      onClick={addCourseAssignment}
                      className="bg-green-600 text-white px-3 py-1 rounded text-sm hover:bg-green-700"
                      disabled={courses.length === 0}
                    >
                      Add Course
                    </button>
                  </div>

                  {courseAssignments.map((assignment, index) => (
                    <div key={index} className="border border-gray-200 rounded-lg p-4 mb-4 bg-gray-50">
                      <div className="grid grid-cols-2 md:grid-cols-3 gap-4">
                        <div>
                          <label className="block text-sm font-medium text-gray-700">Course</label>
                          <select
                            value={assignment.course.id}
                            onChange={(e) => {
                              const course = courses.find(c => c.id === parseInt(e.target.value));
                              if (course && course.id) {
                                updateCourseAssignment(index, { 
                                  course: {
                                    id: course.id,
                                    name: course.name,
                                    subject: course.subject,
                                    durationHours: course.durationHours,
                                    sessionsPerWeek: course.sessionsPerWeek,
                                    minCapacity: course.minCapacity
                                  }
                                });
                              }
                            }}
                            className="mt-1 block w-full border border-gray-300 rounded-md px-3 py-2 text-sm"
                          >
                            {courses.map(course => (
                              <option key={course.id} value={course.id}>
                                {course.name} ({course.subject})
                              </option>
                            ))}
                          </select>
                        </div>

                        <div>
                          <label className="block text-sm font-medium text-gray-700">Assigned Teacher</label>
                          <select
                            value={assignment.assignedTeacher?.id || ''}
                            onChange={(e) => {
                              const teacher = teachers.find(t => t.id === parseInt(e.target.value));
                              updateCourseAssignment(index, { assignedTeacher: teacher });
                            }}
                            className="mt-1 block w-full border border-gray-300 rounded-md px-3 py-2 text-sm"
                          >
                            <option value="">Auto-assign</option>
                            {teachers.map(teacher => (
                              <option key={teacher.id} value={teacher.id}>
                                {teacher.name}
                              </option>
                            ))}
                          </select>
                        </div>

                        <div>
                          <label className="block text-sm font-medium text-gray-700">Priority</label>
                          <select
                            value={assignment.priority}
                            onChange={(e) => updateCourseAssignment(index, { priority: parseInt(e.target.value) })}
                            className="mt-1 block w-full border border-gray-300 rounded-md px-3 py-2 text-sm"
                          >
                            <option value={1}>Low</option>
                            <option value={2}>Medium</option>
                            <option value={3}>High</option>
                            <option value={4}>Critical</option>
                          </select>
                        </div>

                        <div>
                          <label className="block text-sm font-medium text-gray-700">Student Count</label>
                          <input
                            type="number"
                            min="1"
                            value={assignment.studentCount || ''}
                            onChange={(e) => updateCourseAssignment(index, { studentCount: parseInt(e.target.value) })}
                            className="mt-1 block w-full border border-gray-300 rounded-md px-3 py-2 text-sm"
                          />
                        </div>

                        <div>
                          <label className="block text-sm font-medium text-gray-700">Preferred Time Start</label>
                          <input
                            type="time"
                            value={assignment.preferredTimeStart || ''}
                            onChange={(e) => updateCourseAssignment(index, { preferredTimeStart: e.target.value })}
                            className="mt-1 block w-full border border-gray-300 rounded-md px-3 py-2 text-sm"
                          />
                        </div>

                        <div>
                          <label className="block text-sm font-medium text-gray-700">Preferred Time End</label>
                          <input
                            type="time"
                            value={assignment.preferredTimeEnd || ''}
                            onChange={(e) => updateCourseAssignment(index, { preferredTimeEnd: e.target.value })}
                            className="mt-1 block w-full border border-gray-300 rounded-md px-3 py-2 text-sm"
                          />
                        </div>
                      </div>

                      <div className="mt-3">
                        <label className="block text-sm font-medium text-gray-700 mb-2">Preferred Days</label>
                        <div className="flex space-x-2">
                          {[1, 2, 3, 4, 5, 6, 7].map(day => {
                            const dayNames = ['', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];
                            const isSelected = assignment.preferredDays?.includes(day.toString());
                            return (
                              <button
                                key={day}
                                type="button"
                                onClick={() => {
                                  const days = assignment.preferredDays?.split(',').filter(Boolean) || [];
                                  const newDays = isSelected 
                                    ? days.filter(d => d !== day.toString())
                                    : [...days, day.toString()];
                                  updateCourseAssignment(index, { preferredDays: newDays.join(',') });
                                }}
                                className={`px-3 py-1 text-xs rounded ${
                                  isSelected 
                                    ? 'bg-blue-600 text-white' 
                                    : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
                                }`}
                              >
                                {dayNames[day]}
                              </button>
                            );
                          })}
                        </div>
                      </div>

                      <div className="flex justify-end mt-3">
                        <button
                          type="button"
                          onClick={() => removeCourseAssignment(index)}
                          className="text-red-600 hover:text-red-800 text-sm"
                        >
                          Remove
                        </button>
                      </div>
                    </div>
                  ))}

                  {courseAssignments.length === 0 && (
                    <div className="text-center py-6 text-gray-500 border-2 border-dashed border-gray-300 rounded-lg">
                      No courses assigned. Add courses to this template.
                    </div>
                  )}
                </div>
                
                <div className="flex justify-end space-x-3 pt-4 border-t">
                  <button
                    type="button"
                    onClick={resetForm}
                    className="px-4 py-2 text-sm font-medium text-gray-700 bg-gray-200 rounded-md hover:bg-gray-300"
                  >
                    Cancel
                  </button>
                  <button
                    type="submit"
                    className="px-4 py-2 text-sm font-medium text-white bg-blue-600 rounded-md hover:bg-blue-700"
                  >
                    {editingTemplate ? 'Update' : 'Create'}
                  </button>
                </div>
              </form>
            </div>
          </div>
        </div>
      )}

      {/* Templates List */}
      <div className="grid gap-6">
        {templates.map((template) => (
          <div key={template.id} className="bg-white shadow rounded-lg overflow-hidden">
            <div className="px-6 py-4 border-b border-gray-200">
              <div className="flex items-center justify-between">
                <div>
                  <h3 className="text-lg font-medium text-gray-900">{template.name}</h3>
                  <p className="text-sm text-gray-500">
                    {template.weekStartDate} to {template.weekEndDate}
                  </p>
                </div>
                <div className="flex items-center space-x-4">
                  <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${
                    template.status === 'PUBLISHED' ? 'bg-green-100 text-green-800' :
                    template.status === 'DRAFT' ? 'bg-yellow-100 text-yellow-800' :
                    'bg-gray-100 text-gray-800'
                  }`}>
                    {template.status}
                  </span>
                  <div className="flex space-x-2">
                    <button
                      onClick={() => handleGenerateSchedule(template.id!)}
                      disabled={generatingSchedule === template.id}
                      className="bg-green-600 text-white px-3 py-1 rounded text-sm hover:bg-green-700 disabled:opacity-50"
                    >
                      {generatingSchedule === template.id ? 'Generating...' : 'Generate Schedule'}
                    </button>
                    <button
                      onClick={() => handleEdit(template)}
                      className="text-blue-600 hover:text-blue-800 font-medium text-sm"
                    >
                      Edit
                    </button>
                    <button
                      onClick={() => handleDelete(template.id!)}
                      className="text-red-600 hover:text-red-800 font-medium text-sm"
                    >
                      Delete
                    </button>
                  </div>
                </div>
              </div>
            </div>
            
            {template.courseAssignments && template.courseAssignments.length > 0 && (
              <div className="px-6 py-4">
                <h4 className="text-sm font-medium text-gray-900 mb-3">Course Assignments</h4>
                <div className="space-y-2">
                  {template.courseAssignments.map((assignment, index) => (
                    <div key={index} className="flex items-center justify-between text-sm">
                      <div>
                        <span className="font-medium">{assignment.course.name}</span>
                        <span className="text-gray-500 ml-2">({assignment.course.subject})</span>
                        {assignment.assignedTeacher && (
                          <span className="text-blue-600 ml-2">→ {assignment.assignedTeacher.name}</span>
                        )}
                      </div>
                      <div className="flex items-center space-x-4 text-gray-500">
                        <span>Priority: {assignment.priority}</span>
                        {assignment.studentCount && <span>Students: {assignment.studentCount}</span>}
                        {assignment.preferredDays && (
                          <span>Days: {assignment.preferredDays.split(',').map(d => getWeekDayName(d)).join(', ')}</span>
                        )}
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            )}
          </div>
        ))}
        
        {templates.length === 0 && (
          <div className="text-center py-12">
            <div className="text-gray-500 text-lg">No schedule templates found.</div>
            <button
              onClick={() => setShowForm(true)}
              className="mt-4 bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700"
            >
              Create Your First Template
            </button>
          </div>
        )}
      </div>
    </div>
  );
};

export default ScheduleTemplatesManagement;
