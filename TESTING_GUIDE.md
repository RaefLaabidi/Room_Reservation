# Reservation Management System - Complete Testing Guide

## 🎯 Overview
This guide will help you test all the features we've implemented across the 5 parts of the system:
- Part 1: Spring Boot Backend Foundation
- Part 2: Enhanced Backend Logic (Scheduling & Conflict Detection)
- Part 3: React Frontend with TypeScript
- Part 4: Schedule UI with Calendar
- Part 5: Conflict Detection UI

---

## 🔧 Pre-Testing Setup

### Backend Status Check
1. **Verify Backend is Running**: http://localhost:8080
   - Should see a basic response or Whitelabel Error Page (expected for root)
   - Check database connection is working

### Frontend Status Check
1. **Verify Frontend is Running**: http://localhost:3000
   - Should load the login page
   - No console errors in browser developer tools

---

## 📋 Testing Checklist

### Part 1: Authentication & Basic CRUD ✅

#### Test 1: User Authentication
**URL**: http://localhost:3000/login

**Test Cases**:
1. **Invalid Login**:
   - Email: `test@test.com`
   - Password: `wrongpassword`
   - Expected: Error message displayed

2. **Valid Login** (if you have test data):
   - Email: `admin@example.com`
   - Password: `password`
   - Expected: Redirect to dashboard

**What to Check**:
- [ ] Login form accepts input
- [ ] Error messages display properly
- [ ] Successful login redirects to dashboard
- [ ] JWT token is stored in localStorage
- [ ] Navigation bar appears after login

#### Test 2: Dashboard Overview
**URL**: http://localhost:3000/dashboard

**What to Check**:
- [ ] Statistics cards show correct counts
- [ ] Tab navigation works (Overview, Events, Rooms, Users)
- [ ] Data loads without errors
- [ ] "Add Event" and "Add Room" buttons are present

#### Test 3: User Management
**Navigate to**: Dashboard → Users Tab

**What to Check**:
- [ ] Users table displays with Name, Email, Role columns
- [ ] Role badges have correct colors (Admin=red, Teacher=blue, Student=green)
- [ ] Table is responsive

#### Test 4: Room Management
**Navigate to**: Dashboard → Rooms Tab

**Test Cases**:
1. **View Rooms**:
   - [ ] Rooms display in card layout
   - [ ] Shows room name, capacity, location

2. **Add New Room**:
   - Click "Add Room" button
   - Fill form: Name="Test Room", Capacity=30, Location="Building A"
   - [ ] Modal opens correctly
   - [ ] Form validation works
   - [ ] Success: Room appears in list
   - [ ] Modal closes after success

#### Test 5: Event Management
**Navigate to**: Dashboard → Events Tab

**Test Cases**:
1. **View Events**:
   - [ ] Events table shows Title/Type, Date & Time, Room, Teacher, Status
   - [ ] Status badges have correct colors

2. **Add New Event**:
   - Click "Add Event" button
   - Fill form with valid data
   - [ ] Form has all required fields
   - [ ] Teacher and Room dropdowns populate
   - [ ] Date and time pickers work
   - [ ] Event appears in table after creation

---

### Part 2: Advanced Backend Logic ✅

#### Test 6: API Endpoints (Use Browser Network Tab)
**Open Browser Developer Tools → Network Tab**

**Test Direct API Calls**:
1. **GET /api/events**: Should return events list
2. **GET /api/rooms**: Should return rooms list
3. **GET /api/users**: Should return users list
4. **GET /api/conflicts**: Should return conflicts (may be empty initially)

**What to Check**:
- [ ] API calls return JSON data
- [ ] Status codes are 200 for success
- [ ] Response structure matches expected format
- [ ] CORS headers allow frontend requests

#### Test 7: Schedule Generation
**Navigate to**: Schedule Page

**Test Cases**:
1. **Generate Schedule Button**:
   - Click "Generate Schedule"
   - [ ] Button shows "Generating..." state
   - [ ] API call to POST /api/events/generate
   - [ ] Calendar updates with new events

---

### Part 3: React Frontend Foundation ✅

#### Test 8: Navigation & Routing
**Test Cases**:
1. **Navigation Menu**:
   - [ ] Navbar shows Dashboard, Schedule, Conflicts links
   - [ ] Logo/brand name appears
   - [ ] Logout button functions

2. **Protected Routes**:
   - Try accessing http://localhost:3000/dashboard without login
   - [ ] Should redirect to login page

3. **Responsive Design**:
   - Resize browser window
   - [ ] Layout adapts to mobile view
   - [ ] Navigation collapses on mobile

#### Test 9: TypeScript Integration
**Open Browser Console**:
- [ ] No TypeScript compilation errors
- [ ] Intellisense works in development
- [ ] Type safety prevents runtime errors

---

### Part 4: Schedule UI with Calendar ✅

#### Test 10: Calendar Functionality
**Navigate to**: http://localhost:3000/schedule

**Test Cases**:
1. **Calendar Views**:
   - [ ] Month view button works
   - [ ] Week view button works
   - [ ] Day view button works
   - [ ] Current view is highlighted

