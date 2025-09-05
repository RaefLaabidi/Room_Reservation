-- 🏢 INTELLIGENT ROOM LOCATION UPDATE SCRIPT
-- Updates room locations to perfectly match course subjects for AI room assignment
-- Subjects found: Computer Science, Mathematics, Physics, Chemistry, Business, Economics, 
-- Engineering, Literature, Languages, Psychology, Sociology, Anthropology, Art, Medicine, 
-- Health Sciences, Environmental Science

-- First, let's see current rooms
SELECT id, name, capacity, location FROM rooms ORDER BY id;

-- Update room locations to be AI-compatible with course subjects
-- 🔬 COMPUTER SCIENCE & TECHNOLOGY ROOMS
UPDATE rooms SET location = 'Computer Lab Building - Programming Lab' WHERE id = 1;
UPDATE rooms SET location = 'Computer Science Building - Tech Center' WHERE id = 2;
UPDATE rooms SET location = 'Engineering Building - Computer Workshop' WHERE id = 3;
UPDATE rooms SET location = 'Tech Center - Advanced Computing Lab' WHERE id = 4;
UPDATE rooms SET location = 'IT Building - Software Development Lab' WHERE id = 5;

-- 🧮 MATHEMATICS ROOMS
UPDATE rooms SET location = 'Mathematics Building - Lecture Hall' WHERE id = 6;
UPDATE rooms SET location = 'Science Building - Math Classroom' WHERE id = 7;
UPDATE rooms SET location = 'Academic Center - Mathematics Seminar Room' WHERE id = 8;

-- 🔬 PHYSICS & CHEMISTRY LABORATORY ROOMS
UPDATE rooms SET location = 'Science Building - Physics Lab' WHERE id = 9;
UPDATE rooms SET location = 'Science Building - Chemistry Lab' WHERE id = 10;
UPDATE rooms SET location = 'Research Center - Advanced Science Lab' WHERE id = 11;
UPDATE rooms SET location = 'Physics Building - Experiment Lab' WHERE id = 12;
UPDATE rooms SET location = 'Chemistry Building - Research Lab' WHERE id = 13;

-- 💼 BUSINESS & ECONOMICS ROOMS
UPDATE rooms SET location = 'Business Building - Conference Room' WHERE id = 14;
UPDATE rooms SET location = 'Business Center - Executive Boardroom' WHERE id = 15;
UPDATE rooms SET location = 'Management Building - Seminar Hall' WHERE id = 16;
UPDATE rooms SET location = 'Business Building - Case Study Room' WHERE id = 17;
UPDATE rooms SET location = 'Economics Department - Lecture Hall' WHERE id = 18;

-- ⚙️ ENGINEERING ROOMS
UPDATE rooms SET location = 'Engineering Building - Workshop Lab' WHERE id = 19;
UPDATE rooms SET location = 'Engineering Center - Mechanical Lab' WHERE id = 20;
UPDATE rooms SET location = 'Tech Building - Engineering Design Studio' WHERE id = 21;
UPDATE rooms SET location = 'Engineering Building - Electrical Lab' WHERE id = 22;

-- 🌐 LANGUAGE & LITERATURE ROOMS
UPDATE rooms SET location = 'Humanities Building - Language Lab' WHERE id = 23;
UPDATE rooms SET location = 'Liberal Arts Building - Literature Seminar Room' WHERE id = 24;
UPDATE rooms SET location = 'Language Center - Communication Lab' WHERE id = 25;
UPDATE rooms SET location = 'Foreign Languages Building - Conversation Room' WHERE id = 26;

-- 🧠 PSYCHOLOGY & SOCIAL SCIENCE ROOMS
UPDATE rooms SET location = 'Social Sciences Building - Psychology Lab' WHERE id = 27;
UPDATE rooms SET location = 'Psychology Department - Research Room' WHERE id = 28;
UPDATE rooms SET location = 'Social Studies Building - Sociology Seminar Room' WHERE id = 29;
UPDATE rooms SET location = 'Anthropology Building - Research Lab' WHERE id = 30;

-- 🎨 ART & DESIGN ROOMS
UPDATE rooms SET location = 'Arts Building - Creative Studio' WHERE id = 31;
UPDATE rooms SET location = 'Design Center - Art Workshop' WHERE id = 32;
UPDATE rooms SET location = 'Creative Arts Building - Digital Media Studio' WHERE id = 33;
UPDATE rooms SET location = 'Art Department - Photography Lab' WHERE id = 34;

-- 🏥 MEDICAL & HEALTH SCIENCE ROOMS
UPDATE rooms SET location = 'Medical Building - Anatomy Lab' WHERE id = 35;
UPDATE rooms SET location = 'Health Sciences Building - Clinical Room' WHERE id = 36;
UPDATE rooms SET location = 'Medical Center - Research Lab' WHERE id = 37;
UPDATE rooms SET location = 'Health Department - Public Health Seminar Room' WHERE id = 38;

