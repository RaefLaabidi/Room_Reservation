-- Safe Migration: Add course relationship to events table
-- This migration preserves all existing data

-- Add the course_id column (nullable to maintain compatibility)
ALTER TABLE events ADD COLUMN IF NOT EXISTS course_id BIGINT;

-- Add foreign key constraint
ALTER TABLE events 
ADD CONSTRAINT IF NOT EXISTS fk_events_course 
FOREIGN KEY (course_id) REFERENCES courses(id) 
ON DELETE SET NULL;

-- Create helpful indexes for performance
CREATE INDEX IF NOT EXISTS idx_events_course_id ON events(course_id);
CREATE INDEX IF NOT EXISTS idx_events_date_course ON events(date, course_id);
CREATE INDEX IF NOT EXISTS idx_events_teacher_course ON events(teacher_id, course_id);

-- Add a helpful comment
COMMENT ON COLUMN events.course_id IS 'Optional link to course for course-type events. NULL for non-course events (meetings, exams, etc.)';

-- Verify the migration
SELECT 
    'Migration completed successfully!' as status,
    COUNT(*) as total_events,
    COUNT(course_id) as events_with_courses,
    COUNT(*) - COUNT(course_id) as events_without_courses
FROM events;

COMMIT;