2. **Event Display**:
   - [ ] Events appear on calendar
   - [ ] Event colors: Blue=Course, Orange=Defense, Green=Meeting
   - [ ] Events with conflicts have red borders

3. **Event Legend**:
   - [ ] Legend shows all event types with correct colors
   - [ ] "Has Conflict" indicator visible

#### Test 11: Event Interaction
**Test Cases**:
1. **Click on Calendar Event**:
   - [ ] Side drawer opens from right
   - [ ] Event details display correctly
   - [ ] Close button (X) works

2. **Event Details Drawer**:
   - [ ] Shows Title, Type, Date & Time, Room, Teacher, Status
   - [ ] Type badge has correct color
   - [ ] Status badge has correct color
   - [ ] Description appears if present

3. **Event Actions**:
   - [ ] "Edit" button opens edit modal
   - [ ] "Delete" button shows confirmation dialog
   - [ ] Actions affect the calendar display

#### Test 12: Add Event from Schedule
**Test Cases**:
1. **Add Event Button**:
   - [ ] Opens event form modal
   - [ ] All form fields are present
   - [ ] Can save and event appears on calendar

---

### Part 5: Conflict Detection UI ✅

#### Test 13: Conflict Detection Page
**Navigate to**: http://localhost:3000/conflicts

**Test Cases**:
1. **Page Layout**:
   - [ ] "Run Conflict Detection" button present
   - [ ] Summary cards show counts by type
   - [ ] Search bar for filtering

2. **Conflict Display**:
   - Desktop: [ ] Table view with all columns
   - Mobile: [ ] Collapsible card view
   - [ ] Color coding: Red=Room, Purple=Teacher, Yellow=Capacity

#### Test 14: Search and Filter
**Test Cases**:
1. **Search Functionality**:
   - Type teacher name in search bar
   - [ ] Results filter correctly
   - [ ] "No Matching Conflicts" message when appropriate

#### Test 15: Conflict Resolution
**Test Cases**:
1. **Reschedule Modal**:
   - Click "Reschedule" on a conflict
   - [ ] Modal opens with date/time pickers
   - [ ] Form validation works
   - [ ] Can update event time

2. **Change Room Modal**:
   - Click "Change Room" on a conflict
   - [ ] Modal opens with room dropdown
   - [ ] Available rooms listed with capacity info
   - [ ] Can assign new room

3. **Ignore Conflict**:
   - Click "Ignore" on a conflict
   - [ ] Confirmation dialog appears
   - [ ] Conflict removed from list

#### Test 16: Mobile Responsiveness
**Test on Mobile/Narrow Browser**:
1. **Conflicts Page**:
   - [ ] Table converts to cards
   - [ ] Cards show all necessary info
   - [ ] Actions buttons work in mobile view

2. **Schedule Page**:
   - [ ] Calendar adjusts for mobile
   - [ ] Side drawer works on mobile
   - [ ] Buttons stack properly

---

## 🚨 Error Scenarios to Test

#### Test 17: Error Handling
1. **Network Errors**:
   - Stop backend server
   - Try to perform actions
   - [ ] Appropriate error messages display
   - [ ] App doesn't crash

2. **Validation Errors**:
   - Submit forms with invalid data
   - [ ] Client-side validation works
   - [ ] Server-side validation messages display

3. **Authentication Errors**:
   - Clear localStorage
   - Try to access protected pages
   - [ ] Redirects to login appropriately

---

## 📊 Performance Testing

#### Test 18: Performance Check
1. **Page Load Times**:
   - [ ] Initial load under 3 seconds
   - [ ] Navigation between pages is smooth

2. **Data Loading**:
   - [ ] Loading spinners appear during API calls
   - [ ] Large datasets load without freezing

---

## 🎉 Success Criteria

### All Features Working ✅
- [ ] User can log in successfully
- [ ] Dashboard shows correct data
- [ ] Can create/view/edit events and rooms
- [ ] Calendar displays events with proper colors
- [ ] Conflict detection identifies issues
- [ ] Can resolve conflicts through UI
- [ ] Mobile responsive design works
- [ ] No console errors
- [ ] All API endpoints respond correctly

### Integration Complete ✅
- [ ] Frontend connects to backend APIs
- [ ] Data persists in database
- [ ] Real-time updates work
- [ ] Security (JWT) functions properly

---

## 🐛 Common Issues & Solutions

1. **CORS Errors**: Check backend CORS configuration
2. **API Not Found**: Verify backend is running on port 8080
3. **Database Connection**: Check PostgreSQL/H2 configuration
4. **Build Errors**: Run `npm install` and check dependencies

---

## 📝 Test Results Template

Copy this and fill out as you test:

```
✅ Authentication: ___/5 tests passed
✅ Dashboard: ___/4 tests passed  
✅ Room Management: ___/3 tests passed
✅ Event Management: ___/4 tests passed
✅ Schedule UI: ___/6 tests passed
✅ Conflict Detection: ___/8 tests passed
✅ Mobile Responsive: ___/3 tests passed
✅ Error Handling: ___/3 tests passed

Overall Score: ___/36 tests passed
```

---

Start with the authentication tests and work your way through each section. Let me know which tests pass/fail so I can help fix any issues!
