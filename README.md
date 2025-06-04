# Team Task Manager - Modern Task Management Application

![Team Task Manager](https://img.shields.io/badge/Status-Production%20Ready-green)
![Frontend](https://img.shields.io/badge/Frontend-React%20TypeScript-blue)
![Backend](https://img.shields.io/badge/Backend-Spring%20Boot-brightgreen)
![Database](https://img.shields.io/badge/Database-H2-orange)

## 🚀 Overview

Team Task Manager is a modern, animated web application for team task management built with cutting-edge technologies. It features a beautiful glassmorphism design, rich animations, and comprehensive project management capabilities.

## ✨ Key Features

### 🎨 Modern Design
- **Glassmorphism UI** with backdrop-filter effects
- **Animated background** with floating particles
- **Responsive design** that works on all devices
- **Rich animations** using Framer Motion and Lottie
- **Beautiful gradients** and custom CSS properties

### 📊 Task Management
- **Kanban Board** with drag & drop functionality
- **Task CRUD operations** (Create, Read, Update, Delete)
- **Priority system** (High, Medium, Low) with visual indicators
- **Status tracking** (To Do, In Progress, Done)
- **Due date management** with overdue detection
- **User assignment** with team member profiles

### 📈 Advanced Analytics
- **Interactive Dashboard** with real-time statistics
- **Circular progress charts** for completion rates
- **Priority and status distribution** with animated progress bars
- **Team performance tracking** with individual metrics
- **Project completion analytics**

### 🔔 Smart Notifications
- **Real-time notification center** with animated bell icon
- **Overdue task alerts**
- **Due date reminders** (today & tomorrow)
- **Achievement notifications** for progress milestones
- **Animated notification badges**

### 📄 Data Export
- **CSV export** for tasks, team reports, and project summaries
- **Comprehensive reporting** with detailed statistics
- **Data visualization** for better insights

## 🛠 Technology Stack

### Backend
- **Java 17** with Spring Boot 3.x
- **Spring Data JPA** for database operations
- **H2 Database** for development and testing
- **REST API** with JSON serialization
- **Gradle** for build management

### Frontend
- **React 18** with TypeScript
- **Framer Motion** for advanced animations
- **React Beautiful DnD** for drag & drop
- **Lottie React** for animated icons
- **Modern CSS** with custom properties and glassmorphism

## 🏗 Architecture

### Component Structure
```
frontend/src/
├── components/
│   ├── Dashboard.tsx        # Analytics dashboard with charts
│   ├── StatsCard.tsx       # Animated statistics cards
│   ├── TaskCard.tsx        # Individual task components
│   └── NotificationCenter.tsx # Smart notification system
├── utils/
│   └── csvExport.ts        # Data export utilities
├── lottie/
│   └── avatar.json         # Animated avatar data
└── App.tsx                 # Main application component
```

### Backend Structure
```
src/main/java/org/example/
├── Task.java              # Task entity with JPA mapping
├── User.java              # User entity with relationships
├── TaskController.java    # REST API endpoints
├── UserController.java    # User management API
├── TaskRepository.java    # Data access layer
├── UserRepository.java    # User data operations
└── Main.java             # Spring Boot application entry
```

## 🚀 Getting Started

### Prerequisites
- Java 17 or higher
- Node.js 16+ and npm
- Git

### Installation

1. **Clone the repository**
```bash
git clone <repository-url>
cd projekt
```

2. **Start the backend**
```bash
./gradlew bootRun
```
Backend will start on `http://localhost:8080`

3. **Start the frontend** (in a new terminal)
```bash
cd frontend
npm install
npm start
```
Frontend will start on `http://localhost:3000`

### Sample Data
The application includes sample users and tasks:
- **Users**: Jan Kowalski, Anna Nowak
- **Tasks**: Various priority levels and statuses
- **Demo data** for testing all features

## 🎯 Core Functionalities

### 1. Task Management
- Create tasks with title, description, priority, due date
- Assign tasks to team members
- Drag & drop between status columns
- Real-time status updates
- Task history tracking

### 2. Dashboard Analytics
- Project completion rate with circular charts
- Priority distribution visualization
- Team performance metrics
- Real-time statistics updates

### 3. Notification System
- Animated notification bell with badge counter
- Smart alerts for overdue tasks
- Due date reminders
- Progress achievement notifications
- Expandable notification center

### 4. Data Export
- Export tasks to CSV format
- Generate team performance reports
- Create project summary documents
- Download with timestamped filenames

## 🎨 Design Features

### Animations
- **Page transitions** with staggered animations
- **Hover effects** on interactive elements
- **Loading states** with smooth transitions
- **Drag & drop feedback** with rotation effects
- **Notification animations** with spring physics

### Visual Design
- **Glassmorphism cards** with backdrop blur
- **Gradient backgrounds** and borders
- **Animated floating particles**
- **Responsive grid layouts**
- **Modern typography** with Inter font

## 🔧 API Endpoints

### Tasks
- `GET /api/tasks` - Get all tasks
- `POST /api/tasks` - Create new task
- `PUT /api/tasks/{id}` - Update task
- `DELETE /api/tasks/{id}` - Delete task
- `GET /api/tasks/{id}` - Get task details

### Users
- `GET /api/users` - Get all users
- `POST /api/users` - Create new user
- `PUT /api/users/{id}` - Update user
- `DELETE /api/users/{id}` - Delete user

## 📱 Responsive Design

The application is fully responsive with:
- **Mobile-first approach**
- **Flexible grid layouts**
- **Touch-friendly interactions**
- **Optimized animations** for mobile
- **Adaptive component sizing**

## 🎭 Animation Showcase

### Micro-interactions
- Button hover effects with scale and color transitions
- Card animations with lift and shadow effects
- Loading spinners with CSS animations
- Form validation with shake effects

### Page Animations
- Staggered entrance animations
- Route transitions with fade effects
- Modal animations with scale and blur
- Toast notifications sliding from right

## 📊 Performance Features

- **Lazy loading** for components
- **Optimized animations** with transform and opacity
- **Efficient re-renders** with React.memo and useMemo
- **Smooth 60fps animations** with GPU acceleration

## 🛡 Production Ready Features

- **Error handling** with graceful fallbacks
- **Loading states** for better UX
- **Type safety** with TypeScript
- **Cross-browser compatibility**
- **Accessibility** considerations

## 🎯 Project Goals Achieved

✅ **Modern Interface** - Glassmorphism design with rich animations  
✅ **Task Management** - Complete CRUD operations with drag & drop  
✅ **Team Collaboration** - User assignment and team performance tracking  
✅ **Data Visualization** - Interactive charts and real-time statistics  
✅ **Smart Notifications** - Automated alerts and progress tracking  
✅ **Export Capabilities** - CSV exports for data analysis  
✅ **Responsive Design** - Works perfectly on all devices  
✅ **Production Quality** - Error handling, loading states, type safety  

## 🔮 Future Enhancements

- Real-time collaboration with WebSockets
- Advanced filtering and search capabilities
- File attachments for tasks
- Time tracking functionality
- Integration with external calendar systems
- Advanced reporting with charts
- Multi-project support
- Dark/light theme toggle

## 👥 Team & Contributions

This project demonstrates modern full-stack development practices with:
- Clean architecture and separation of concerns
- Comprehensive error handling and validation
- Beautiful UI/UX with accessibility in mind
- Performance optimization and responsive design
- Modern development tools and best practices

---

**Built with ❤️ using React, Spring Boot, and modern web technologies.** 