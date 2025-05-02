import http.client
import names
import random
import string

# Make the website generate accounts, since it has the code needed to encrypt them in a way that it can decrypt later.
conn = http.client.HTTPConnection("localhost", 8080)
headers = {
  'Content-Type': 'application/x-www-form-urlencoded'
}

for i in range(250):
    first, last = names.get_first_name(), names.get_last_name()
    username = first[0] + last + str(random.randrange(2000, 2020))
    password = ''.join(random.choices(string.ascii_uppercase + string.digits, k=16))
    payload = f'username={username}&password={password}&firstName={first}&lastName={last}'
    conn.request("POST", "/register", payload, headers)
    res = conn.getresponse()
    data = res.read()

for i in range(1, 5):
    first, last = 'Test', f'User {i}'
    username = (first[0] + last + str(2000 + i)).replace(' ', '')
    password = f'password{i}'
    payload = f'username={username}&password={password}&firstName={first}&lastName={last}'
    conn.request("POST", "/register", payload, headers)
    res = conn.getresponse()
    data = res.read()
