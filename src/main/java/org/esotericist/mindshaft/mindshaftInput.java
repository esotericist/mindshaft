package org.esotericist.mindshaft;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;

import org.lwjgl.input.Keyboard;

class inputHandler {
    
    private static KeyBinding[] keyBindings;
    private boolean[] pressed;


    public void processKeys() {
        // binding 0: enable/disable toggle
        if (keyBindings[0].isPressed() && !pressed[0]) {
            if (mindshaftConfig.enabled) {
                mindshaftConfig.setEnabled(false);
            } else {
                mindshaftConfig.setEnabled(true);
            }
            pressed[0] = true;
        }
        if (!keyBindings[0].isPressed() && pressed[0]) {
            pressed[0] = false;
        }

        // binding 1: fullscreen toggle
        // this doesn't have a config entry because it isn't meant to be persistent across sessions.
        if (keyBindings[1].isPressed() && !pressed[1]) {
            if (Mindshaft.zoom.fullscreen == false) {
                Mindshaft.zoom.fullscreen = true;
            } else {
                Mindshaft.zoom.fullscreen = false;
            }
        }
        if (!keyBindings[1].isPressed() && pressed[1]) {
            pressed[1] = false;
        }

        // binding 2: zoom in
        if (keyBindings[2].isPressed() && !pressed[2]) {
            Mindshaft.zoom.nextZoom();
            pressed[2] = true;
        }
        if (!keyBindings[2].isPressed() && pressed[2]) {
            pressed[2] = false;
        }

        // binding 3: zoom out
        if (keyBindings[3].isPressed() && !pressed[3]) {
            Mindshaft.zoom.prevZoom();
            pressed[3] = true;
        }
        if (!keyBindings[3].isPressed() && pressed[3]) {
            pressed[3] = false;
        }
        
    }

    public void initBindings() {
        keyBindings = new KeyBinding[4];
        pressed = new boolean[4];
        
        keyBindings[0] = new KeyBinding("mindshaft.key.toggle.desc", Keyboard.KEY_NUMPAD1, "mindshaft.key.category");
        keyBindings[1] = new KeyBinding("mindshaft.key.fullscreen.desc", Keyboard.KEY_NUMPAD2, "mindshaft.key.category");
        keyBindings[2] = new KeyBinding("mindshaft.key.zoomin.desc", Keyboard.KEY_NUMPAD6, "mindshaft.key.category");
        keyBindings[3] = new KeyBinding("mindshaft.key.zoomout.desc", Keyboard.KEY_NUMPAD3, "mindshaft.key.category");
        
        for (int i = 0; i < keyBindings.length; ++i) {
            ClientRegistry.registerKeyBinding(keyBindings[i]);
            pressed[i] = false;
        }
    }

}