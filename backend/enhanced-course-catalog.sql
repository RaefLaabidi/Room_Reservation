-- Enhanced Professional Course Catalog for University Auto-Scheduling
-- This script adds a comprehensive set of courses across multiple departments

-- Computer Science & Engineering Courses
INSERT INTO courses (name, subject, duration_hours, sessions_per_week, min_capacity, max_capacity, created_at, updated_at) VALUES
('Advanced Data Structures', 'Computer Science', 3, 2, 20, 35, NOW(), NOW()),
('Machine Learning Fundamentals', 'Computer Science', 4, 2, 15, 30, NOW(), NOW()),
('Software Engineering Principles', 'Computer Science', 3, 2, 25, 40, NOW(), NOW()),
('Database Systems Design', 'Computer Science', 3, 2, 20, 35, NOW(), NOW()),
('Computer Networks', 'Computer Science', 3, 2, 18, 32, NOW(), NOW()),
('Cybersecurity Fundamentals', 'Computer Science', 3, 2, 15, 28, NOW(), NOW()),
('Web Development Advanced', 'Computer Science', 4, 2, 20, 30, NOW(), NOW()),
('Mobile App Development', 'Computer Science', 4, 2, 16, 25, NOW(), NOW()),
('Artificial Intelligence', 'Computer Science', 4, 2, 12, 24, NOW(), NOW()),
('Cloud Computing Architecture', 'Computer Science', 3, 2, 18, 30, NOW(), NOW()),

-- Mathematics Courses
('Calculus III', 'Mathematics', 4, 3, 25, 45, NOW(), NOW()),
('Linear Algebra', 'Mathematics', 3, 2, 20, 35, NOW(), NOW()),
('Discrete Mathematics', 'Mathematics', 3, 2, 22, 38, NOW(), NOW()),
('Statistics and Probability', 'Mathematics', 3, 2, 25, 40, NOW(), NOW()),
('Differential Equations', 'Mathematics', 3, 2, 18, 32, NOW(), NOW()),
('Number Theory', 'Mathematics', 3, 2, 15, 25, NOW(), NOW()),
('Applied Mathematics', 'Mathematics', 4, 2, 20, 35, NOW(), NOW()),

-- Physics Courses
('Quantum Physics', 'Physics', 4, 2, 15, 28, NOW(), NOW()),
('Thermodynamics', 'Physics', 3, 2, 20, 35, NOW(), NOW()),
('Electromagnetism', 'Physics', 4, 2, 18, 30, NOW(), NOW()),
('Mechanics and Dynamics', 'Physics', 3, 2, 22, 38, NOW(), NOW()),
('Optics and Waves', 'Physics', 3, 2, 16, 28, NOW(), NOW()),
('Nuclear Physics', 'Physics', 3, 2, 12, 22, NOW(), NOW()),

-- Chemistry Courses
('Organic Chemistry II', 'Chemistry', 4, 2, 18, 32, NOW(), NOW()),
('Physical Chemistry', 'Chemistry', 4, 2, 16, 28, NOW(), NOW()),
('Analytical Chemistry', 'Chemistry', 3, 2, 20, 35, NOW(), NOW()),
('Biochemistry Fundamentals', 'Chemistry', 4, 2, 15, 25, NOW(), NOW()),
('Inorganic Chemistry', 'Chemistry', 3, 2, 18, 30, NOW(), NOW()),

-- Business Administration Courses
('Strategic Management', 'Business', 3, 2, 25, 45, NOW(), NOW()),
('Financial Analysis', 'Business', 3, 2, 20, 38, NOW(), NOW()),
('Marketing Strategy', 'Business', 3, 2, 22, 40, NOW(), NOW()),
('Operations Management', 'Business', 3, 2, 20, 35, NOW(), NOW()),
('Human Resource Management', 'Business', 3, 2, 25, 42, NOW(), NOW()),
('International Business', 'Business', 3, 2, 18, 32, NOW(), NOW()),
('Business Ethics', 'Business', 2, 2, 30, 50, NOW(), NOW()),
('Entrepreneurship', 'Business', 3, 2, 16, 28, NOW(), NOW()),

