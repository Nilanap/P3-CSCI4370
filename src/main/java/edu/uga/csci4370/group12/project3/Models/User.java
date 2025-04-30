
package edu.uga.csci4370.group12.project3.Models;

public class User {
    private final String userId;
    private final String username;
    private final String firstName;
    private final String lastName;

    public User(String userId, String username, String firstName, String lastName) {
        this.userId = userId;
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public User(String userId, String firstName, String lastName) {
        this(userId, null, firstName, lastName);
    }

    public String getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
}