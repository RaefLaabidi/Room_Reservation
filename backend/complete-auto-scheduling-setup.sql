-- ========================================
-- AUTO-SCHEDULING DATA SETUP
-- University Constraints Implementation
-- ========================================

-- 1. TEACHER SUBJECTS (Teacher expertise in different subjects)
INSERT INTO teacher_subjects (teacher_id, subject, expertise) VALUES
-- Programming Teachers
(3, 'Programming', 5),          -- Prof1 (Expert)
(4, 'Programming', 4),          -- Prof2 (Advanced)
(5, 'Programming', 3),          -- Prof3 (Intermediate)
(7, 'Programming', 4),          -- Prof5 (Advanced)

-- Mathematics Teachers  
(3, 'Mathematics', 4),          -- Prof1 (Advanced)
(4, 'Mathematics', 5),          -- Prof2 (Expert)
(8, 'Mathematics', 5),          -- Prof6 (Expert)
(9, 'Mathematics', 3),          -- Prof7 (Intermediate)

-- Web Development Teachers
(5, 'Web Development', 5),      -- Prof3 (Expert)
(6, 'Web Development', 4),      -- Prof4 (Advanced)
(7, 'Web Development', 3),      -- Prof5 (Intermediate)
(9, 'Web Development', 4),      -- Prof7 (Advanced)

-- Database Teachers
(4, 'Database', 4),             -- Prof2 (Advanced)
(6, 'Database', 5),             -- Prof4 (Expert)
(8, 'Database', 3),             -- Prof6 (Intermediate)

-- Networks Teachers
(6, 'Networks', 5),             -- Prof4 (Expert)
(7, 'Networks', 4),             -- Prof5 (Advanced)
(8, 'Networks', 3);             -- Prof6 (Intermediate)

-- 2. TEACHER AVAILABILITY (University Time Constraints)
-- Morning: 9:00 AM - 12:15 PM (Monday-Saturday)
-- Afternoon: 1:30 PM - 4:45 PM (Monday, Tuesday, Thursday, Friday only)
-- Wednesday & Saturday: Morning only
-- Sunday: No work

INSERT INTO teacher_availability (teacher_id, day_of_week, start_time, end_time, is_available) VALUES
-- TEACHER 3 (Prof1) - Full availability
(3, 1, '09:00:00', '12:15:00', true),    -- Monday Morning
(3, 1, '13:30:00', '16:45:00', true),    -- Monday Afternoon
(3, 2, '09:00:00', '12:15:00', true),    -- Tuesday Morning
(3, 2, '13:30:00', '16:45:00', true),    -- Tuesday Afternoon
(3, 3, '09:00:00', '12:15:00', true),    -- Wednesday Morning Only
(3, 4, '09:00:00', '12:15:00', true),    -- Thursday Morning
(3, 4, '13:30:00', '16:45:00', true),    -- Thursday Afternoon
(3, 5, '09:00:00', '12:15:00', true),    -- Friday Morning
(3, 5, '13:30:00', '16:45:00', true),    -- Friday Afternoon
(3, 6, '09:00:00', '12:15:00', true),    -- Saturday Morning Only

-- TEACHER 4 (Prof2) - Full availability
(4, 1, '09:00:00', '12:15:00', true),    -- Monday Morning
(4, 1, '13:30:00', '16:45:00', true),    -- Monday Afternoon
(4, 2, '09:00:00', '12:15:00', true),    -- Tuesday Morning
(4, 2, '13:30:00', '16:45:00', true),    -- Tuesday Afternoon
(4, 3, '09:00:00', '12:15:00', true),    -- Wednesday Morning Only
(4, 4, '09:00:00', '12:15:00', true),    -- Thursday Morning
(4, 4, '13:30:00', '16:45:00', true),    -- Thursday Afternoon
(4, 5, '09:00:00', '12:15:00', true),    -- Friday Morning
(4, 5, '13:30:00', '16:45:00', true),    -- Friday Afternoon
(4, 6, '09:00:00', '12:15:00', true),    -- Saturday Morning Only

-- TEACHER 5 (Prof3) - Full availability
(5, 1, '09:00:00', '12:15:00', true),    -- Monday Morning
(5, 1, '13:30:00', '16:45:00', true),    -- Monday Afternoon
(5, 2, '09:00:00', '12:15:00', true),    -- Tuesday Morning
(5, 2, '13:30:00', '16:45:00', true),    -- Tuesday Afternoon
(5, 3, '09:00:00', '12:15:00', true),    -- Wednesday Morning Only
(5, 4, '09:00:00', '12:15:00', true),    -- Thursday Morning
(5, 4, '13:30:00', '16:45:00', true),    -- Thursday Afternoon
(5, 5, '09:00:00', '12:15:00', true),    -- Friday Morning
(5, 5, '13:30:00', '16:45:00', true),    -- Friday Afternoon
(5, 6, '09:00:00', '12:15:00', true),    -- Saturday Morning Only

