# spring-security-practice

# 🔐 Secure Task App

A production-grade **REST API** built with **Spring Boot 3.2** demonstrating all major features of **Spring Security 6** — JWT authentication, Google OAuth2 login, BCrypt password hashing, and Role-Based Access Control (RBAC).

---

## 🧰 Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.2 |
| Security | Spring Security 6 |
| Auth | JWT (JJWT) + Google OAuth2 |
| ORM | Spring Data JPA + Hibernate |
| Database | PostgreSQL |
| Password Hashing | BCrypt |
| Build Tool | Maven |
| Monitoring | Spring Actuator |

---

## ✨ Features

- ✅ **User Registration** — Name + Email + Password → BCrypt hash stored → JWT returned
- ✅ **Username/Password Login** — Email + Password → BCrypt verify → JWT returned
- ✅ **Google OAuth2 Login** — Google authenticates user → saved to DB → JWT returned
- ✅ **JWT-Protected Endpoints** — `Authorization: Bearer <token>` on every request
- ✅ **Role-Based Access Control** — `ROLE_USER` for normal access, `ROLE_ADMIN` for admin endpoints
- ✅ **Stateless Session** — No server-side sessions; JWT carries all auth state
- ✅ **Health Endpoint** — `/actuator/health` for monitoring

---

## 📁 Project Structure

```
secure-task-app/
├── src/main/java/com/example/securetaskapp/
│   ├── SecureTaskAppApplication.java       # Entry point (@SpringBootApplication)
│   ├── config/
│   │   └── SecurityConfig.java             # Main security rules & filter chain
│   ├── controller/
│   │   ├── AuthController.java             # POST /register, POST /login, GET /me
│   │   └── TaskController.java             # GET /tasks (user), DELETE /tasks/admin (admin)
│   ├── dto/
│   │   └── AuthDTOs.java                   # RegisterRequest, LoginRequest, AuthResponse
│   ├── entity/
│   │   └── User.java                       # JPA Entity + implements UserDetails
│   ├── filter/
│   │   └── JwtAuthFilter.java              # Runs on every request, reads Bearer token
│   ├── handler/
│   │   └── OAuth2SuccessHandler.java       # Generates JWT after Google login
│   ├── repository/
│   │   └── UserRepository.java             # findByEmail(), existsByEmail()
│   └── service/
│       ├── AuthService.java                # register() and login() business logic
│       ├── CustomOAuth2UserService.java    # Handles Google user info, saves to DB
│       ├── JwtUtil.java                    # Generate / validate / extract JWT claims
│       └── UserDetailsServiceImpl.java     # Loads User from DB by email for Spring
└── src/main/resources/
    ├── application.yml                     # DB URL, JWT secret, Google OAuth2 config
    └── schema.sql                          # Creates users table on startup
```

---

## ⚙️ Prerequisites

- Java 17+
- Maven 3.8+
- PostgreSQL 13+
- Google Cloud Console account (for OAuth2)

---

## 🗄️ Database Setup

Create the PostgreSQL database before running the app:

```sql
CREATE DATABASE secure_task_db;
```

The `schema.sql` file runs automatically on startup and creates the `users` table.

---

## 🔑 Google OAuth2 Setup

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project → **APIs & Services** → **Credentials**
3. Create **OAuth 2.0 Client ID** (Web Application type)
4. Add Authorized Redirect URI:
   ```
   http://localhost:8080/login/oauth2/code/google
   ```
5. Copy the **Client ID** and **Client Secret**

---

## 🛠️ Configuration

Edit `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/secure_task_db
    username: your_db_username
    password: your_db_password

  security:
    oauth2:
      client:
        registration:
          google:
            client-id: YOUR_GOOGLE_CLIENT_ID
            client-secret: YOUR_GOOGLE_CLIENT_SECRET
            scope: openid, email, profile

app:
  jwt:
    secret: your-very-long-secret-key-minimum-32-characters
    expiration-ms: 86400000   # 24 hours
```

---

## 🚀 Run the Application

```bash
# Clone the repository
git clone https://github.com/your-username/secure-task-app.git
cd secure-task-app

# Build
mvn clean install

# Run
mvn spring-boot:run
```

App starts at: `http://localhost:8080`

---

## 📡 API Endpoints

