-- Finds a class with the given class code. Used as part of enrolling a student into a class.
-- URL: /profile
SELECT classId, classCode, className FROM class WHERE classCode = ?;

-- Enrolls a given user into a given class.
-- URL: /profile
INSERT INTO enrollment (userId, classId, enrollmentDate) VALUES (?, ?, ?);

-- Gets all classes a given user is enrolled in. Used when viewing a user's profile.
-- URL: /profile
SELECT c.classId, c.classCode, c.className FROM class c JOIN enrollment e ON c.classId = e.classId WHERE e.userId = ?;

-- Gets all users enrolled in a given class. Used when searching for classes.
-- URL: /search-classes
SELECT u.userId, u.username, u.firstName, u.lastName FROM user u JOIN enrollment e ON u.userId = e.userId JOIN class c ON e.classId = c.classId WHERE c.classCode = ?;

-- Gets user data from a given ID. Used when viewing another user's profile.
-- URL: /profile/view
SELECT userId, username, firstName, lastName FROM user WHERE userId = ?;

-- Determines if a given user follows another given user, as well as following and unfollowing users. Used to implement the "Follow"/"Unfollow" buttons on other users' profiles and on the class search page.
-- URLs: /search-classes, /profile/view
SELECT COUNT(*) FROM follow WHERE followerId = ? AND followeeId = ?;
INSERT INTO follow (followerId, followeeId, followDate) VALUES (?, ?, ?);
DELETE FROM follow WHERE followerId = ? AND followeeId = ?;

-- Get all users followed by and following the given user, respectively. Used on the "Connections" page.
-- URL: /follow
SELECT u.userId, u.username, u.firstName, u.lastName FROM user u JOIN follow f ON u.userId = f.followeeId WHERE f.followerId = ?;
SELECT u.userId, u.username, u.firstName, u.lastName FROM user u JOIN follow f ON u.userId = f.followerId WHERE f.followeeId = ?;

-- Creates or deletes an availability for the given user.
-- URL: /profile/availability
INSERT INTO availability (userId, studyDate, startTime, endTime) VALUES (?, ?, ?, ?);
DELETE FROM availability WHERE availabilityId = ?;

-- Gets all availabilities for a single given user. Used to show the current user's availabilities.
-- URL: /profile/availability
SELECT availabilityId, userId, studyDate, startTime, endTime FROM availability WHERE userId = ? ORDER BY studyDate, startTime;

-- Gets all availabilities for users that a given user follows. Used for the home page's main functionality.
-- URL: /
SELECT u.userId, u.firstName, u.lastName, a.availabilityId, a.studyDate, a.startTime, a.endTime FROM user u JOIN follow f ON u.userId = f.followeeId JOIN availability a ON u.userId = a.userId WHERE f.followerId = ? ORDER BY u.userId, a.studyDate, a.startTime;

-- Creates a new study request from a given user to a given availability.
-- URL: /
INSERT INTO study_request (requesterId, availabilityId, requestDate)VALUES (?, ?, ?);

-- Gets all pending study requests for a given user.
-- URL: /
SELECT sr.requestId, sr.requesterId, t.userId AS targetUserId, sr.availabilityId, sr.status, sr.requestDate, u.firstName, u.lastName, a.studyDate, a.startTime, a.endTime FROM study_request sr JOIN user u ON sr.requesterId = u.userId JOIN availability a ON sr.availabilityId = a.availabilityId JOIN user t ON a.userId = t.userId WHERE t.userId = ? AND sr.status = 'PENDING' ORDER BY sr.requestDate DESC;

-- Accepts or rejects a given study request.
-- URL: /
UPDATE study_request SET status = ? WHERE requestId = ?;

-- Gets all classes that two given users share. Used to determine if a user can request to join another user's availability.
-- URL: /
SELECT c.classId, c.classCode, c.className FROM class c JOIN enrollment e1 ON c.classId = e1.classId JOIN enrollment e2 ON c.classId = e2.classId WHERE e1.userId = ? AND e2.userId = ?;

-- Gets all upcoming study sessions that a given user requested to and was allowed to join.
-- URL: /sessions
SELECT sr.requestId, sr.requesterId, t.userId AS targetUserId, sr.availabilityId, sr.status, sr.requestDate, u1.firstName AS requesterFirstName, u1.lastName AS requesterLastName, t.firstName AS targetFirstName, t.lastName AS targetLastName, a.studyDate, a.startTime, a.endTime FROM study_request sr JOIN user u1 ON sr.requesterId = u1.userId JOIN availability a ON sr.availabilityId = a.availabilityId JOIN user t ON t.userId = a.userId WHERE (sr.requesterId = ? OR t.userId = ?) AND sr.status = 'APPROVED' AND a.studyDate >= CURRENT_DATE ORDER BY a.studyDate, a.startTime;

-- Gets all information about a given user, including their hashed password. Used for authentication.
-- URL: /login
SELECT userId, firstName, lastName, password FROM user WHERE username = ?;

-- Registers a new user.
-- URL: /login
INSERT INTO user (username, password, firstName, lastName) VALUES (?, ?, ?, ?);