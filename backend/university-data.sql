-- University Data Insertion Script
-- This script inserts realistic university data including teachers, students, and rooms

-- First, let's insert Teachers (Faculty Members)
-- Password for all users: mypassword (hashed with BCrypt)
-- BCrypt hash for "mypassword": $2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.

-- Computer Science Department Teachers
INSERT INTO users (name, email, role, password) VALUES
('Dr. Sarah Wilson', 'sarah.wilson@university.edu', 'TEACHER', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.'),
('Prof. Michael Chen', 'michael.chen@university.edu', 'TEACHER', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.'),
('Dr. Emily Rodriguez', 'emily.rodriguez@university.edu', 'TEACHER', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.'),
('Prof. David Thompson', 'david.thompson@university.edu', 'TEACHER', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.'),
('Dr. Lisa Anderson', 'lisa.anderson@university.edu', 'TEACHER', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.'),

-- Mathematics Department Teachers
('Prof. Robert Johnson', 'robert.johnson@university.edu', 'TEACHER', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.'),
('Dr. Jennifer Smith', 'jennifer.smith@university.edu', 'TEACHER', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.'),
('Prof. Ahmed Hassan', 'ahmed.hassan@university.edu', 'TEACHER', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.'),

-- Engineering Department Teachers
('Dr. Maria Garcia', 'maria.garcia@university.edu', 'TEACHER', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.'),
('Prof. James Brown', 'james.brown@university.edu', 'TEACHER', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.'),
('Dr. Anna Petrov', 'anna.petrov@university.edu', 'TEACHER', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.'),

-- Business Department Teachers
('Prof. William Davis', 'william.davis@university.edu', 'TEACHER', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.'),
('Dr. Sophie Martin', 'sophie.martin@university.edu', 'TEACHER', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.'),

-- Science Department Teachers
('Prof. Thomas Lee', 'thomas.lee@university.edu', 'TEACHER', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.'),
('Dr. Catherine White', 'catherine.white@university.edu', 'TEACHER', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.');

-- Now insert Students (many more than teachers)
-- Computer Science Students
INSERT INTO users (name, email, role, password) VALUES
('Alex Johnson', 'alex.johnson@student.university.edu', 'STUDENT', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.'),
('Emma Smith', 'emma.smith@student.university.edu', 'STUDENT', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.'),
('Ryan Davis', 'ryan.davis@student.university.edu', 'STUDENT', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.'),
('Sophia Wilson', 'sophia.wilson@student.university.edu', 'STUDENT', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.'),
('Liam Brown', 'liam.brown@student.university.edu', 'STUDENT', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.'),
('Olivia Jones', 'olivia.jones@student.university.edu', 'STUDENT', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.'),
('Noah Miller', 'noah.miller@student.university.edu', 'STUDENT', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.'),
('Ava Garcia', 'ava.garcia@student.university.edu', 'STUDENT', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.'),
('Ethan Rodriguez', 'ethan.rodriguez@student.university.edu', 'STUDENT', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.'),
('Isabella Martinez', 'isabella.martinez@student.university.edu', 'STUDENT', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.'),

-- Mathematics Students
('Mason Anderson', 'mason.anderson@student.university.edu', 'STUDENT', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.'),
('Charlotte Taylor', 'charlotte.taylor@student.university.edu', 'STUDENT', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.'),
('Lucas Thomas', 'lucas.thomas@student.university.edu', 'STUDENT', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.'),
('Amelia Hernandez', 'amelia.hernandez@student.university.edu', 'STUDENT', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.'),
('Benjamin Moore', 'benjamin.moore@student.university.edu', 'STUDENT', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.'),
('Harper Martin', 'harper.martin@student.university.edu', 'STUDENT', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.'),
('Elijah Jackson', 'elijah.jackson@student.university.edu', 'STUDENT', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.'),
('Mia Thompson', 'mia.thompson@student.university.edu', 'STUDENT', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.'),

-- Engineering Students
('Logan White', 'logan.white@student.university.edu', 'STUDENT', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.'),
('Evelyn Lopez', 'evelyn.lopez@student.university.edu', 'STUDENT', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.'),
('Alexander Lee', 'alexander.lee@student.university.edu', 'STUDENT', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.'),
('Abigail Gonzalez', 'abigail.gonzalez@student.university.edu', 'STUDENT', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.'),
('Samuel Wilson', 'samuel.wilson@student.university.edu', 'STUDENT', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.'),
('Emily Clark', 'emily.clark@student.university.edu', 'STUDENT', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.'),
('Sebastian Lewis', 'sebastian.lewis@student.university.edu', 'STUDENT', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.'),
('Madison Robinson', 'madison.robinson@student.university.edu', 'STUDENT', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.'),

-- Business Students
('Jack Walker', 'jack.walker@student.university.edu', 'STUDENT', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.'),
('Ella Perez', 'ella.perez@student.university.edu', 'STUDENT', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.'),
('Owen Hall', 'owen.hall@student.university.edu', 'STUDENT', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.'),
('Scarlett Young', 'scarlett.young@student.university.edu', 'STUDENT', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.'),
('Carter Allen', 'carter.allen@student.university.edu', 'STUDENT', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.'),
('Victoria Sanchez', 'victoria.sanchez@student.university.edu', 'STUDENT', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.'),
('Wyatt Wright', 'wyatt.wright@student.university.edu', 'STUDENT', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.'),
('Grace King', 'grace.king@student.university.edu', 'STUDENT', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.'),

-- Science Students
('Julian Scott', 'julian.scott@student.university.edu', 'STUDENT', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.'),
('Zoey Green', 'zoey.green@student.university.edu', 'STUDENT', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.'),
('Grayson Adams', 'grayson.adams@student.university.edu', 'STUDENT', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.'),
('Lily Baker', 'lily.baker@student.university.edu', 'STUDENT', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.'),
('Leo Rivera', 'leo.rivera@student.university.edu', 'STUDENT', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.'),
('Hannah Campbell', 'hannah.campbell@student.university.edu', 'STUDENT', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.'),
('Jaxon Mitchell', 'jaxon.mitchell@student.university.edu', 'STUDENT', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.'),
('Natalie Carter', 'natalie.carter@student.university.edu', 'STUDENT', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.'),

-- Additional Students for various departments
('Hunter Roberts', 'hunter.roberts@student.university.edu', 'STUDENT', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.'),
('Layla Gomez', 'layla.gomez@student.university.edu', 'STUDENT', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.'),
('Aaron Phillips', 'aaron.phillips@student.university.edu', 'STUDENT', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.'),
('Stella Evans', 'stella.evans@student.university.edu', 'STUDENT', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.'),
('Connor Turner', 'connor.turner@student.university.edu', 'STUDENT', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.'),
('Paisley Diaz', 'paisley.diaz@student.university.edu', 'STUDENT', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.'),
('Ian Parker', 'ian.parker@student.university.edu', 'STUDENT', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.'),
('Savannah Cruz', 'savannah.cruz@student.university.edu', 'STUDENT', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.'),
('Christian Edwards', 'christian.edwards@student.university.edu', 'STUDENT', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.'),
('Audrey Collins', 'audrey.collins@student.university.edu', 'STUDENT', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.'),
('Cooper Stewart', 'cooper.stewart@student.university.edu', 'STUDENT', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.'),
('Maya Sanchez', 'maya.sanchez@student.university.edu', 'STUDENT', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.'),
('Easton Morris', 'easton.morris@student.university.edu', 'STUDENT', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.'),
('Claire Reed', 'claire.reed@student.university.edu', 'STUDENT', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.'),
('Colton Cook', 'colton.cook@student.university.edu', 'STUDENT', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.'),
('Caroline Bailey', 'caroline.bailey@student.university.edu', 'STUDENT', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.');

-- Insert Rooms (University Building Structure)
-- Block A - Administrative and Large Lecture Halls
INSERT INTO rooms (name, capacity, location) VALUES
('A01', 150, 'Block A - Auditorium'),
('A02', 120, 'Block A - Large Lecture Hall'),
('A03', 100, 'Block A - Conference Room'),
('A04', 80, 'Block A - Seminar Hall'),
('A05', 60, 'Block A - Meeting Room'),

-- Block B - Computer Science Department
('B01', 40, 'Block B - Computer Lab 1'),
('B02', 40, 'Block B - Computer Lab 2'),
('B03', 35, 'Block B - Programming Lab'),
('B04', 30, 'Block B - Software Engineering Lab'),
('B05', 25, 'Block B - Database Lab'),
('B06', 50, 'Block B - CS Lecture Hall'),
('B07', 45, 'Block B - CS Classroom'),
('B08', 20, 'Block B - Research Lab'),

-- Block C - Mathematics Department
('C01', 45, 'Block C - Math Classroom 1'),
('C02', 40, 'Block C - Math Classroom 2'),
('C03', 35, 'Block C - Statistics Lab'),
('C04', 30, 'Block C - Calculus Room'),
('C05', 25, 'Block C - Tutorial Room'),
('C06', 50, 'Block C - Math Lecture Hall'),

-- Block D - Engineering Department (including your example)
('D01', 25, 'Block D - Small Classroom'),
('D02', 30, 'Block D - Project Room'),
('D03', 35, 'Block D - Engineering Lab'),
('D04', 20, 'Block D - Tutorial Room'),
('D05', 15, 'Block D - Small Meeting Room'),
('D06', 10, 'Block D - Office Space'),
('D07', 40, 'Block D - Workshop'),
('D08', 55, 'Block D - Engineering Lecture Hall'),
('D09', 45, 'Block D - Design Studio'),
('D10', 30, 'Block D - CAD Lab'),

-- Block E - Science Department
('E01', 30, 'Block E - Chemistry Lab'),
('E02', 28, 'Block E - Physics Lab'),
('E03', 35, 'Block E - Biology Lab'),
('E04', 40, 'Block E - Science Classroom'),
('E05', 25, 'Block E - Research Lab'),
('E06', 16, 'Block E - Small Lab'),
('E07', 45, 'Block E - Science Lecture Hall'),
('E08', 20, 'Block E - Prep Room'),

-- Block F - Business Department
('F01', 50, 'Block F - Business Classroom 1'),
('F02', 45, 'Block F - Business Classroom 2'),
('F03', 40, 'Block F - Case Study Room'),
('F04', 30, 'Block F - Presentation Room'),
('F05', 60, 'Block F - Business Lecture Hall'),
('F06', 25, 'Block F - Group Study Room'),
('F07', 35, 'Block F - MBA Classroom'),

-- Block G - General Purpose and Study Areas
('G01', 20, 'Block G - Study Room 1'),
('G02', 20, 'Block G - Study Room 2'),
('G03', 15, 'Block G - Group Study'),
('G04', 25, 'Block G - Tutorial Room'),
('G05', 30, 'Block G - Multi-purpose Room'),
('G06', 40, 'Block G - Event Hall'),
('G07', 12, 'Block G - Small Meeting'),
('G08', 18, 'Block G - Consultation Room'),

-- Block H - Library and Additional Spaces
('H01', 80, 'Block H - Library Auditorium'),
('H02', 35, 'Block H - Library Classroom'),
('H03', 20, 'Block H - Discussion Room'),
('H04', 15, 'Block H - Quiet Study'),
('H05', 25, 'Block H - Group Work Area'),
('H06', 30, 'Block H - Media Room'),

-- Block I - Special Purpose Rooms
('I01', 100, 'Block I - Exam Hall'),
('I02', 90, 'Block I - Assessment Center'),
('I03', 50, 'Block I - Defense Room'),
('I04', 40, 'Block I - Thesis Presentation'),
('I05', 30, 'Block I - Interview Room'),
('I06', 25, 'Block I - Counseling Room');

-- Summary Information
-- Teachers: 15 faculty members across 5 departments
-- Students: 50+ students across various departments  
-- Rooms: 60+ rooms across 9 blocks with varied capacities (10-150)
-- All passwords are set to "mypassword" for easy testing
