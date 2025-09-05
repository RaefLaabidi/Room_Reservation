-- Auto-Scheduling Database Schema Extensions

-- Teacher availability patterns (recurring weekly availability)
CREATE TABLE teacher_availability (
    id BIGSERIAL PRIMARY KEY,
    teacher_id BIGINT NOT NULL REFERENCES users(id),
    day_of_week INTEGER NOT NULL, -- 1=Monday, 2=Tuesday, ..., 7=Sunday
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    is_available BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT valid_day_of_week CHECK (day_of_week >= 1 AND day_of_week <= 7),
    CONSTRAINT valid_time_range CHECK (start_time < end_time)
);

-- Teacher temporary unavailability (specific dates - holidays, meetings, etc.)
CREATE TABLE teacher_unavailability (
    id BIGSERIAL PRIMARY KEY,
    teacher_id BIGINT NOT NULL REFERENCES users(id),
    unavailable_date DATE NOT NULL,
    start_time TIME,
    end_time TIME,
    reason VARCHAR(255),
    all_day BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Course definitions (what needs to be scheduled)
CREATE TABLE courses (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    subject VARCHAR(255) NOT NULL,
    duration_hours INTEGER NOT NULL, -- Duration in hours
    sessions_per_week INTEGER DEFAULT 1,
    min_capacity INTEGER DEFAULT 1,
    preferred_room_type VARCHAR(255), -- 'LAB', 'LECTURE', 'SEMINAR', etc.
    department VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Schedule templates (admin defines what to schedule for a week)
CREATE TABLE schedule_templates (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    week_start_date DATE NOT NULL,
    week_end_date DATE NOT NULL,
    created_by BIGINT NOT NULL REFERENCES users(id),
    status VARCHAR(50) DEFAULT 'DRAFT', -- 'DRAFT', 'PUBLISHED', 'ARCHIVED'
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Course assignments in a schedule template
CREATE TABLE template_course_assignments (
    id BIGSERIAL PRIMARY KEY,
    template_id BIGINT NOT NULL REFERENCES schedule_templates(id),
    course_id BIGINT NOT NULL REFERENCES courses(id),
    assigned_teacher_id BIGINT REFERENCES users(id), -- Can be null if auto-assign
    preferred_time_start TIME,
    preferred_time_end TIME,
    preferred_days VARCHAR(20), -- e.g., "1,3,5" for Mon,Wed,Fri
    student_count INTEGER,
    priority INTEGER DEFAULT 1, -- Higher number = higher priority
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Generated schedules (results of auto-scheduling)
CREATE TABLE generated_schedules (
    id BIGSERIAL PRIMARY KEY,
    template_id BIGINT NOT NULL REFERENCES schedule_templates(id),
    total_courses INTEGER,
    scheduled_courses INTEGER,
    failed_courses INTEGER,
    conflicts_count INTEGER,
    generation_status VARCHAR(50) DEFAULT 'PENDING', -- 'PENDING', 'SUCCESS', 'FAILED', 'PARTIAL'
    generation_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    notes TEXT
);

-- Teacher preferences (soft constraints)
CREATE TABLE teacher_preferences (
    id BIGSERIAL PRIMARY KEY,
    teacher_id BIGINT NOT NULL REFERENCES users(id),
    preferred_start_time TIME,
    preferred_end_time TIME,
    max_hours_per_day INTEGER DEFAULT 8,
    max_consecutive_hours INTEGER DEFAULT 4,
    lunch_break_start TIME DEFAULT '12:00',
    lunch_break_end TIME DEFAULT '13:00',
    preferred_days VARCHAR(20), -- e.g., "1,2,3,4,5" for weekdays only
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Subjects that teachers can teach
CREATE TABLE teacher_subjects (
    id BIGSERIAL PRIMARY KEY,
    teacher_id BIGINT NOT NULL REFERENCES users(id),
    subject VARCHAR(255) NOT NULL,
    expertise_level INTEGER DEFAULT 1, -- 1=Basic, 2=Intermediate, 3=Expert
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(teacher_id, subject)
);

-- Add indexes for better performance
CREATE INDEX idx_teacher_availability_teacher_day ON teacher_availability(teacher_id, day_of_week);
CREATE INDEX idx_teacher_unavailability_teacher_date ON teacher_unavailability(teacher_id, unavailable_date);
CREATE INDEX idx_template_assignments_template ON template_course_assignments(template_id);
CREATE INDEX idx_teacher_subjects_teacher ON teacher_subjects(teacher_id);
CREATE INDEX idx_teacher_subjects_subject ON teacher_subjects(subject);

-- Insert some sample data
-- Sample teacher availability (Dr. Sarah Wilson available Mon-Fri 8-17)
INSERT INTO teacher_availability (teacher_id, day_of_week, start_time, end_time) VALUES
(5, 1, '08:00', '17:00'), -- Monday
(5, 2, '08:00', '17:00'), -- Tuesday  
(5, 3, '08:00', '17:00'), -- Wednesday
(5, 4, '08:00', '17:00'), -- Thursday
(5, 5, '08:00', '17:00'); -- Friday

-- Sample teacher subjects
INSERT INTO teacher_subjects (teacher_id, subject, expertise_level) VALUES
(5, 'Computer Science', 3),
(5, 'Programming', 3),
(6, 'Mathematics', 3),
(6, 'Statistics', 2);

-- Sample courses
INSERT INTO courses (name, subject, duration_hours, sessions_per_week, min_capacity) VALUES
('Introduction to Programming', 'Programming', 2, 2, 20),
('Advanced Java', 'Programming', 3, 1, 15),
('Calculus I', 'Mathematics', 2, 3, 30),
('Statistics', 'Statistics', 2, 2, 25);

-- Sample teacher preferences
INSERT INTO teacher_preferences (teacher_id, preferred_start_time, preferred_end_time, max_hours_per_day) VALUES
(5, '09:00', '16:00', 6),
(6, '08:00', '17:00', 8);
