# 🔧 BonsaiMart Admin Panel

Admin application for managing the **BonsaiMart** e-commerce store. Built with Java, Firebase, and Material Design 3.

---

## 📱 Features

### 🔐 Authentication
- **Admin Login/Signup** with Firebase Auth
- **Secret Admin Code** verification (`BONSAI2026`)
- **Role-based Access Control** — Only admins can login

### 📊 Dashboard
- **Live Stats** — Products, Categories, Users, Sold Items
- **Total Sales Counter** — Lifetime revenue
- **Bar Chart** — Monthly sales overview (12 months)
- **Pie Chart** — Category distribution
- **Recent Activity** — Latest actions

### 📦 Orders Management
- **View All Orders** — Sorted by date (newest first)
- **Search** — By Order ID, Customer Name, Phone
- **Filter Chips** — All / Pending / Paid / Shipped / Delivered / Cancelled
- **Status Update** — Change order status with one click
- **Auto Notification** — User gets notified on status change
- **Color-Coded Status** — Easy visual identification

### 🏷️ Category Management
- **View Categories** — Grid/List with images
- **Add Category** — Name + Image (Firebase Storage)
- **Edit Category** — Update name/image
- **Delete Category** — With product count validation
- **Auto-Generated** `categoryId` — For product mapping

### 🌱 Product CRUD
- **Add Product** — 3 images max + Category + Price + Stock
- **View Products** — With search + List layout
- **Update Product** — Pre-filled form for edits
- **Delete Product** — With confirmation
- **Firebase Storage** — Image upload
- **Product Attributes** — Size, Color support

### 👥 Users Management
- **View All Users** — Name, Email, Profile Picture
- **Search** — By name or email
- **Delete User** — With confirmation dialog
- **Live Sync** — Real-time from Firestore

### 🔔 Send Notifications
- **Send to All Users** — Title + Message
- **Recent Notifications** — Last 10 shown
- **Empty State** — When no notifications
- **Timestamp** — Relative time display (e.g., "2h ago")

### 📄 Export Reports
- **Export Orders** — CSV with full details
- **Export Products** — CSV with all fields
- **Share** — Via WhatsApp, Email, or other apps
- **FileProvider** — Secure file sharing

### 👤 Admin Profile
- **View Profile** — Name, Email, Avatar (first letter)
- **Update Name** — Firestore sync
- **Update Email** — With re-authentication
- **Update Password** — With old password verification
- **Logout** — With confirmation dialog

---

## 🛠️ Tech Stack

| Technology | Purpose |
|---|---|
| **Java** | Primary language |
| **Firebase Firestore** | Real-time cloud database |
| **Firebase Auth** | Admin authentication |
| **Firebase Storage** | Image uploads |
| **Glide** | Image loading with disk cache |
| **Material Design 3** | Modern UI components |
| **Custom View** | Pie chart implementation |
| **Lombok** | Boilerplate reduction |
| **View Binding** | Type-safe view access |

---

## 🚀 Setup Instructions

### Prerequisites
- Android Studio (latest)
- JDK 11 or higher
- Firebase account
- Basic knowledge of Android & Firebase

### Step-by-Step Setup

#### 1. Clone the Repository
```bash
git clone https://github.com/Piushan452/BonsaiMart-Admin.git
cd BonsaiMart-Admin
