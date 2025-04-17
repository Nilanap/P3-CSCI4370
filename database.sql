CREATE DATABASE IF NOT EXISTS study_buddy;


USE study_buddy;

-- user table
CREATE TABLE IF NOT EXISTS user (
    userId INT AUTO_INCREMENT,
    username VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    firstName VARCHAR(255) NOT NULL,
    lastName VARCHAR(255) NOT NULL,
    PRIMARY KEY (userId),
    UNIQUE (username),
    CONSTRAINT username_min_length CHECK (CHAR_LENGTH(TRIM(username)) >= 2),
    CONSTRAINT firstName_min_length CHECK (CHAR_LENGTH(TRIM(firstName)) >= 2),
    CONSTRAINT lastName_min_length CHECK (CHAR_LENGTH(TRIM(lastName)) >= 2)
);

-- classes and their id table
CREATE TABLE IF NOT EXISTS class (
    classId INT AUTO_INCREMENT,
    classCode VARCHAR(50) NOT NULL,
    className VARCHAR(255) NOT NULL,
    PRIMARY KEY (classId),
    UNIQUE (classCode),
    CONSTRAINT classCode_min_length CHECK (CHAR_LENGTH(TRIM(classCode)) >= 1),
    CONSTRAINT className_min_length CHECK (CHAR_LENGTH(TRIM(className)) >= 1)
);


CREATE TABLE IF NOT EXISTS enrollment (
    userId INT NOT NULL,
    classId INT NOT NULL,
    enrollmentDate DATETIME NOT NULL,
    PRIMARY KEY (userId, classId),
    FOREIGN KEY (userId) REFERENCES user(userId)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    FOREIGN KEY (classId) REFERENCES class(classId)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS study_request (
    requestId INT AUTO_INCREMENT,
    senderId INT NOT NULL,
    receiverId INT NOT NULL,
    classId INT NOT NULL,
    status ENUM('pending', 'approved', 'rejected') NOT NULL DEFAULT 'pending',
    requestDate DATETIME NOT NULL,
    PRIMARY KEY (requestId),
    FOREIGN KEY (senderId) REFERENCES user(userId)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    FOREIGN KEY (receiverId) REFERENCES user(userId)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    FOREIGN KEY (classId) REFERENCES class(classId)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT unique_request UNIQUE (senderId, receiverId, classId)
);

CREATE TABLE IF NOT EXISTS message (
    messageId INT AUTO_INCREMENT,
    senderId INT NOT NULL,
    receiverId INT NOT NULL,
    classId INT NOT NULL,
    messageText VARCHAR(500) NOT NULL,
    messageDate DATETIME NOT NULL,
    PRIMARY KEY (messageId),
    FOREIGN KEY (senderId) REFERENCES user(userId)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    FOREIGN KEY (receiverId) REFERENCES user(userId)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    FOREIGN KEY (classId) REFERENCES class(classId)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT messageText_min_length CHECK (CHAR_LENGTH(TRIM(messageText)) > 0)
);