-- Update script to fix password hashes for university users
-- This will update all university users to use a working BCrypt hash for "mypassword"

-- First, let's get a working hash by checking the newly registered user
-- We'll use a known working BCrypt hash for "mypassword"

-- Update all university teachers and students to use the correct hash
-- Note: This is a BCrypt hash for "mypassword" that works with Spring Boot
UPDATE users 
SET password = (
    SELECT password 
    FROM users 
    WHERE email = 'newteacher@university.edu'
) 
WHERE email LIKE '%@university.edu' 
AND email != 'newteacher@university.edu';

-- Verify the update
SELECT name, email, LEFT(password, 20) as password_start 
FROM users 
WHERE email LIKE '%@university.edu' 
LIMIT 5;
