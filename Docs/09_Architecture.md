# Architecture

## Architecture Overview

DiveApp follows a Clean Architecture inspired MVVM structure.

Architecture Layers

Presentation
↓
Application
↓
Domain
↓
Infrastructure
↓
Persistence

---

# Frontend Architecture

SwiftUI

↓

View

↓

ViewModel

↓

Repository

↓

Service

↓

API Client

↓

Backend API

---

## Presentation Layer

Responsible for UI.

Contains

- Views
- Components
- Navigation
- Theme

Never contains business logic.

---

## Application Layer

Responsible for application logic.

Contains

- ViewModels
- State
- Use Cases

---

## Domain Layer

Responsible for business rules.

Contains

- Models
- Entities
- Business Rules

Independent from UI.

---

## Infrastructure Layer

Responsible for external services.

Contains

- API
- Authentication
- Storage
- Location
- Image Upload

---

## Persistence Layer

Responsible for data storage.

Contains

- PostgreSQL
- Local Cache (Future)

---

# Backend Architecture

FastAPI

↓

Router

↓

Service

↓

Repository

↓

Database

---

Router

Only receives requests.

---

Service

Contains business logic.

---

Repository

Responsible for database access.

---

Database

PostgreSQL

---

# Project Structure

Frontend

App

Components

Features

Shared

Resources

Services

Repositories

Models

Utilities

Extensions

Backend

app

routers

services

repositories

models

schemas

core

middlewares

utils

database

---

# Feature Module Structure

Feature

View

ViewModel

Repository

Service

Model

Components

---

# State Management

MVVM State

Observable

Published

Environment

Dependency Injection

---

# Networking

REST API

↓

API Client

↓

Repository

↓

ViewModel

↓

View

---

# Authentication Flow

OAuth

↓

JWT

↓

Access Token

↓

Refresh Token

↓

Authenticated Request

---

# Image Flow

Image Picker

↓

Compression

↓

Upload

↓

Storage

↓

URL

↓

Database

---

# Error Handling

UI Error

↓

ViewModel

↓

Service

↓

Repository

↓

API

---

# Dependency Rules

Presentation

↓

Application

↓

Domain

↓

Infrastructure

Never reverse the dependency.

---

# Logging

Application Log

API Log

Error Log

Audit Log

---

# Security

HTTPS

JWT

Environment Variables

Role Based Authorization

Input Validation

---

# Future Expansion

Apple Watch

Apple Health

AI Features

Offline Cache

Push Notification

Multiple Languages

Tablet Support

---

# Architecture Principles

Maintainability First

Readability First

Consistency First

Scalability First

Simplicity First
