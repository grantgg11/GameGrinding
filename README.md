# GameGrinding  
**A Video Game Collection Organizer**  

## Background  
GameGrinding is a desktop application developed to address a common challenge faced by avid gamers: keeping track of video game collections spread across multiple platforms, consoles, and digital storefronts. With the ever-growing number of gaming systems, accounts, and media formats, it can be difficult for players to maintain a complete and organized record of their libraries.  

GameGrinding provides a centralized, user-friendly solution that enables users to manage their collections through both manual entry and automated data retrieval from the MobyGames API.  

This project was developed as part of my Master’s in Software Engineering Capstone at Grand Canyon University, serving as a culmination of advanced coursework in software architecture, secure software development, database management, and full-stack application design. The application’s architecture was built with scalability, maintainability, and user experience in mind, incorporating established Software Development Life Cycle (SDLC) best practices from requirements analysis to final delivery.  

GameGrinding features a responsive JavaFX-based interface paired with an SQLite relational database for offline-capable data storage. Security was a priority throughout development, with AES encryption for sensitive data, password hashing, role-based access control, and HTTPS/TLS for secure online communication with the MobyGames API.  

---

## Approach to Implementation  
GameGrinding was developed following SDLC best practices, progressing through requirements gathering, design, implementation, testing, and final delivery.  

**Key implementation details include:**  
- **Language & Frameworks:** Java 17, JavaFX 17, FXML, Scene Builder  
- **Database:** SQLite 3.x with indexed relational tables for optimized query performance  
- **API Integration:** MobyGames API for automatic retrieval of game details (title, genre, release date, platform, developer, publisher, cover image)  
- **Architecture:** Layered MVC structure with clear separation of UI, service, and database layers  
- **Security:** AES encryption for sensitive data, password hashing, role-based access control for admin features, and HTTPS/TLS encryption for secure online communication with the MobyGames API  
- **Performance Tracking:** SLF4J with Logback for logging API requests, system performance, and database integrity  
- **Testing:** Comprehensive JUnit 5 and Mockito unit tests, plus TestFX-based system and UI testing for functional and non-functional requirements  

The application was packaged with all dependencies using **jpackage** to ensure portability and ease of deployment.  

---

## How to Run GameGrinding  

### Requirements  
- **Operating System:** Windows 10 or later (packaged executable)  
- **Java Runtime:** Not required if using the packaged executable; Java 17 required for source build  
- **Internet Connection:** Required for API-based game searches  

### Option 1 – Run Packaged Executable (Recommended)  
1. Download the latest release from the **[Releases](../../releases)** section on GitHub  
2. Extract the `.zip` file to your desired location  
3. Inside the extracted folder, double-click `GameGrinding.exe` to start  
4. On first launch, create a user account  
5. Start adding games manually or via the MobyGames API search  

### Option 2 – Run from Source  
1. Clone the repository:  
   ```bash
   git clone https://github.com/<your-username>/GameGrinding.git
2. Open the project in **Eclipse** or **IntelliJ IDEA**  
3. Ensure **Java 17** and **JavaFX SDK 17** are installed and configured  
4. Add the following VM arguments to your run configuration:  
   ```bash
   --module-path <path-to-javafx-sdk>/lib --add-modules javafx.controls,javafx.fxml

## Key Features  
- Add games via MobyGames API or manual entry  
- Sort and filter collection by platform, genre, and completion status  
- Secure login system with encryption and password hashing  
- View game details including cover art, release date, developer, and publisher  
- Admin-only reporting for API requests, performance, and database integrity  
- Offline mode with local SQLite storage  

---

## Planned Future Enhancements  
- User statistics dashboard  
- Price tracking and historical price trends  
- Accessibility improvements (colorblind mode, larger text options)  

---

## Capstone Final Presentation & Demonstration  
**[Watch the Presentation](https://www.loom.com/share/395ded474f9a446dbfdc46a899ef7a40?sid=421115a8-304d-4603-a4f6-1b2dd80526d2)**
