from pygame import *
from math import *

running = True
screen = display.set_mode((833,634))
mapz = image.load("map1.png")
points = []

screen.blit(mapz,(0,0))
while running:
    for evt in event.get():
        if evt.type == QUIT:
            running = False
        elif evt.type == MOUSEBUTTONDOWN:
            if evt.button == 1:
                start = evt.pos
        elif evt.type == MOUSEBUTTONUP:
            if evt.button == 1:
                end = evt.pos
                x,y = start
                x1,y1 = end
                dist = hypot(x-x1,y-y1);
                for i in range(int(dist)):
                    if [int(x+(x1-x)/dist*i),int(y+(y1-y)/dist*i)] not in points:
                        points.append([int(x+(x1-x)/dist*i),int(y+(y1-y)/dist*i)])
                draw.line(screen,(0,0,0),(x,y),(x1,y1),2)
    mb = mouse.get_pressed()
    mx,my = mouse.get_pos()
    display.flip()

myfile = open("map1.txt","w")
myfile.write(str(len(points))+"\n")
for each in points:
    myfile.write("%i,%i\n"%(each[0],each[1]))
quit()
