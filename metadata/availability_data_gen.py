import random

def random_date_times():
    hour = random.randint(10, 18)
    min = random.randint(0, 59)
    return f'2025-05-0{random.randint(1,9)}', f'{hour}:{min}:00', f'{hour + 1}:{min}:00'

print('INSERT INTO availability (userId, studyDate, startTime, endTime) VALUES')
for i in range(1, 255):
    taken = set()    
    for j in range(10):
        day, start, end = random_date_times()
        while (day, start, end) in taken:
           day, start, end = random_date_times()
        taken.add((day, start, end))
        print(f'({i}, \'{day}\', \'{start}\', \'{end}\'),')