### Auth Endpoints (Public)

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/auth/register` | Register new user |
| `POST` | `/api/auth/login` | Login with email + password |
| `GET` | `/oauth2/authorization/google` | Initiate Google OAuth2 login |
| `GET` | `/actuator/health` | Health check |

### Protected Endpoints (JWT Required)

| Method | Endpoint | Role Required | Description |
|--------|----------|---------------|-------------|
| `GET` | `/api/auth/me` | `ROLE_USER` | Get current logged-in user |
| `GET` | `/api/tasks` | `ROLE_USER` | Get all tasks |
| `DELETE` | `/api/tasks/admin` | `ROLE_ADMIN` | Delete all tasks (admin only) |

---

## 🧪 Testing with cURL / Postman

### Register

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Rahul Kumar",
    "email": "rahul@gmail.com",
    "password": "password123"
  }'
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "email": "rahul@gmail.com",
  "name": "Rahul Kumar",
  "role": "ROLE_USER"
}
```

---

### Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "rahul@gmail.com",
    "password": "password123"
  }'
```

---

### Access Protected Endpoint

```bash
curl -X GET http://localhost:8080/api/tasks \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

---

### Google OAuth2 Login

Open in browser:
```
http://localhost:8080/oauth2/authorization/google
```

On success, you will be redirected to:
```
http://localhost:3000/oauth2/callback?token=<JWT_TOKEN>
```

---

## 🔐 Authentication Flows

### Username + Password Flow

```
Client → POST /api/auth/login { email, password }
       → AuthService.login()
       → authenticationManager.authenticate()
       → UserDetailsServiceImpl.loadUserByUsername(email)
       → BCryptPasswordEncoder.matches(raw, hashed)
       → JwtUtil.generateToken(user)
       → Return { token, email, name, role }

Future Requests:
Client → Authorization: Bearer <token>
       → JwtAuthFilter reads token
       → JwtUtil.extractUsername(token)
       → Load User from DB, validate token
       → Set SecurityContextHolder
       → Controller executes
```

### Google OAuth2 Flow

```
Client → GET /oauth2/authorization/google
       → Spring redirects to Google consent screen
       → User logs in and approves
       → Google redirects to /login/oauth2/code/google?code=abc
       → Spring exchanges code for access token
       → CustomOAuth2UserService.loadUser() called
       → Save/update user in DB
       → OAuth2SuccessHandler generates JWT
       → Redirect to frontend with token
```

---

## 🛡️ Security Architecture

```
HTTP Request
    │
    ▼
JwtAuthFilter (OncePerRequestFilter)
    │  Reads Authorization header
    │  Validates JWT
    │  Sets SecurityContextHolder
    ▼
SecurityConfig (Filter Chain)
    │  Defines public vs protected routes
    │  Configures OAuth2 login
    │  Configures session = STATELESS
    ▼
Controller
    │  @PreAuthorize("hasRole('ADMIN')") for admin endpoints
    │  @AuthenticationPrincipal injects current User
    ▼
Service / Repository
```

---

## 👥 Roles

| Role | Access |
|------|--------|
| `ROLE_USER` | Register, Login, `/api/auth/me`, `/api/tasks` |
| `ROLE_ADMIN` | All of above + `/api/tasks/admin` (DELETE) |

New users registered via `/api/auth/register` get `ROLE_USER` by default.

---

## 🔍 JWT Token Inspection

Decode any JWT at [https://jwt.io](https://jwt.io). Example payload:

```json
{
  "sub": "rahul@gmail.com",
  "role": "ROLE_USER",
  "iat": 1711862400,
  "exp": 1711948800
}
```

> ⚠️ **Note:** JWT payload is Base64 encoded, NOT encrypted. Never store passwords or secrets inside a JWT.

---

## 📦 Key Dependencies

| Dependency | Purpose |
|------------|---------|
| `spring-boot-starter-security` | Core Spring Security filters & managers |
| `spring-boot-starter-oauth2-client` | Google OAuth2 login support |
| `spring-boot-starter-data-jpa` | JPA + Hibernate ORM |
| `spring-boot-starter-web` | REST controllers + Jackson JSON |
| `spring-boot-starter-validation` | `@NotBlank`, `@Email`, `@Size` input validation |
| `jjwt-api` + `jjwt-impl` + `jjwt-jackson` | JWT create / validate / parse |
| `postgresql` | JDBC driver |
| `lombok` | `@Data`, `@Builder`, `@RequiredArgsConstructor` |
| `spring-boot-starter-actuator` | `/actuator/health` monitoring endpoint |

---

## 🧑‍💻 Author

Built as a learning project covering **Spring Security 6** internals — JWT filter chain, OAuth2 integration, stateless session management, and role-based access control.

---

## 📄 License

This project was created by Abhijit Sagar for educational purposes.
