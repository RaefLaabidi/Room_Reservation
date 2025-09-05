# Reservation Management System - Frontend

A modern React 18 + TypeScript frontend application built with Vite for the reservation management system.

## Features

- **Modern Tech Stack**: React 18, TypeScript, Vite, Tailwind CSS
- **Interactive Calendar**: React Big Calendar for schedule visualization
- **Responsive Design**: Mobile-first design with Tailwind CSS
- **Form Management**: User-friendly forms with validation
- **API Integration**: Axios-based API client with authentication
- **Routing**: React Router DOM for navigation
- **Component Library**: Headless UI for accessible components

## Prerequisites

- Node.js 16+ 
- npm or yarn

## Installation

1. **Install dependencies:**
   ```bash
   npm install
   ```

## Development

```bash
# Start development server
npm run dev
```

The application will be available at `http://localhost:3000`

## Build

```bash
# Build for production
npm run build
```

## Project Structure

```
src/
├── components/          # Reusable UI components
│   ├── Navbar.tsx      # Navigation bar
│   ├── EventForm.tsx   # Event creation form
│   ├── RoomForm.tsx    # Room creation form
│   └── ConflictCard.tsx # Conflict display card
├── pages/              # Page components
│   ├── Login.tsx       # Authentication page
│   ├── Dashboard.tsx   # Admin dashboard
│   ├── Schedule.tsx    # Interactive calendar
│   └── Conflicts.tsx   # Conflict detection page
├── services/           # API services
│   └── api.ts          # Axios configuration and API calls
├── types/              # TypeScript type definitions
│   ├── User.ts         # User-related types
│   ├── Room.ts         # Room-related types
│   ├── Event.ts        # Event-related types
│   ├── Availability.ts # Availability types
│   ├── Conflict.ts     # Conflict types
│   └── index.ts        # Type exports
├── App.tsx             # Main application component
├── main.tsx            # Application entry point
└── index.css           # Global styles with Tailwind
```

## Features

### 1. Authentication
- Login page with form validation
- JWT token management
- Automatic token refresh
- Protected routes

### 2. Dashboard
- System overview with statistics
- Tabbed interface for different data views
- Users, rooms, and events management
- Quick actions and forms

### 3. Interactive Schedule
- React Big Calendar integration
- Multiple view modes (month, week, day)
- Event details sidebar
- Color-coded event types
- Click to view event details

### 4. Conflict Detection
- Visual conflict cards
- Grouped by conflict type (Room, Teacher, Capacity)
- Detailed conflict descriptions
- Quick action buttons

### 5. Forms
- Event creation with validation
- Room creation
- Dropdown selections for teachers and rooms
- Date and time pickers

## Routing

- `/login` → Authentication page
- `/dashboard` → Main dashboard with system overview
- `/schedule` → Interactive calendar view
- `/conflicts` → Conflict detection and resolution

## API Integration

The frontend connects to the Spring Boot backend at `http://localhost:8080/api` with the following endpoints:

- **Authentication**: `/auth/login`
- **Users**: `/users` (CRUD operations)
- **Rooms**: `/rooms` (CRUD operations)
- **Events**: `/events` (CRUD + scheduling)
- **Availabilities**: `/availabilities` (CRUD operations)
- **Conflicts**: `/conflicts` (detection and listing)

## Styling

- **Tailwind CSS**: Utility-first CSS framework
- **Responsive Design**: Mobile-first approach
- **Custom Calendar Styles**: Enhanced React Big Calendar appearance
- **Component Variants**: Consistent color schemes and spacing

## TypeScript

Fully typed application with:
- API response/request types
- Component prop types
- Enum definitions for backend enums
- Strict type checking enabled

## Development Scripts

```bash
npm run dev      # Start development server
npm run build    # Build for production
npm run preview  # Preview production build
npm run lint     # Run ESLint
```

## Browser Support

- Chrome (latest)
- Firefox (latest)
- Safari (latest)
- Edge (latest)

## Contributing

1. Follow TypeScript best practices
2. Use Tailwind CSS for styling
3. Maintain component modularity
4. Add proper error handling
5. Write meaningful commit messages
