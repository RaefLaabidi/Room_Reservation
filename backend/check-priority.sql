-- Check if there are any events scheduled and their order
SELECT 
    e.id,
    e.title,
    e.date,
    e.start_time,
    c.name as course_name,
    c.subject,
    tca.priority
FROM events e
LEFT JOIN courses c ON e.course_id = c.id
LEFT JOIN template_course_assignments tca ON tca.course_id = c.id
ORDER BY e.date, e.start_time;

-- Check template_course_assignments table for priority data
SELECT 
    tca.id,
    c.name as course_name,
    c.subject,
    tca.priority,
    tca.student_count,
    st.name as template_name
FROM template_course_assignments tca
JOIN courses c ON tca.course_id = c.id
JOIN schedule_templates st ON tca.template_id = st.id
ORDER BY tca.priority DESC;

-- Check if there are any schedule templates
SELECT * FROM schedule_templates;