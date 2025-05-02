# Project 3 - Study Buddy

> For CSCI 4370 - Database Management.
> Created by Group 12 - Matthew Griffith, Nilan Patel, Michael Scott, and Bryce Wellman.

## Contributions

| Group Member     | Contributions                                                                                  |
|------------------|------------------------------------------------------------------------------------------------|
| Matthew Griffith | TODO                                                                                           |
| Nilan Patel      | TODO                                                                                           |
| Michael Scott    | Most metadata files (`queries.sql`, `prelim.pdf`, etc.), collecting course data in `data.sql`. |
| Bryce Wellman    | TODO                                                                                           |

## Building and Running

1. Open `src/main/resources/application.properties` and change `spring.datasource.url`, `spring.datasource.username`,
   and `spring.datasource.password` to the URL the database will be stored at, the username to log in with, and the
   password to log in with, respectively.
2. If it does not already exist, create the database at the URL you specified using the provided `ddl.sql` file.
3. Run the following command:

```
mvn spring-boot:run --% -Dspring-boot.run.arguments="--server.port=8081"
```

This will host the website on `http://localhost:8081/`.

## Database Information

Database Name: `study_buddy`

Database Username: `root`

Database Password: `mysqlpass`

## Demo Logins

| Username   | Password  |
|------------|-----------|
| TUser12001 | password1 |
| TUser22002 | password2 |
| TUser32003 | password3 |
| TUser42004 | password4 |

## Used Technologies

| Technology            | Usage Reason                                                                    |
|-----------------------|---------------------------------------------------------------------------------|
| Java                  | Primary backend programming language.                                           |
| HTML, CSS, JavaScript | Used for creating the Web frontend of the project.                              |
| Spring Boot           | Used as the basis of the website.                                               |
| Mustache              | Used to create templates of frontend HTML files to be populated by Spring Boot. |
| Docker                | Used to host the database used by the project.                                  |
| MySQL                 | Used as the DBMS for this project.                                              |
| JDBC                  | Used to communicate with the  MySQL database.                                   |