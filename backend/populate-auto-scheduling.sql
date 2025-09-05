INSERT INTO teacher_subjects (teacher_id, subject, expertise) VALUES
(3, 'Programming', 5),
(4, 'Programming', 4),
(5, 'Programming', 3),
(3, 'Mathematics', 4),
(4, 'Mathematics', 5),
(5, 'Web Development', 5),
(6, 'Web Development', 4);

INSERT INTO teacher_availability (teacher_id, day_of_week, start_time, end_time, is_available) VALUES
-- Teacher 3 availability
(3, 1, '09:00:00', '17:00:00', true),
(3, 2, '09:00:00', '17:00:00', true),
(3, 3, '09:00:00', '12:15:00', true),
(3, 4, '09:00:00', '17:00:00', true),
(3, 5, '09:00:00', '17:00:00', true),
(3, 6, '09:00:00', '12:15:00', true),
-- Teacher 4 availability
(4, 1, '09:00:00', '17:00:00', true),
(4, 2, '09:00:00', '17:00:00', true),
(4, 3, '09:00:00', '12:15:00', true),
(4, 4, '09:00:00', '17:00:00', true),
(4, 5, '09:00:00', '17:00:00', true),
(4, 6, '09:00:00', '12:15:00', true),
-- Teacher 5 availability
(5, 1, '09:00:00', '17:00:00', true),
(5, 2, '09:00:00', '17:00:00', true),
(5, 3, '09:00:00', '12:15:00', true),
(5, 4, '09:00:00', '17:00:00', true),
(5, 5, '09:00:00', '17:00:00', true),
(5, 6, '09:00:00', '12:15:00', true);
