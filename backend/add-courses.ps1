# Professional University Course Catalog Enhancement Script
# This PowerShell script adds a comprehensive set of realistic university courses

Write-Host "Adding Professional University Course Catalog..." -ForegroundColor Green
Write-Host "This will add 50+ courses across multiple departments" -ForegroundColor Yellow

$baseUrl = "http://localhost:8080/api/courses"
$headers = @{ "Content-Type" = "application/json" }

# Define comprehensive course catalog
$courses = @(
    # Computer Science & Engineering
    @{
        name = "Data Structures and Algorithms"
        subject = "Computer Science"
        durationHours = 4
        sessionsPerWeek = 2
        minCapacity = 20
        preferredRoomType = "Computer Lab"
    },
    @{
        name = "Machine Learning Fundamentals"
        subject = "Computer Science"
        durationHours = 3
        sessionsPerWeek = 2
        minCapacity = 15
        preferredRoomType = "Computer Lab"
    },
    @{
        name = "Software Engineering Principles"
        subject = "Computer Science"
        durationHours = 3
        sessionsPerWeek = 2
        minCapacity = 25
        preferredRoomType = "Lecture Hall"
    },
    @{
        name = "Database Systems Design"
        subject = "Computer Science"
        durationHours = 3
        sessionsPerWeek = 2
        minCapacity = 20
        preferredRoomType = "Computer Lab"
    },
    @{
        name = "Computer Networks"
        subject = "Computer Science"
        durationHours = 3
        sessionsPerWeek = 2
        minCapacity = 18
        preferredRoomType = "Computer Lab"
    },
    @{
        name = "Cybersecurity Fundamentals"
        subject = "Computer Science"
        durationHours = 3
        sessionsPerWeek = 2
        minCapacity = 15
        preferredRoomType = "Computer Lab"
    },
    @{
        name = "Web Development Advanced"
        subject = "Computer Science"
        durationHours = 4
        sessionsPerWeek = 2
        minCapacity = 20
        preferredRoomType = "Computer Lab"
    },
    @{
        name = "Mobile App Development"
        subject = "Computer Science"
        durationHours = 4
        sessionsPerWeek = 2
        minCapacity = 16
        preferredRoomType = "Computer Lab"
    },
    @{
        name = "Artificial Intelligence"
        subject = "Computer Science"
        durationHours = 4
        sessionsPerWeek = 2
        minCapacity = 12
        preferredRoomType = "Computer Lab"
    },
    @{
        name = "Cloud Computing Architecture"
        subject = "Computer Science"
        durationHours = 3
        sessionsPerWeek = 2
        minCapacity = 18
        preferredRoomType = "Computer Lab"
    },

    # Mathematics
    @{
        name = "Calculus II"
        subject = "Mathematics"
        durationHours = 4
        sessionsPerWeek = 3
        minCapacity = 25
        preferredRoomType = "Classroom"
    },
    @{
        name = "Calculus III"
        subject = "Mathematics"
        durationHours = 4
        sessionsPerWeek = 3
        minCapacity = 25
        preferredRoomType = "Classroom"
    },
    @{
        name = "Linear Algebra"
        subject = "Mathematics"
        durationHours = 3
        sessionsPerWeek = 2
        minCapacity = 20
        preferredRoomType = "Classroom"
    },
    @{
        name = "Discrete Mathematics"
        subject = "Mathematics"
        durationHours = 3
        sessionsPerWeek = 2
        minCapacity = 22
        preferredRoomType = "Classroom"
    },
    @{
        name = "Statistics and Probability"
        subject = "Mathematics"
        durationHours = 3
        sessionsPerWeek = 2
        minCapacity = 25
        preferredRoomType = "Classroom"
    },
    @{
        name = "Differential Equations"
        subject = "Mathematics"
        durationHours = 3
        sessionsPerWeek = 2
        minCapacity = 18
        preferredRoomType = "Classroom"
    },
    @{
        name = "Number Theory"
        subject = "Mathematics"
        durationHours = 3
        sessionsPerWeek = 2
        minCapacity = 15
        preferredRoomType = "Classroom"
    },

    # Physics
    @{
        name = "General Physics I"
        subject = "Physics"
        durationHours = 4
        sessionsPerWeek = 2
        minCapacity = 20
        preferredRoomType = "Physics Lab"
    },
    @{
        name = "General Physics II"
        subject = "Physics"
        durationHours = 4
        sessionsPerWeek = 2
        minCapacity = 20
        preferredRoomType = "Physics Lab"
    },
    @{
        name = "Quantum Physics"
        subject = "Physics"
        durationHours = 4
        sessionsPerWeek = 2
        minCapacity = 15
        preferredRoomType = "Physics Lab"
    },
    @{
        name = "Thermodynamics"
        subject = "Physics"
        durationHours = 3
        sessionsPerWeek = 2
        minCapacity = 20
        preferredRoomType = "Classroom"
    },
    @{
        name = "Electromagnetism"
        subject = "Physics"
        durationHours = 4
        sessionsPerWeek = 2
        minCapacity = 18
        preferredRoomType = "Physics Lab"
    },

    # Chemistry
    @{
        name = "General Chemistry I"
        subject = "Chemistry"
        durationHours = 4
        sessionsPerWeek = 2
        minCapacity = 18
        preferredRoomType = "Chemistry Lab"
    },
    @{
        name = "Organic Chemistry I"
        subject = "Chemistry"
        durationHours = 4
        sessionsPerWeek = 2
        minCapacity = 16
        preferredRoomType = "Chemistry Lab"
    },
    @{
        name = "Organic Chemistry II"
        subject = "Chemistry"
        durationHours = 4
        sessionsPerWeek = 2
        minCapacity = 18
        preferredRoomType = "Chemistry Lab"
    },
    @{
        name = "Physical Chemistry"
        subject = "Chemistry"
        durationHours = 4
        sessionsPerWeek = 2
        minCapacity = 16
        preferredRoomType = "Chemistry Lab"
    },
    @{
        name = "Analytical Chemistry"
        subject = "Chemistry"
        durationHours = 3
        sessionsPerWeek = 2
        minCapacity = 20
        preferredRoomType = "Chemistry Lab"
    },

    # Business Administration
    @{
        name = "Principles of Management"
        subject = "Business"
        durationHours = 3
        sessionsPerWeek = 2
        minCapacity = 30
        preferredRoomType = "Lecture Hall"
    },
    @{
        name = "Strategic Management"
        subject = "Business"
        durationHours = 3
        sessionsPerWeek = 2
        minCapacity = 25
        preferredRoomType = "Classroom"
    },
    @{
        name = "Financial Management"
        subject = "Business"
        durationHours = 3
        sessionsPerWeek = 2
        minCapacity = 20
        preferredRoomType = "Classroom"
    },
    @{
        name = "Marketing Strategy"
        subject = "Business"
        durationHours = 3
        sessionsPerWeek = 2
        minCapacity = 22
        preferredRoomType = "Classroom"
    },
    @{
        name = "Operations Management"
        subject = "Business"
        durationHours = 3
        sessionsPerWeek = 2
        minCapacity = 20
        preferredRoomType = "Classroom"
    },
    @{
        name = "Human Resource Management"
        subject = "Business"
        durationHours = 3
        sessionsPerWeek = 2
        minCapacity = 25
        preferredRoomType = "Classroom"
    },
    @{
        name = "International Business"
        subject = "Business"
        durationHours = 3
        sessionsPerWeek = 2
        minCapacity = 18
        preferredRoomType = "Classroom"
    },
    @{
        name = "Entrepreneurship"
        subject = "Business"
        durationHours = 3
        sessionsPerWeek = 2
        minCapacity = 16
        preferredRoomType = "Classroom"
    },

    # Economics
    @{
        name = "Microeconomics"
        subject = "Economics"
        durationHours = 3
        sessionsPerWeek = 2
        minCapacity = 25
        preferredRoomType = "Lecture Hall"
    },
    @{
        name = "Macroeconomics"
        subject = "Economics"
        durationHours = 3
        sessionsPerWeek = 2
        minCapacity = 25
        preferredRoomType = "Lecture Hall"
    },
    @{
        name = "Econometrics"
        subject = "Economics"
        durationHours = 4
        sessionsPerWeek = 2
        minCapacity = 18
        preferredRoomType = "Computer Lab"
    },
    @{
        name = "Development Economics"
        subject = "Economics"
        durationHours = 3
        sessionsPerWeek = 2
        minCapacity = 20
        preferredRoomType = "Classroom"
    },

    # Engineering
    @{
        name = "Mechanical Engineering Design"
        subject = "Engineering"
        durationHours = 4
        sessionsPerWeek = 2
        minCapacity = 18
        preferredRoomType = "Engineering Lab"
    },
    @{
        name = "Electrical Circuits"
        subject = "Engineering"
        durationHours = 4
        sessionsPerWeek = 2
        minCapacity = 20
        preferredRoomType = "Engineering Lab"
    },
    @{
        name = "Materials Science"
        subject = "Engineering"
        durationHours = 3
        sessionsPerWeek = 2
        minCapacity = 22
        preferredRoomType = "Engineering Lab"
    },
    @{
        name = "Control Systems"
        subject = "Engineering"
        durationHours = 4
        sessionsPerWeek = 2
        minCapacity = 15
        preferredRoomType = "Engineering Lab"
    },

    # Literature & Languages
    @{
        name = "English Literature"
        subject = "Literature"
        durationHours = 3
        sessionsPerWeek = 2
        minCapacity = 20
        preferredRoomType = "Classroom"
    },
    @{
        name = "Creative Writing"
        subject = "Literature"
        durationHours = 2
        sessionsPerWeek = 2
        minCapacity = 12
        preferredRoomType = "Classroom"
    },
    @{
        name = "Spanish Language"
        subject = "Languages"
        durationHours = 3
        sessionsPerWeek = 3
        minCapacity = 15
        preferredRoomType = "Language Lab"
    },
    @{
        name = "French Language"
        subject = "Languages"
        durationHours = 3
        sessionsPerWeek = 3
        minCapacity = 15
        preferredRoomType = "Language Lab"
    },

    # Psychology & Social Sciences
    @{
        name = "Introduction to Psychology"
        subject = "Psychology"
        durationHours = 3
        sessionsPerWeek = 2
        minCapacity = 30
        preferredRoomType = "Lecture Hall"
    },
    @{
        name = "Cognitive Psychology"
        subject = "Psychology"
        durationHours = 3
        sessionsPerWeek = 2
        minCapacity = 20
        preferredRoomType = "Classroom"
    },
    @{
        name = "Social Psychology"
        subject = "Psychology"
        durationHours = 3
        sessionsPerWeek = 2
        minCapacity = 22
        preferredRoomType = "Classroom"
    },
    @{
        name = "Research Methods"
        subject = "Psychology"
        durationHours = 4
        sessionsPerWeek = 2
        minCapacity = 15
        preferredRoomType = "Computer Lab"
    },

    # Biology & Health Sciences
    @{
        name = "General Biology"
        subject = "Biology"
        durationHours = 4
        sessionsPerWeek = 2
        minCapacity = 20
        preferredRoomType = "Biology Lab"
    },
    @{
        name = "Human Anatomy"
        subject = "Biology"
        durationHours = 5
        sessionsPerWeek = 2
        minCapacity = 18
        preferredRoomType = "Biology Lab"
    },
    @{
        name = "Cell Biology"
        subject = "Biology"
        durationHours = 4
        sessionsPerWeek = 2
        minCapacity = 16
        preferredRoomType = "Biology Lab"
    }
)

