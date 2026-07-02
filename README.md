# Teilr - Social Expense Splitting Application

Teilr is a modern web application designed to eliminate the awkwardness and frustration of splitting expenses among friends, roommates, and travel groups.

<p align="center">
  <img src="https://i.imgur.com/n3InaqX.png" alt="image" />
</p>

## Why Teilr? (Market Necessity)

Sharing finances is a frequent source of tension. Based on recent consumer surveys and research from [Starling Bank](https://www.starlingbank.com/news/more-than-half-of-holiday-arguments-stem-from-disagreements-about-money/), [Money Wellness](https://www.moneywellness.com/blog/money-disagreements-top-causes-of-arguments-between-friends-on-holiday), [Nationalwide](https://www.nationwide.co.uk/media/news/house-share-from-hell-brits-reveal-lack-of-sharing-leads-to-housemate-squabbles), [Barclays](https://home.barclays/news/press-releases/2020/11/are-you-a--flatmare---nightmare-flatmates-cost-brits-p434-millio/):
- **Shared Living Conflicts:** 2,001 people who had lived in shared accommodation found that 83% had disagreements with housemates, with cleaning the top complaint, followed by bills and money.
- **Travel Fallouts:** 51% of UK adults have fallen out with a friend on holiday, and 54% of those arguments were triggered by money disagreements.

**The Solution:** By providing real-time transparency, automated debt calculation, and an objective third-party system, Teilr removes the "mental load" of tracking who paid for what and actively helps preserve relationships.

## Technology Stack

Teilr is built on a robust, production-ready Java ecosystem:
- **Backend Framework:** Java 21 & Spring Boot 3.5.x
- **Security:** Spring Security (Form-based authentication, BCrypt password hashing, email verification)
- **Database:** H2 (In-Memory, Dev) / MySQL / PostgreSQL via [Supabase](https://supabase.com) (Cloud, Production)
- **ORM / Persistence:** Spring Data JPA & Hibernate (PostgreSQL dialect in production)
- **Frontend / Views:** Thymeleaf & Vanilla CSS/JS
- **Email:** Gmail SMTP via Spring Mail (account `teilr.webapps@gmail.com`)
- **Tunneling:** [ngrok](https://ngrok.com) (exposes local server for public HTTPS access; sets `APP_BASE_URL` for email verification links)
- **Build Tool:** Maven Wrapper (`mvnw`)

## Environment Configuration (.env)

The project uses the following environment variables (example `.env`):

```dotenv
MAIL_USERNAME=teilr.webapps@gmail.com
MAIL_PASSWORD=your_gmail_app_password
APP_BASE_URL=https://<random>.ngrok-free.dev
DB_URL=jdbc:postgresql://db.<project>.supabase.co:5432/postgres?sslmode=require
DB_USERNAME=postgres
DB_PASSWORD=your_db_password
DB_DIALECT=org.hibernate.dialect.PostgreSQLDialect
```

These values are loaded at runtime to configure email sending, public URL (via ngrok), and database connection. Ensure you replace placeholder passwords with your actual credentials.

## Architecture


```mermaid
flowchart TD
    classDef frontend fill:#e1f5fe,stroke:#03a9f4,stroke-width:2px;
    classDef backend fill:#e8f5e9,stroke:#4caf50,stroke-width:2px;
    classDef database fill:#fce4ec,stroke:#e91e63,stroke-width:2px;
    classDef config fill:#fff3e0,stroke:#ff9800,stroke-width:2px;
    classDef external fill:#ede7f6,stroke:#673ab7,stroke-width:2px,stroke-dasharray:5 5;
    
    Client["Browser / Client"]:::frontend
    Ngrok["ngrok Tunnel <br/> (Public HTTPS → localhost:8080)"]:::external
    Gmail["Gmail SMTP"]:::external
    H2[("H2 In-Memory <br/> (Dev)")]:::database
    Supabase[("MySQL/Supabase PostgreSQL <br/> (Production)")]:::database
    
    subgraph App ["Spring Boot Application (localhost:8080)"]
        Config["Config & Security <br/> SecurityConfig · GlobalControllerAdvice · GlobalExceptionHandler"]:::config
        Controllers["Controllers <br/> Auth · User · Friendship · Group · Expense · View"]:::backend
        Views["Thymeleaf Templates <br/> layout · home · profile · settings"]:::frontend
        Services["Services <br/> User · Mail · Friendship · Group · GroupView · Expense"]:::backend
        Persistence["JPA Entities & Repositories <br/> User · Friendship · Group · GroupMember <br/> Bill · ExpenseSplit · Settlement"]:::backend

        Config -.->|"Secures & Advises"| Controllers
        Controllers -->|"Renders (server-side)"| Views
        
        Controllers <-->|"DTOs"| Services
        Services <--> Persistence
    end
    
    Client <-->|"HTTP (dev)"| Controllers
    Client <-->|"HTTPS (public)"| Ngrok
    Ngrok <-->|"Forwards to localhost:8080"| Controllers
    
    Services -->|"Verification email links (APP_BASE_URL)"| Ngrok
    Services -->|"SMTP"| Gmail
    Persistence <-->|"JDBC / Hibernate"| H2
    Persistence <-->|"JDBC / Hibernate + SSL"| Supabase
```

## Features

- **User Authentication:** Secure registration and login flows.
- **Social System:** Send and accept friendship requests (`Friendships`).
- **Group Management:** Create groups and manage members (`UserGroups`, `GroupMembers`).
- **Expense Tracking:** Create bills (`Bills`, `ExpenseSplits`) within groups, defining exactly who owes what (equally or specific amounts).
- **Settlements:** Keep track of who has paid whom and automatically calculate remaining balances (`Settlements`).

## How to Run Locally

### Prerequisites
- JDK 21 or higher installed on your machine.
- (Optional) MySQL Server if you wish to persist data.

### Quick Start (Development Mode)
The application uses an in-memory H2 database by default. You do not need to install a database to get started.

```bash
# Clone the repository and run via Maven Wrapper
./mvnw spring-boot:run
```
*(On Windows, use `mvnw.cmd spring-boot:run`)*

Once started, the app will be accessible at: **[http://localhost:8080](http://localhost:8080)**.
You can access the database console at `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:teilr`, Username: `sa`, Password: `[empty]`).

### Production Mode (MySQL)
To run the application with a persistent MySQL database, configure your credentials in `src/main/resources/application-mysql.properties` and run with the `mysql` profile:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=mysql
```
