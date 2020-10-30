import GameLogic
import bge
import bpy
import random

def main():

    GameLogic.greenCounter += 1
    if (GameLogic.greenCounter % 2 == 1):
        GameLogic.server.client.publish("Sensor-Simulation", payload="{\"entity\": \"Gate\", \"event\": \"object-detected\", \"channel\": \"green\"}", qos=0, retain=False)
        controller = bge.logic.getCurrentController()
        owner = controller.owner
        scene = bge.logic.getCurrentScene()
        source = scene.objectsInactive[0]
        print(source)
        new_object = scene.addObject(source, owner, 0)
        new_object.worldPosition = source.worldPosition
        new_object.localScale = [1.0,1.0,1.0]
        counter = GameLogic.greenCounter + GameLogic.redCounter
        qr_code = bpy.data.images.load("//qr-codes/code-" + str(counter) + ".png")
        bpy.data.objects["ObjectSource"].active_material.active_texture.image = qr_code

        counter = GameLogic.greenCounter + GameLogic.redCounter + 9
        qr_code = bpy.data.images.load("//qr-codes/code-" + str(counter) + ".png")
        material = bpy.data.objects["ObjectSource"].active_material.copy()
        texture = bpy.data.objects["ObjectSource"].active_material.active_texture.copy()
        texture.image = qr_code
        material.active_texture = texture
        bpy.data.objects["ObjectSource"].active_material = material