-- Economics Courses
('Macroeconomics', 'Economics', 3, 2, 25, 45, NOW(), NOW()),
('Microeconomics', 'Economics', 3, 2, 25, 45, NOW(), NOW()),
('Econometrics', 'Economics', 4, 2, 18, 30, NOW(), NOW()),
('Development Economics', 'Economics', 3, 2, 20, 35, NOW(), NOW()),
('Monetary Policy', 'Economics', 3, 2, 16, 28, NOW(), NOW()),

-- Engineering Courses
('Mechanical Design', 'Engineering', 4, 2, 18, 30, NOW(), NOW()),
('Electrical Circuits', 'Engineering', 4, 2, 20, 32, NOW(), NOW()),
('Materials Science', 'Engineering', 3, 2, 22, 38, NOW(), NOW()),
('Control Systems', 'Engineering', 4, 2, 15, 25, NOW(), NOW()),
('Fluid Mechanics', 'Engineering', 3, 2, 18, 32, NOW(), NOW()),
('Structural Analysis', 'Engineering', 4, 2, 16, 28, NOW(), NOW()),

-- Languages & Literature
('Advanced English Literature', 'Literature', 3, 2, 20, 35, NOW(), NOW()),
('Creative Writing Workshop', 'Literature', 2, 2, 12, 20, NOW(), NOW()),
('Spanish Language Advanced', 'Languages', 3, 3, 15, 25, NOW(), NOW()),
('French Conversation', 'Languages', 2, 3, 10, 18, NOW(), NOW()),
('German for Business', 'Languages', 3, 2, 12, 22, NOW(), NOW()),
('Arabic Studies', 'Languages', 3, 2, 10, 20, NOW(), NOW()),

-- Psychology & Social Sciences
('Cognitive Psychology', 'Psychology', 3, 2, 20, 35, NOW(), NOW()),
('Social Psychology', 'Psychology', 3, 2, 22, 38, NOW(), NOW()),
('Developmental Psychology', 'Psychology', 3, 2, 18, 32, NOW(), NOW()),
('Research Methods in Psychology', 'Psychology', 4, 2, 15, 25, NOW(), NOW()),
('Sociology of Organizations', 'Sociology', 3, 2, 25, 40, NOW(), NOW()),
('Cultural Anthropology', 'Anthropology', 3, 2, 18, 30, NOW(), NOW()),

-- Art & Design
('Digital Media Design', 'Art', 4, 2, 12, 20, NOW(), NOW()),
('Photography Techniques', 'Art', 3, 2, 10, 16, NOW(), NOW()),
('Graphic Design Principles', 'Art', 3, 2, 15, 25, NOW(), NOW()),
('3D Modeling and Animation', 'Art', 4, 2, 12, 18, NOW(), NOW()),

-- Medicine & Health Sciences
('Human Anatomy', 'Medicine', 5, 3, 20, 30, NOW(), NOW()),
('Pharmacology', 'Medicine', 4, 2, 15, 25, NOW(), NOW()),
('Public Health Policy', 'Health Sciences', 3, 2, 20, 35, NOW(), NOW()),
('Epidemiology', 'Health Sciences', 3, 2, 18, 28, NOW(), NOW()),
('Nutrition Science', 'Health Sciences', 3, 2, 22, 35, NOW(), NOW()),

-- Environmental Sciences
('Climate Change Studies', 'Environmental Science', 3, 2, 20, 32, NOW(), NOW()),
('Renewable Energy Systems', 'Environmental Science', 4, 2, 16, 28, NOW(), NOW()),
('Environmental Policy', 'Environmental Science', 3, 2, 18, 30, NOW(), NOW()),
('Ecology and Biodiversity', 'Environmental Science', 4, 2, 15, 25, NOW(), NOW());

