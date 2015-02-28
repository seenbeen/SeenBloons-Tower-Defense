from pygame import *

running = True

screen = display.set_mode((800,600))

level = open("level.txt","w")

count = 0

while running:
    for evt in event.get():
        if evt.type == QUIT:
            running = False
        elif evt.type == KEYDOWN:
            if evt.key == 97:
                level.write("%i 0\n"%count)
            elif evt.key == 115:
                level.write("%i 1\n"%count)
            elif evt.key == 100:
                level.write("%i 2\n"%count)
            elif evt.key == 102:
                level.write("%i 3\n"%count)
            elif evt.key == 103:
                level.write("%i 4\n"%count)
    count+=1;
    time.wait(20);
    display.flip()
level.write("-1 -1\n")
level.close();
quit()
