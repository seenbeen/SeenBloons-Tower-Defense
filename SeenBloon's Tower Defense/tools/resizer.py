from pygame import *
from glob import *
running = True;

#screen = display.set_mode((40,44))
print("Enter Width:")
x = int(input());
print("Enter Height:")
y = int(input());

files = glob("*.png")

images = [image.load(each) for each in files]
#trans = Surface((x,y))
for i in range(len(images)):
    layer = Surface((x,y),SRCALPHA);
    layer.blit(images[i],(x/2-images[i].get_width()/2,y/2-images[i].get_height()/2))
    image.save(layer,files[i])

def rollback():
    for i in range(len(images)):
        image.save(images[i],files[i])
