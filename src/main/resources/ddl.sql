CREATE DATABASE IF NOT EXISTS study_buddy;

USE study_buddy;

-- user table
CREATE TABLE IF NOT EXISTS user
(
    userId    INT AUTO_INCREMENT,
    username  VARCHAR(255) NOT NULL,
    password  VARCHAR(255) NOT NULL,
    firstName VARCHAR(255) NOT NULL,
    lastName  VARCHAR(255) NOT NULL,
    PRIMARY KEY (userId),
    UNIQUE (username),
    CONSTRAINT username_min_length CHECK (CHAR_LENGTH(TRIM(username)) >= 2),
    CONSTRAINT firstName_min_length CHECK (CHAR_LENGTH(TRIM(firstName)) >= 2),
    CONSTRAINT lastName_min_length CHECK (CHAR_LENGTH(TRIM(lastName)) >= 2)
);

-- classes and their id table
CREATE TABLE IF NOT EXISTS class
(
    classId   INT AUTO_INCREMENT,
    classCode VARCHAR(50)  NOT NULL,
    className VARCHAR(255) NOT NULL,
    PRIMARY KEY (classId),
    UNIQUE (classCode),
    CONSTRAINT classCode_min_length CHECK (CHAR_LENGTH(TRIM(classCode)) >= 1),
    CONSTRAINT className_min_length CHECK (CHAR_LENGTH(TRIM(className)) >= 1)
);

CREATE TABLE IF NOT EXISTS enrollment
(
    userId         INT,
    classId        INT,
    enrollmentDate DATETIME NOT NULL,
    PRIMARY KEY (userId, classId),
    FOREIGN KEY (userId) REFERENCES user (userId)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    FOREIGN KEY (classId) REFERENCES class (classId)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS follow
(
    followerId INT,
    followeeId INT,
    followDate DATETIME NOT NULL,
    PRIMARY KEY (followerId, followeeId),
    FOREIGN KEY (followerId) REFERENCES user (userId)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    FOREIGN KEY (followeeId) REFERENCES user (userId)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS availability
(
    availabilityId INT AUTO_INCREMENT PRIMARY KEY,
    userId         INT  NOT NULL,
    studyDate      DATE NOT NULL,
    startTime      TIME NOT NULL,
    endTime        TIME NOT NULL,
    FOREIGN KEY (userId) REFERENCES user (userId),
    UNIQUE (userId, studyDate, startTime)
);

CREATE TABLE IF NOT EXISTS study_request
(
    requestId      INT AUTO_INCREMENT PRIMARY KEY,
    requesterId    INT      NOT NULL,
    availabilityId INT      NOT NULL,
    status         ENUM ('PENDING', 'APPROVED', 'REJECTED') DEFAULT 'PENDING',
    requestDate    DATETIME NOT NULL,
    FOREIGN KEY (requesterId) REFERENCES user (userId),
    FOREIGN KEY (availabilityId) REFERENCES availability (availabilityId),
    UNIQUE (requesterId, availabilityId)
);