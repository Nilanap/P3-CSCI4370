package edu.uga.csci4370.group12.project3.Services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.uga.csci4370.group12.project3.Models.Class;
import edu.uga.csci4370.group12.project3.Models.User;

@Service
public class ClassService {

    private static final Logger LOGGER = Logger.getLogger(ClassService.class.getName());

    private final DataSource dataSource;

    @Autowired
    public ClassService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Class findOrCreateClass(String classCode) throws SQLException {
        if (!classCode.matches("^[A-Z]{4}\\d{4}$")) {
            throw new IllegalArgumentException("Class code must be 4 letters followed by 4 digits (e.g., CSCI4370)");
        }

        String selectSql = "SELECT classId, classCode, className FROM class WHERE classCode = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(selectSql)) {
            pstmt.setString(1, classCode);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Class(
                    rs.getString("classId"),
                    rs.getString("classCode"),
                    rs.getString("className")
                );
            }
        }

        String className = classCode.substring(0, 4) + " " + classCode.substring(4);
        String insertSql = "INSERT INTO class (classCode, className) VALUES (?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(insertSql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, classCode);
            pstmt.setString(2, className);
            pstmt.executeUpdate();
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                return new Class(
                    String.valueOf(rs.getInt(1)),
                    classCode,
                    className
                );
            }
        }
        throw new SQLException("Failed to create class");
    }

    public boolean enrollUserInClass(String userId, String classId) {
        String sql = "INSERT INTO enrollment (userId, classId, enrollmentDate) VALUES (?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, Integer.parseInt(userId));
            pstmt.setInt(2, Integer.parseInt(classId));
            pstmt.setString(3, LocalDateTime.now().toString());
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            if (e.getSQLState().equals("23000")) {
                return false;
            }
            e.printStackTrace();
            return false;
        }
    }

    public List<Class> getUserClasses(String userId) {
        List<Class> classes = new ArrayList<>();
        String sql = "SELECT c.classId, c.classCode, c.className " +
                    "FROM class c " +
                    "JOIN enrollment e ON c.classId = e.classId " +
                    "WHERE e.userId = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, Integer.parseInt(userId));
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                classes.add(new Class(
                    rs.getString("classId"),
                    rs.getString("classCode"),
                    rs.getString("className")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return classes;
    }

    public List<User> getUsersInClass(String classCode) {
        List<User> users = new ArrayList<>();
        String sql = "SELECT u.userId, u.username, u.firstName, u.lastName " +
                    "FROM user u " +
                    "JOIN enrollment e ON u.userId = e.userId " +
                    "JOIN class c ON e.classId = c.classId " +
                    "WHERE c.classCode = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, classCode);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String userId = rs.getString("userId");
                String username = rs.getString("username");
                String firstName = rs.getString("firstName");
                String lastName = rs.getString("lastName");
                LOGGER.info("User in class " + classCode + ": userId=" + userId + ", username=" + username + ", firstName=" + firstName + ", lastName=" + lastName);
                users.add(new User(
                    userId,
                    username != null ? username : "",
                    firstName != null ? firstName : "",
                    lastName != null ? lastName : ""
                ));
            }
        } catch (SQLException e) {
            LOGGER.severe("Error fetching users in class " + classCode + ": " + e.getMessage());
            e.printStackTrace();
        }
        return users;
    }

    public User getUserById(String userId) {
        String sql = "SELECT userId, username, firstName, lastName FROM user WHERE userId = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, Integer.parseInt(userId));
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new User(
                    rs.getString("userId"),
                    rs.getString("username") != null ? rs.getString("username") : "",
                    rs.getString("firstName") != null ? rs.getString("firstName") : "",
                    rs.getString("lastName") != null ? rs.getString("lastName") : ""
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean isFollowing(String followerId, String followeeId) {
        String sql = "SELECT COUNT(*) FROM follow WHERE followerId = ? AND followeeId = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, Integer.parseInt(followerId));
            pstmt.setInt(2, Integer.parseInt(followeeId));
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean followUser(String followerId, String followeeId) {
        String sql = "INSERT INTO follow (followerId, followeeId, followDate) VALUES (?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, Integer.parseInt(followerId));
            pstmt.setInt(2, Integer.parseInt(followeeId));
            pstmt.setString(3, LocalDateTime.now().toString());
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            if (e.getSQLState().equals("23000")) {
                return false;
            }
            e.printStackTrace();
            return false;
        }
    }

    public boolean unfollowUser(String followerId, String followeeId) {
        String sql = "DELETE FROM follow WHERE followerId = ? AND followeeId = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, Integer.parseInt(followerId));
            pstmt.setInt(2, Integer.parseInt(followeeId));
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<User> getFollowees(String userId) {
        List<User> followees = new ArrayList<>();
        String sql = "SELECT u.userId, u.username, u.firstName, u.lastName " +
                    "FROM user u " +
                    "JOIN follow f ON u.userId = f.followeeId " +
                    "WHERE f.followerId = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, Integer.parseInt(userId));
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                followees.add(new User(
                    rs.getString("userId"),
                    rs.getString("username") != null ? rs.getString("username") : "",
                    rs.getString("firstName") != null ? rs.getString("firstName") : "",
                    rs.getString("lastName") != null ? rs.getString("lastName") : ""
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return followees;
    }

    public List<User> getFollowers(String userId) {
        List<User> followers = new ArrayList<>();
        String sql = "SELECT u.userId, u.username, u.firstName, u.lastName " +
                    "FROM user u " +
                    "JOIN follow f ON u.userId = f.followerId " +
                    "WHERE f.followeeId = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, Integer.parseInt(userId));
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                followers.add(new User(
                    rs.getString("userId"),
                    rs.getString("username") != null ? rs.getString("username") : "",
                    rs.getString("firstName") != null ? rs.getString("firstName") : "",
                    rs.getString("lastName") != null ? rs.getString("lastName") : ""
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return followers;
    }

    public boolean saveAvailability(String userId, LocalDate studyDate, LocalTime startTime, LocalTime endTime) {
        String sql = "INSERT INTO availability (userId, studyDate, startTime, endTime) VALUES (?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, Integer.parseInt(userId));
            pstmt.setDate(2, java.sql.Date.valueOf(studyDate));
            pstmt.setTime(3, java.sql.Time.valueOf(startTime));
            pstmt.setTime(4, java.sql.Time.valueOf(endTime));
            int rowsAffected = pstmt.executeUpdate();
            LOGGER.info("Saved availability for user " + userId + ": " + studyDate + " " + startTime + "-" + endTime);
            return rowsAffected > 0;
        } catch (SQLException e) {
            if (e.getSQLState().equals("23000")) {
                LOGGER.warning("Duplicate availability slot for user " + userId);
                return false;
            }
            LOGGER.severe("Error saving availability for user " + userId + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public List<Availability> getAvailability(String userId) {
        List<Availability> availabilityList = new ArrayList<>();
        String sql = "SELECT availabilityId, userId, studyDate, startTime, endTime FROM availability WHERE userId = ? ORDER BY studyDate, startTime";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, Integer.parseInt(userId));
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Availability avail = new Availability(
                    rs.getInt("availabilityId"),
                    rs.getString("userId"),
                    rs.getDate("studyDate").toLocalDate(),
                    rs.getTime("startTime").toLocalTime(),
                    rs.getTime("endTime").toLocalTime()
                );
                availabilityList.add(avail);
                LOGGER.info("Fetched availability for user " + userId + ": " + avail.getStudyDate() + " " + avail.getStartTime() + "-" + avail.getEndTime());
            }
        } catch (SQLException e) {
            LOGGER.severe("Error fetching availability for user " + userId + ": " + e.getMessage());
            e.printStackTrace();
        }
        return availabilityList;
    }

    public boolean deleteAvailability(String availabilityId) {
        String sql = "DELETE FROM availability WHERE availabilityId = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, Integer.parseInt(availabilityId));
            int rowsAffected = pstmt.executeUpdate();
            LOGGER.info("Deleted availability " + availabilityId);
            return rowsAffected > 0;
        } catch (SQLException e) {
            LOGGER.severe("Error deleting availability " + availabilityId + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public List<UserAvailability> getFolloweesAvailability(String userId) {
        List<UserAvailability> followeesAvailability = new ArrayList<>();
        String sql = "SELECT u.userId, u.firstName, u.lastName, a.availabilityId, a.studyDate, a.startTime, a.endTime " +
                    "FROM user u " +
                    "JOIN follow f ON u.userId = f.followeeId " +
                    "JOIN availability a ON u.userId = a.userId " +
                    "WHERE f.followerId = ? " +
                    "ORDER BY u.userId, a.studyDate, a.startTime";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, Integer.parseInt(userId));
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                User user = new User(
                    rs.getString("userId"),
                    "",
                    rs.getString("firstName"),
                    rs.getString("lastName")
                );
                Availability availability = new Availability(
                    rs.getInt("availabilityId"),
                    rs.getString("userId"),
                    rs.getDate("studyDate").toLocalDate(),
                    rs.getTime("startTime").toLocalTime(),
                    rs.getTime("endTime").toLocalTime()
                );
                List<Class> sharedClasses = getSharedClasses(userId, user.getUserId());
                followeesAvailability.add(new UserAvailability(user, availability, sharedClasses));
            }
        } catch (SQLException e) {
            LOGGER.severe("Error fetching followees availability for user " + userId + ": " + e.getMessage());
            e.printStackTrace();
        }
        return followeesAvailability;
    }

    public boolean createStudyRequest(String requesterId, String targetUserId, String availabilityId) {
        String sql = "INSERT INTO study_request (requesterId, targetUserId, availabilityId, status, requestDate) VALUES (?, ?, ?, 'PENDING', ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, Integer.parseInt(requesterId));
            pstmt.setInt(2, Integer.parseInt(targetUserId));
            pstmt.setInt(3, Integer.parseInt(availabilityId));
            pstmt.setTimestamp(4, java.sql.Timestamp.valueOf(LocalDateTime.now()));
            int rowsAffected = pstmt.executeUpdate();
            LOGGER.info("Created study request from user " + requesterId + " to " + targetUserId + " for availability " + availabilityId);
            return rowsAffected > 0;
        } catch (SQLException e) {
            if (e.getSQLState().equals("23000")) {
                LOGGER.warning("Duplicate study request for requester " + requesterId + ", availability " + availabilityId);
                return false;
            }
            LOGGER.severe("Error creating study request: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public List<StudyRequest> getPendingStudyRequests(String userId) {
        List<StudyRequest> requests = new ArrayList<>();
        String sql = "SELECT sr.requestId, sr.requesterId, sr.targetUserId, sr.availabilityId, sr.status, sr.requestDate, " +
                    "u.firstName, u.lastName, a.studyDate, a.startTime, a.endTime " +
                    "FROM study_request sr " +
                    "JOIN user u ON sr.requesterId = u.userId " +
                    "JOIN availability a ON sr.availabilityId = a.availabilityId " +
                    "WHERE sr.targetUserId = ? AND sr.status = 'PENDING' " +
                    "ORDER BY sr.requestDate DESC";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, Integer.parseInt(userId));
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                requests.add(new StudyRequest(
                    rs.getInt("requestId"),
                    new User(
                        rs.getString("requesterId"),
                        "",
                        rs.getString("firstName"),
                        rs.getString("lastName")
                    ),
                    rs.getString("targetUserId"),
                    rs.getInt("availabilityId"),
                    rs.getString("status"),
                    rs.getTimestamp("requestDate").toLocalDateTime(),
                    rs.getDate("studyDate").toLocalDate(),
                    rs.getTime("startTime").toLocalTime(),
                    rs.getTime("endTime").toLocalTime()
                ));
            }
        } catch (SQLException e) {
            LOGGER.severe("Error fetching pending study requests for user " + userId + ": " + e.getMessage());
            e.printStackTrace();
        }
        return requests;
    }

    public boolean updateStudyRequestStatus(String requestId, String status) {
        String sql = "UPDATE study_request SET status = ? WHERE requestId = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status);
            pstmt.setInt(2, Integer.parseInt(requestId));
            int rowsAffected = pstmt.executeUpdate();
            LOGGER.info("Updated study request " + requestId + " to status " + status);
            return rowsAffected > 0;
        } catch (SQLException e) {
            LOGGER.severe("Error updating study request " + requestId + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public List<Class> getSharedClasses(String userId, String followeeId) {
        List<Class> sharedClasses = new ArrayList<>();
        String sql = "SELECT c.classId, c.classCode, c.className " +
                    "FROM class c " +
                    "JOIN enrollment e1 ON c.classId = e1.classId " +
                    "JOIN enrollment e2 ON c.classId = e2.classId " +
                    "WHERE e1.userId = ? AND e2.userId = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, Integer.parseInt(userId));
            pstmt.setInt(2, Integer.parseInt(followeeId));
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                sharedClasses.add(new Class(
                    rs.getString("classId"),
                    rs.getString("classCode"),
                    rs.getString("className")
                ));
            }
            LOGGER.info("Fetched " + sharedClasses.size() + " shared classes for user " + userId + " and followee " + followeeId);
        } catch (SQLException e) {
            LOGGER.severe("Error fetching shared classes for user " + userId + " and followee " + followeeId + ": " + e.getMessage());
            e.printStackTrace();
        }
        return sharedClasses;
    }

    public List<StudySession> getUpcomingStudySessions(String userId) {
        List<StudySession> sessions = new ArrayList<>();
        String sql = "SELECT sr.requestId, sr.requesterId, sr.targetUserId, sr.availabilityId, sr.status, sr.requestDate, " +
                    "u1.firstName AS requesterFirstName, u1.lastName AS requesterLastName, " +
                    "u2.firstName AS targetFirstName, u2.lastName AS targetLastName, " +
                    "a.studyDate, a.startTime, a.endTime " +
                    "FROM study_request sr " +
                    "JOIN user u1 ON sr.requesterId = u1.userId " +
                    "JOIN user u2 ON sr.targetUserId = u2.userId " +
                    "JOIN availability a ON sr.availabilityId = a.availabilityId " +
                    "WHERE (sr.requesterId = ? OR sr.targetUserId = ?) AND sr.status = 'APPROVED' AND a.studyDate >= CURRENT_DATE " +
                    "ORDER BY a.studyDate, a.startTime";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, Integer.parseInt(userId));
            pstmt.setInt(2, Integer.parseInt(userId));
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String requesterId = rs.getString("requesterId");
                String targetUserId = rs.getString("targetUserId");
                // Determine partner: the other user (not the logged-in user)
                User partner = userId.equals(requesterId) ?
                    new User(targetUserId, "", rs.getString("targetFirstName"), rs.getString("targetLastName")) :
                    new User(requesterId, "", rs.getString("requesterFirstName"), rs.getString("requesterLastName"));
                sessions.add(new StudySession(
                    rs.getInt("requestId"),
                    partner,
                    rs.getInt("availabilityId"),
                    rs.getString("status"),
                    rs.getTimestamp("requestDate").toLocalDateTime(),
                    rs.getDate("studyDate").toLocalDate(),
                    rs.getTime("startTime").toLocalTime(),
                    rs.getTime("endTime").toLocalTime()
                ));
            }
            LOGGER.info("Fetched " + sessions.size() + " upcoming study sessions for user " + userId);
        } catch (SQLException e) {
            LOGGER.severe("Error fetching upcoming study sessions for user " + userId + ": " + e.getMessage());
            e.printStackTrace();
        }
        return sessions;
    }

    public static class Availability {
        private final int availabilityId;
        private final String userId;
        private final LocalDate studyDate;
        private final LocalTime startTime;
        private final LocalTime endTime;

        public Availability(int availabilityId, String userId, LocalDate studyDate, LocalTime startTime, LocalTime endTime) {
            this.availabilityId = availabilityId;
            this.userId = userId;
            this.studyDate = studyDate;
            this.startTime = startTime;
            this.endTime = endTime;
        }

        public int getAvailabilityId() { return availabilityId; }
        public String getUserId() { return userId; }
        public LocalDate getStudyDate() { return studyDate; }
        public LocalTime getStartTime() { return startTime; }
        public LocalTime getEndTime() { return endTime; }
    }

    public static class UserAvailability {
        private final User user;
        private final Availability availability;
        private final List<Class> sharedClasses;

        public UserAvailability(User user, Availability availability, List<Class> sharedClasses) {
            this.user = user;
            this.availability = availability;
            this.sharedClasses = sharedClasses;
        }

        public User getUser() { return user; }
        public Availability getAvailability() { return availability; }
        public List<Class> getSharedClasses() { return sharedClasses; }
    }

    public static class StudyRequest {
        private final int requestId;
        private final User requester;
        private final String targetUserId;
        private final int availabilityId;
        private final String status;
        private final LocalDateTime requestDate;
        private final LocalDate studyDate;
        private final LocalTime startTime;
        private final LocalTime endTime;

        public StudyRequest(int requestId, User requester, String targetUserId, int availabilityId, String status,
                           LocalDateTime requestDate, LocalDate studyDate, LocalTime startTime, LocalTime endTime) {
            this.requestId = requestId;
            this.requester = requester;
            this.targetUserId = targetUserId;
            this.availabilityId = availabilityId;
            this.status = status;
            this.requestDate = requestDate;
            this.studyDate = studyDate;
            this.startTime = startTime;
            this.endTime = endTime;
        }

        public int getRequestId() { return requestId; }
        public User getRequester() { return requester; }
        public String getTargetUserId() { return targetUserId; }
        public int getAvailabilityId() { return availabilityId; }
        public String getStatus() { return status; }
        public LocalDateTime getRequestDate() { return requestDate; }
        public LocalDate getStudyDate() { return studyDate; }
        public LocalTime getStartTime() { return startTime; }
        public LocalTime getEndTime() { return endTime; }
    }

    public static class StudySession {
        private final int requestId;
        private final User partner;
        private final int availabilityId;
        private final String status;
        private final LocalDateTime requestDate;
        private final LocalDate studyDate;
        private final LocalTime startTime;
        private final LocalTime endTime;

        public StudySession(int requestId, User partner, int availabilityId, String status,
                           LocalDateTime requestDate, LocalDate studyDate, LocalTime startTime, LocalTime endTime) {
            this.requestId = requestId;
            this.partner = partner;
            this.availabilityId = availabilityId;
            this.status = status;
            this.requestDate = requestDate;
            this.studyDate = studyDate;
            this.startTime = startTime;
            this.endTime = endTime;
        }

        public int getRequestId() { return requestId; }
        public User getPartner() { return partner; }
        public int getAvailabilityId() { return availabilityId; }
        public String getStatus() { return status; }
        public LocalDateTime getRequestDate() { return requestDate; }
        public LocalDate getStudyDate() { return studyDate; }
        public LocalTime getStartTime() { return startTime; }
        public LocalTime getEndTime() { return endTime; }
    }
}