-- TEACHER 6 (Prof4) - Full availability
(6, 1, '09:00:00', '12:15:00', true),    -- Monday Morning
(6, 1, '13:30:00', '16:45:00', true),    -- Monday Afternoon
(6, 2, '09:00:00', '12:15:00', true),    -- Tuesday Morning
(6, 2, '13:30:00', '16:45:00', true),    -- Tuesday Afternoon
(6, 3, '09:00:00', '12:15:00', true),    -- Wednesday Morning Only
(6, 4, '09:00:00', '12:15:00', true),    -- Thursday Morning
(6, 4, '13:30:00', '16:45:00', true),    -- Thursday Afternoon
(6, 5, '09:00:00', '12:15:00', true),    -- Friday Morning
(6, 5, '13:30:00', '16:45:00', true),    -- Friday Afternoon
(6, 6, '09:00:00', '12:15:00', true),    -- Saturday Morning Only

-- TEACHER 7 (Prof5) - Full availability
(7, 1, '09:00:00', '12:15:00', true),    -- Monday Morning
(7, 1, '13:30:00', '16:45:00', true),    -- Monday Afternoon
(7, 2, '09:00:00', '12:15:00', true),    -- Tuesday Morning
(7, 2, '13:30:00', '16:45:00', true),    -- Tuesday Afternoon
(7, 3, '09:00:00', '12:15:00', true),    -- Wednesday Morning Only
(7, 4, '09:00:00', '12:15:00', true),    -- Thursday Morning
(7, 4, '13:30:00', '16:45:00', true),    -- Thursday Afternoon
(7, 5, '09:00:00', '12:15:00', true),    -- Friday Morning
(7, 5, '13:30:00', '16:45:00', true),    -- Friday Afternoon
(7, 6, '09:00:00', '12:15:00', true),    -- Saturday Morning Only

-- TEACHER 8 (Prof6) - Partial availability (mornings preferred)
(8, 1, '09:00:00', '12:15:00', true),    -- Monday Morning
(8, 2, '09:00:00', '12:15:00', true),    -- Tuesday Morning
(8, 3, '09:00:00', '12:15:00', true),    -- Wednesday Morning Only
(8, 4, '09:00:00', '12:15:00', true),    -- Thursday Morning
(8, 4, '13:30:00', '16:45:00', true),    -- Thursday Afternoon
(8, 5, '09:00:00', '12:15:00', true),    -- Friday Morning
(8, 6, '09:00:00', '12:15:00', true),    -- Saturday Morning Only

-- TEACHER 9 (Prof7) - Partial availability (afternoons preferred)
(9, 1, '13:30:00', '16:45:00', true),    -- Monday Afternoon
(9, 2, '09:00:00', '12:15:00', true),    -- Tuesday Morning
(9, 2, '13:30:00', '16:45:00', true),    -- Tuesday Afternoon
(9, 3, '09:00:00', '12:15:00', true),    -- Wednesday Morning Only
(9, 4, '13:30:00', '16:45:00', true),    -- Thursday Afternoon
(9, 5, '13:30:00', '16:45:00', true),    -- Friday Afternoon
(9, 6, '09:00:00', '12:15:00', true);    -- Saturday Morning Only

-- 3. ENHANCED COURSES (Update existing courses with proper session requirements)
UPDATE courses SET 
    sessions_per_week = 2,
    duration_hours = 2,
    preferred_room_type = 'Computer Lab'
WHERE subject = 'Programming';

UPDATE courses SET 
    sessions_per_week = 3,
    duration_hours = 1,
    preferred_room_type = 'Classroom'
WHERE subject = 'Mathematics';

UPDATE courses SET 
    sessions_per_week = 2,
    duration_hours = 2,
    preferred_room_type = 'Computer Lab'
WHERE subject = 'Web Development';

-- 4. VERIFICATION QUERIES (Optional - for testing)
-- SELECT 'Teacher Subjects Count: ' || COUNT(*) FROM teacher_subjects;
-- SELECT 'Teacher Availability Count: ' || COUNT(*) FROM teacher_availability;
-- SELECT subject, COUNT(*) as teacher_count FROM teacher_subjects GROUP BY subject;

COMMIT;
