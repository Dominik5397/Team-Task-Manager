# Team Task Manager - Modern Task Management Application

![Team Task Manager](https://img.shields.io/badge/Status-Production%20Ready-green)
![Frontend](https://img.shields.io/badge/Frontend-React%20TypeScript-blue)
![Backend](https://img.shields.io/badge/Backend-Spring%20Boot-brightgreen)
![Database](https://img.shields.io/badge/Database-H2-orange)
![Architecture](https://img.shields.io/badge/Architecture-Service%20Layer-purple)

## 🚀 Overview

Team Task Manager is a modern, animated web application for team task management built with cutting-edge technologies. It features a beautiful glassmorphism design, rich animations, comprehensive project management capabilities, and a robust service layer architecture.

## ✨ Key Features

### 🎨 Modern Design
- **Glassmorphism UI** with backdrop-filter effects
- **Animated background** with floating particles
- **Responsive design** that works on all devices
- **Rich animations** using our proven dual-library strategy ([see ANIMATION_STRATEGY.md](ANIMATION_STRATEGY.md))
  - **Framer Motion** for all UI animations and interactions
  - **Lottie React** for complex vector animations and illustrations
- **Beautiful gradients** and custom CSS properties

### 📊 Task Management
- **Kanban Board** with drag & drop functionality
- **Task CRUD operations** (Create, Read, Update, Delete)
- **Priority system** (High, Medium, Low) with visual indicators
- **Status tracking** (To Do, In Progress, Done)
- **Due date management** with overdue detection
- **User assignment** with team member profiles
- **Automatic change logging** for all task operations
- **Advanced task filtering** by user, status, and priority

### 📈 Advanced Analytics
- **Interactive Dashboard** with real-time statistics
- **Circular progress charts** for completion rates
- **Priority and status distribution** with animated progress bars
- **Team performance tracking** with individual metrics
- **Project completion analytics**
- **User statistics** with completion rates and task breakdowns

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

### 🏗️ Service Layer Architecture
- **Clean separation** of concerns between controllers, services, and repositories
- **Business logic encapsulation** in dedicated service classes
- **Comprehensive validation** and error handling
- **Automatic change tracking** for audit trails
- **Extensive unit testing** with high coverage

## 🛠 Technology Stack

### Backend
- **Java 17** with Spring Boot 3.x
- **Service Layer Architecture** with clean separation of concerns
- **Spring Data JPA** for database operations
- **Jakarta Validation** for data validation
- **Type-safe enums** for status and priority
- **H2 Database** for development and testing
- **REST API** with JSON serialization
- **Gradle** for build management
- **JUnit 5 & Mockito** for comprehensive testing

### Frontend
- **React 18** with TypeScript
- **Framer Motion** for advanced animations
- **React Beautiful DnD** for drag & drop
- **Lottie React** for animated icons
- **Modern CSS** with custom properties and glassmorphism

## 🏗 Architecture

### Service Layer Architecture
```
Controller Layer (HTTP/REST)
    ↓
Service Layer (Business Logic)
    ↓
Repository Layer (Data Access)
    ↓
Database Layer (H2)
```

### Backend Structure
```
src/main/java/org/example/
├── entities/
│   ├── Task.java              # Task entity with validation
│   ├── User.java              # User entity with relationships
│   ├── TaskStatus.java        # Type-safe status enum
│   ├── TaskPriority.java      # Type-safe priority enum
│   └── UserStats.java         # User statistics model
├── services/
│   ├── TaskService.java       # Task business logic interface
│   ├── TaskServiceImpl.java   # Task service implementation
│   ├── UserService.java       # User business logic interface
│   └── UserServiceImpl.java   # User service implementation
├── controllers/
│   ├── TaskController.java    # Task REST endpoints
│   ├── UserController.java    # User REST endpoints
│   └── EnumController.java    # Enum values API
├── repositories/
│   ├── TaskRepository.java    # Task data access
│   └── UserRepository.java    # User data access
├── validation/
│   └── GlobalExceptionHandler.java # Global error handling
└── Main.java                  # Spring Boot application entry
```

### Frontend Structure
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

## 🚀 Getting Started

### Prerequisites
- Java 17 or higher
- Node.js 16+ and npm
- Git

### Installation

1. **Clone the repository**
```bash
git clone <repository-url>
cd Team-Task-Manager
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
- **Users**: Jan Kowalski, Anna Nowak, Piotr Wiśniewski
- **Tasks**: Various priority levels and statuses
- **Demo data** for testing all features

## 🎯 Core Functionalities

### 1. Task Management
- Create tasks with title, description, priority, due date
- Assign tasks to team members
- Drag & drop between status columns
- Real-time status updates
- **Automatic change logging** with timestamps and descriptions
- **Advanced filtering** by user, status, priority
- **Task assignment/unassignment** with audit trail

### 2. User Management
- **User CRUD operations** with validation
- **Email and username uniqueness** validation
- **User statistics** calculation (completion rates, task breakdowns)
- **Automatic task unassignment** when deleting users
- **User search** by email and username

### 3. Dashboard Analytics
- Project completion rate with circular charts
- Priority distribution visualization
- Team performance metrics
- Real-time statistics updates
- **Individual user statistics** with detailed breakdowns

### 4. Notification System
- Animated notification bell with badge counter
- Smart alerts for overdue tasks
- Due date reminders
- Progress achievement notifications
- Expandable notification center

### 5. Data Export
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

### Task Management
- `GET /api/tasks` - Get all tasks
- `POST /api/tasks` - Create new task (with validation)
- `PUT /api/tasks/{id}` - Update task (with change logging)
- `DELETE /api/tasks/{id}` - Delete task
- `GET /api/tasks/{id}` - Get task details
- `GET /api/tasks/user/{userId}` - Get tasks by user
- `GET /api/tasks/status/{status}` - Get tasks by status
- `GET /api/tasks/priority/{priority}` - Get tasks by priority
- `PUT /api/tasks/{taskId}/assign/{userId}` - Assign task to user
- `PUT /api/tasks/{taskId}/unassign` - Unassign task
- `PUT /api/tasks/{taskId}/status?status={status}` - Change task status
- `PUT /api/tasks/{taskId}/priority?priority={priority}` - Change task priority

### User Management
- `GET /api/users` - Get all users
- `POST /api/users` - Create new user (with validation)
- `PUT /api/users/{id}` - Update user (with uniqueness validation)
- `DELETE /api/users/{id}` - Delete user (with task cleanup)
- `GET /api/users/{id}` - Get user details
- `GET /api/users/{userId}/stats` - Get user statistics
- `GET /api/users/email/{email}` - Find user by email
- `GET /api/users/username/{username}` - Find user by username
- `GET /api/users/check-email/{email}` - Check email existence
- `GET /api/users/check-username/{username}` - Check username existence
- `GET /api/users/with-tasks` - Get users with assigned tasks
- `GET /api/users/without-tasks` - Get users without tasks
- `POST /api/users/seed` - Initialize sample data

### Enum Values
- `GET /api/enums/all` - Get all enum values with metadata
- `GET /api/enums/task-statuses` - Get task status values
- `GET /api/enums/task-priorities` - Get task priority values

## 🧪 Testing

### Unit Tests
- **TaskServiceTest** - Comprehensive service layer testing
- **UserServiceTest** - User business logic testing
- **ValidationTest** - Data validation testing
- **EnumTest** - Type-safe enum testing

### Test Coverage
- Service layer business logic
- Validation scenarios
- Error handling
- Change logging functionality
- Statistics calculation

### Running Tests
```bash
./gradlew test
```

## 📱 **Responsive Design**

The application is fully responsive with:
- **Mobile-first approach**
- **Flexible grid layouts**
- **Touch-friendly interactions**
- **GPU-optimized animations** for mobile ([see Performance Guide](ANIMATION_PERFORMANCE_GUIDE.md))
- **Adaptive component sizing**

## 📚 **Documentation**

- **[Animation Strategy](ANIMATION_STRATEGY.md)** - Comprehensive animation architecture and dual-library approach
- **[Performance Guide](ANIMATION_PERFORMANCE_GUIDE.md)** - 60fps optimization techniques and GPU acceleration
- **[Service Layer Guide](SERVICE_LAYER_GUIDE.md)** - Detailed architecture documentation
- **[Validation Guide](VALIDATION_GUIDE.md)** - Data validation and enum implementation
- **API Documentation** - Available through endpoint exploration

## 🎭 **Animation Showcase**

### GPU-Optimized Performance
- **60fps animations** using only `transform` and `opacity`
- **Zero layout thrashing** with hardware acceleration
- **Device-adaptive performance** based on capabilities
- **Real-time monitoring** tools for development

### Micro-interactions
- Button hover effects with scale and color transitions
- Card animations with lift and shadow effects
- Loading spinners with CSS animations
- Form validation with shake effects

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