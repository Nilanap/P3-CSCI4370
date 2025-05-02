import mysql.connector
import random

# Query the DB for valid user IDs, since there shouldn't be any study requests from users to users they don't follow.
mydb = mysql.connector.connect(
  host="localhost",
  port=33306,
  user="root",
  password="mysqlpassword",
  database="study_buddy"
)

cursor = mydb.cursor()

print('INSERT INTO study_request (requesterId, availabilityId, status, requestDate) VALUES')
for i in range(1, 255):
    # Adapted from queries.sql
    cursor.execute(f'SELECT a.availabilityId FROM user u JOIN follow f ON u.userId = f.followeeId JOIN availability a ON u.userId = a.userId WHERE f.followerId = {i} ORDER BY u.userId, a.studyDate, a.startTime')
    results = cursor.fetchall()
    taken = set()
    for j in range(10):
        availabilityId = random.choice(results)
        while availabilityId in taken:
           availabilityId = random.choice(results)
        taken.add(availabilityId)
        status = random.choice(['APPROVED', 'PENDING', 'REJECTED'])
        print(f'({i}, {availabilityId[0]}, \'{status}\', CURRENT_DATE),')
