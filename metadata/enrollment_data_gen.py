import random

print('INSERT INTO enrollment VALUES')
for i in range(1, 255):
    enrolled = set()
    taken = set()
    for j in range(150):
        k = random.randint(1, 8557);
        while (k == i or k in taken):
            k = random.randint(1, 8557)
        taken.add(k)
        enrolled.add(f'({i}, {k}, NOW()),')
    print('\n'.join(enrolled))
