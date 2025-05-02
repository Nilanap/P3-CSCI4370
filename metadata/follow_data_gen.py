import names
import random
import string

print('INSERT INTO follow VALUES')
for i in range(1, 255):
    followees = set()
    taken = set()
    for j in range (1, 125):
        k = random.randint(1, 254);
        while (k == i or k in taken):
            k = random.randint(1, 254)
        taken.add(k)
        followees.add(f'({i}, {k}, NOW()),')
    print('\n'.join(followees))
