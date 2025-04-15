# Kanban Board - Android App

A modern Kanban board application built with Kotlin that helps users manage their tasks efficiently using a visual board approach.

## Features

- **Authentication**
  - Firebase Authentication integration
  - Email/Password sign-up and login
  - Password reset functionality
  - Secure user data management

- **Task Management**
  - Kanban board with three columns (Todo, In Progress, Done)
  - Create, Read, Update, and Delete (CRUD) tasks
  - Task prioritization (Low, Medium, High)
  - Due date setting and tracking
  - Task status management
  - Drag and drop support for task movement

- **User Experience**
  - Material Design implementation
  - Dark mode support
  - Responsive layout
  - Smooth animations and transitions
  - Intuitive user interface

- **Notifications**
  - Push notifications for task due dates
  - Customizable notification settings
  - Background service for notification management

## Technical Stack

- **Language**: Kotlin
- **Architecture**: MVVM (Model-View-ViewModel)
- **Dependencies**:
  - Firebase (Authentication, Firestore, Cloud Messaging)
  - Android Architecture Components
  - Material Design Components
  - ViewPager2
  - Coroutines
  - LiveData
  - ViewModel

## Setup Instructions

1. Clone the repository
2. Create a new Firebase project
3. Add your `google-services.json` file to the app directory
4. Enable Email/Password authentication in Firebase Console
5. Set up Firestore database with appropriate security rules
6. Build and run the project

## Firebase Security Rules

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
    match /tasks/{taskId} {
      allow read, write: if request.auth != null && 
        request.auth.uid == resource.data.userId;
    }
  }
}
```

## Project Structure

```
app/
├── src/
│   ├── main/
│   │   ├── java/com/kanbanboard/
│   │   │   ├── activities/
│   │   │   ├── adapters/
│   │   │   ├── fragments/
│   │   │   ├── models/
│   │   │   ├── services/
│   │   │   ├── utils/
│   │   │   ├── viewmodels/
│   │   │   └── KanbanApplication.kt
│   │   ├── res/
│   │   └── AndroidManifest.xml
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments

- Material Design for Android
- Firebase Documentation
- Android Developer Documentation