$successCount = 0
$errorCount = 0

Write-Host "`n🚀 Starting to add courses..." -ForegroundColor Cyan

foreach ($course in $courses) {
    try {
        $jsonBody = $course | ConvertTo-Json -Depth 3
        $response = Invoke-WebRequest -Uri $baseUrl -Method POST -Body $jsonBody -ContentType "application/json" -TimeoutSec 10
        
        if ($response.StatusCode -eq 200 -or $response.StatusCode -eq 201) {
            $successCount++
            Write-Host "✅ Added: $($course.name)" -ForegroundColor Green
        } else {
            $errorCount++
            Write-Host "❌ Failed: $($course.name) - Status: $($response.StatusCode)" -ForegroundColor Red
        }
    }
    catch {
        $errorCount++
        Write-Host "❌ Error adding $($course.name): $($_.Exception.Message)" -ForegroundColor Red
    }
    
    # Small delay to avoid overwhelming the server
    Start-Sleep -Milliseconds 200
}

Write-Host "`n📊 Summary:" -ForegroundColor Yellow
Write-Host "✅ Successfully added: $successCount courses" -ForegroundColor Green
Write-Host "❌ Errors: $errorCount courses" -ForegroundColor Red
Write-Host "🎯 Total attempted: $($courses.Count) courses" -ForegroundColor Cyan

Write-Host "`n🔍 Verifying final course count..." -ForegroundColor Cyan
try {
    $finalResponse = Invoke-WebRequest -Uri $baseUrl -Method GET -TimeoutSec 5
    $finalCourses = ($finalResponse.Content | ConvertFrom-Json)
    Write-Host "📚 Total courses now in database: $($finalCourses.length)" -ForegroundColor Green
    
    # Group by subject
    $subjects = $finalCourses | Group-Object -Property subject | Sort-Object Name
    Write-Host "`n📋 Courses by Subject:" -ForegroundColor Yellow
    foreach ($subject in $subjects) {
        Write-Host "  $($subject.Name): $($subject.Count) courses" -ForegroundColor White
    }
}
catch {
    Write-Host "❌ Could not verify final count: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "Course catalog enhancement complete!" -ForegroundColor Green