-- 🌍 ENVIRONMENTAL SCIENCE ROOMS
UPDATE rooms SET location = 'Environmental Sciences Building - Green Lab' WHERE id = 39;
UPDATE rooms SET location = 'Sustainability Center - Environment Research Lab' WHERE id = 40;
UPDATE rooms SET location = 'Environmental Studies - Field Research Room' WHERE id = 41;

-- 🏛️ GENERAL PURPOSE ROOMS (Fallback for any subject)
UPDATE rooms SET location = 'Main Building - Lecture Hall A' WHERE id = 42;
UPDATE rooms SET location = 'Academic Center - Classroom 201' WHERE id = 43;
UPDATE rooms SET location = 'Main Building - Lecture Hall B' WHERE id = 44;
UPDATE rooms SET location = 'Academic Building - General Classroom' WHERE id = 45;

-- If there are more rooms, update them to general lecture halls
UPDATE rooms SET location = 'Main Building - Lecture Hall C' WHERE id > 45 AND id <= 50;
UPDATE rooms SET location = 'Academic Center - General Classroom' WHERE id > 50 AND id <= 55;
UPDATE rooms SET location = 'Campus Center - Multi-Purpose Room' WHERE id > 55;

-- Verify the updates
SELECT id, name, capacity, location FROM rooms ORDER BY id;

-- Show perfect AI room-to-subject matching for verification
SELECT 'Computer Science → ' as subject_match, id, name, location 
FROM rooms 
WHERE location LIKE '%Computer%' OR location LIKE '%Tech%' OR location LIKE '%Programming%' OR location LIKE '%IT %'
UNION ALL
SELECT 'Mathematics → ' as subject_match, id, name, location 
FROM rooms 
WHERE location LIKE '%Math%'
UNION ALL
SELECT 'Physics → ' as subject_match, id, name, location 
FROM rooms 
WHERE location LIKE '%Physics%' OR location LIKE '%Experiment%'
UNION ALL
SELECT 'Chemistry → ' as subject_match, id, name, location 
FROM rooms 
WHERE location LIKE '%Chemistry%'
UNION ALL
SELECT 'Business/Economics → ' as subject_match, id, name, location 
FROM rooms 
WHERE location LIKE '%Business%' OR location LIKE '%Conference%' OR location LIKE '%Boardroom%' OR location LIKE '%Economics%'
UNION ALL
SELECT 'Engineering → ' as subject_match, id, name, location 
FROM rooms 
WHERE location LIKE '%Engineering%' OR location LIKE '%Workshop%' OR location LIKE '%Mechanical%' OR location LIKE '%Electrical%'
UNION ALL
SELECT 'Languages/Literature → ' as subject_match, id, name, location 
FROM rooms 
WHERE location LIKE '%Language%' OR location LIKE '%Literature%' OR location LIKE '%Humanities%' OR location LIKE '%Communication%'
UNION ALL
SELECT 'Psychology/Social Sciences → ' as subject_match, id, name, location 
FROM rooms 
WHERE location LIKE '%Psychology%' OR location LIKE '%Social%' OR location LIKE '%Sociology%' OR location LIKE '%Anthropology%'
UNION ALL
SELECT 'Art/Design → ' as subject_match, id, name, location 
FROM rooms 
WHERE location LIKE '%Art%' OR location LIKE '%Creative%' OR location LIKE '%Design%' OR location LIKE '%Studio%'
UNION ALL
SELECT 'Medicine/Health → ' as subject_match, id, name, location 
FROM rooms 
WHERE location LIKE '%Medical%' OR location LIKE '%Health%' OR location LIKE '%Anatomy%' OR location LIKE '%Clinical%'
UNION ALL
SELECT 'Environmental Science → ' as subject_match, id, name, location 
FROM rooms 
WHERE location LIKE '%Environment%' OR location LIKE '%Green%' OR location LIKE '%Sustainability%'
ORDER BY subject_match, id;

-- Verify the updates
SELECT id, name, capacity, location FROM rooms ORDER BY id;

-- Show room-to-subject matching for verification
SELECT 
    'Computer Science courses will use:' as info,
    id, name, location 
FROM rooms 
WHERE location LIKE '%Computer%' OR location LIKE '%Tech%' OR location LIKE '%Programming%'
UNION ALL
SELECT 
    'Mathematics courses will use:' as info,
    id, name, location 
FROM rooms 
WHERE location LIKE '%Math%'
UNION ALL
SELECT 
    'Science courses will use:' as info,
    id, name, location 
FROM rooms 
WHERE location LIKE '%Lab%' AND (location LIKE '%Science%' OR location LIKE '%Physics%' OR location LIKE '%Chemistry%' OR location LIKE '%Biology%')
UNION ALL
SELECT 
    'Business courses will use:' as info,
    id, name, location 
FROM rooms 
WHERE location LIKE '%Business%' OR location LIKE '%Conference%' OR location LIKE '%Boardroom%';