-- Update existing courses with more realistic data
UPDATE courses SET 
    min_capacity = 15, 
    max_capacity = 30,
    duration_hours = 3,
    sessions_per_week = 2
WHERE name IN ('Introduction to Programming', 'Data Analysis', 'Web Development');

-- Add more teacher subject expertise
INSERT INTO teacher_subjects (teacher_id, subject, expertise_level, years_experience) VALUES
-- Computer Science teachers
(2, 'Computer Science', 'EXPERT', 8),
(2, 'Mathematics', 'INTERMEDIATE', 5),
(3, 'Computer Science', 'ADVANCED', 6),
(3, 'Engineering', 'INTERMEDIATE', 4),

-- Mathematics teachers  
(4, 'Mathematics', 'EXPERT', 12),
(4, 'Physics', 'ADVANCED', 7),
(5, 'Mathematics', 'ADVANCED', 8),
(5, 'Statistics', 'EXPERT', 10),

-- Physics teachers
(6, 'Physics', 'EXPERT', 15),
(6, 'Mathematics', 'ADVANCED', 10),
(7, 'Physics', 'ADVANCED', 9),
(7, 'Engineering', 'INTERMEDIATE', 5),

-- Business teachers
(8, 'Business', 'EXPERT', 12),
(8, 'Economics', 'ADVANCED', 8),
(9, 'Business', 'ADVANCED', 7),
(9, 'Management', 'EXPERT', 10),

-- Additional subject expertise
(10, 'Chemistry', 'EXPERT', 11),
(10, 'Biology', 'INTERMEDIATE', 6),
(11, 'Literature', 'EXPERT', 14),
(11, 'Languages', 'ADVANCED', 9),
(12, 'Psychology', 'EXPERT', 10),
(12, 'Sociology', 'ADVANCED', 7),
(13, 'Art', 'EXPERT', 8),
(13, 'Design', 'ADVANCED', 6),
(14, 'Medicine', 'EXPERT', 16),
(14, 'Health Sciences', 'ADVANCED', 12),
(15, 'Environmental Science', 'EXPERT', 9),
(15, 'Chemistry', 'INTERMEDIATE', 5);

-- Add diverse teacher availability patterns
INSERT INTO teacher_availability (teacher_id, day_of_week, start_time, end_time, is_preferred_time) VALUES
-- Full-time teachers with different patterns
(2, 'MONDAY', '09:00', '16:45', true),
(2, 'TUESDAY', '09:00', '16:45', true),
(2, 'WEDNESDAY', '09:00', '12:15', true),
(2, 'THURSDAY', '09:00', '16:45', false),
(2, 'FRIDAY', '13:30', '16:45', false),
(2, 'SATURDAY', '09:00', '12:15', false),

(3, 'MONDAY', '13:30', '16:45', true),
(3, 'TUESDAY', '09:00', '16:45', true),
(3, 'WEDNESDAY', '09:00', '12:15', false),
(3, 'THURSDAY', '09:00', '16:45', true),
(3, 'FRIDAY', '09:00', '16:45', true),
(3, 'SATURDAY', '09:00', '12:15', true),

-- Part-time teachers
(4, 'MONDAY', '09:00', '12:15', true),
(4, 'WEDNESDAY', '09:00', '12:15', true),
(4, 'FRIDAY', '09:00', '12:15', true),

(5, 'TUESDAY', '13:30', '16:45', true),
(5, 'THURSDAY', '13:30', '16:45', true),
(5, 'SATURDAY', '09:00', '12:15', true),

-- Evening preference teachers
(6, 'MONDAY', '13:30', '16:45', true),
(6, 'TUESDAY', '13:30', '16:45', true),
(6, 'WEDNESDAY', '09:00', '12:15', false),
(6, 'THURSDAY', '13:30', '16:45', true),
(6, 'FRIDAY', '13:30', '16:45', true);

COMMIT;
