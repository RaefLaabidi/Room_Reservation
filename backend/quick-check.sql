-- Quick check to see current events and any priority data
SELECT 
    e.id,
    e.title,
    e.date,
    e.start_time,
    e.description,
    c.name as course_name,
    c.subject
FROM events e
LEFT JOIN courses c ON e.course_id = c.id
ORDER BY e.date, e.start_time
LIMIT 20;

-- Check if any template_course_assignments exist (this would show priority)
SELECT COUNT(*) as template_assignments_count FROM template_course_assignments;

-- Check if any schedule_templates exist
SELECT COUNT(*) as schedule_templates_count FROM schedule_templates;