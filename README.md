# 🚗 Parking System API

## Introduction

The Parking System API allows users to register, authenticate, and manage parking sessions. 
The API provides endpoints for booking, updating, canceling bookings, managing parking sessions,
and retrieving information about buildings, available parking spots, and bookings.

## 💡 Features Overview

### User Management
- **User Registration**: Allows users to register with their email and password.
- **User Authentication**: Users can authenticate themselves to get a JWT token for further interactions with the system.
- **Token-based Authorization**: All API requests requiring user authentication are secured using JWT tokens, which must be included in the `Authorization` header.

### Booking Management
- **Book Parking Spot**: Allows users to reserve a parking spot at a specific building for a defined time range.
- **View Bookings**: Users can fetch all their active and past bookings.
- **Update Bookings**: Allows users to modify an existing booking by updating the start time, end time, or other details.
- **Cancel Booking**: Users can cancel a booking when it's no longer needed.

### Parking Session Management
- **Start Parking Session**: Users can start a parking session when they park their vehicle in a reserved or not reserved spot.
- **End Parking Session**: Ends the parking session when the vehicle is released, ensuring accurate parking duration records.

### Building and Spot Information
- **Retrieve Building Information**: Fetch details about different buildings, including which ones are available for parking, and their respective time slots.
- **Available Time Slots**: Check available time slots for parking at specific buildings.
- **Parking Spot Availability**: Retrieve the availability of parking spots at a building and book them accordingly.

### Secure Authentication and Authorization
- **JWT Authentication**: Secure access to the API with JSON Web Tokens (JWT) to ensure that only authorized users can access sensitive endpoints.
- **API Key for Parking Session**: Special API Key used for parking session operations from **GATE SYSTEM** to ensure authorized execution of parking session related endpoints.

### Real-time Booking & Parking Data
- **Real-time Spot Availability**: The API checks the availability of parking spots in real time, so users can make bookings based on the current status of spots.
- **Booking Confirmation & Details**: After a booking is made, users can fetch booking details such as the building, time range, and status.

### Data Storage & Management
- **Database-backed**: All data related to users, bookings, parking spots, and buildings is stored in a PostgreSQL database.
- **Session Tracking**: Tracks all parking sessions (start and end times), ensuring the accurate computation of parking durations and billing.

---

## 🔧 Setup
### Requirements:
- Java 21+
- Gradle
- PostgreSQL
- Docker

## 🚀 Running without Docker
- Prepare DB and adjust parameters in *application.yml* for connection
- ```./gradlew clean build```
- ```java -jar build build/libs/app.jar```

## 🐳 Running with Docker
- ```docker compose up -d```