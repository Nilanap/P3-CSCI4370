# P3-CSCI4370
Study Buddy Project Plan and Database Design

## Building and Running

1. Open `src/main/resources/application.properties` and change `spring.datasource.url`, `spring.datasource.username`, and `spring.datasource.password` to the URL the database will be stored at, the username to log in with, and the password to log in with, respectively.
2. If it does not already exist, create the database at the URL you specified using the provided `database.sql` file.
3. Run the following command:

```
mvn spring-boot:run --% -Dspring-boot.run.arguments="--server.port=8081"
```

This will host the website on `http://localhost:8081/`.

----

Title for the Project

Study Buddy: A Platform for Connecting Classmates for Collaborative Study Sessions

| Describe the Problem and the Domain |

Problem: Students often struggle to find study partners for specific classes, especially before exams. Coordinating study sessions can be challenging without a centralized platform to match students based on shared classes and facilitate communication.
Domain: The application operates in the educational technology domain, targeting college or university students who need to collaborate with peers for exam preparation. It addresses the need for efficient matchmaking and communication within an academic context.

| Describe the Solution You Develop |

| Solution: Study Buddy is a web-based platform that allows students to: |
- Register and create a profile with their class schedule, including class codes and IDs.
- Search for other students enrolled in the same classes using class IDs.
- Request to connect with potential study partners, with an approval process to confirm the match.
- Communicate via a messaging system to coordinate study sessions.

| User Interfaces: |
- Registration/Login Page: Users sign up with a username, password, first name, last name, and optional profile details.
- Profile Page: Displays user information and allows users to input their class schedule (class code and ID).
- Class Search Page: Users enter a class ID to find other students enrolled in the same class.
- Study Request Page: Users can send study requests to others and view pending requests (sent or received).
- Messaging Interface: A chat-like interface for matched users to exchange messages and plan study sessions.
- Dashboard: Displays matched study partners, upcoming study sessions, and recent messages.


| The Technologies You Will Be Using |
- Java, MySQL, Docker, Springboot, HTML, CSS, JavaScript




