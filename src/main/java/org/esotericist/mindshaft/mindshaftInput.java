package org.esotericist.mindshaft;

import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.ClientRegistry;
import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

class inputHandler {

    private static KeyMapping[] keyBindings;
    private boolean[] pressed;

    @SubscribeEvent
    public void onKeyInput(InputEvent.Key event) {

        Minecraft mc = Minecraft.getInstance();
        if (!mc.isWindowActive()) {
            return;
        }

        zoomState zoom = Mindshaft.zoom;

        // binding 0: enable/disable toggle
        if (keyBindings[0].isDown() && !pressed[0]) {
            if (mindshaftConfig.enabled) {
                mindshaftConfig.setEnabled(false);
            } else {
                mindshaftConfig.setEnabled(true);
            }
            pressed[0] = true;
        }
        if (!keyBindings[0].isDown() && pressed[0]) {
            pressed[0] = false;
        }

        // binding 1: fullscreen toggle
        // this doesn't have a config entry because it isn't meant to be persistent
        // across sessions.
        if (keyBindings[1].isDown() && !pressed[1]) {
            if (zoom.fullscreen == false) {
                zoom.fullscreen = true;
            } else {
                zoom.fullscreen = false;
            }
        }
        if (!keyBindings[1].isDown() && pressed[1]) {
            pressed[1] = false;
        }

        // binding 2: zoom in
        if (keyBindings[2].isDown() && !pressed[2]) {
            zoom.nextZoom();
            pressed[2] = true;
        }
        if (!keyBindings[2].isDown() && pressed[2]) {
            pressed[2] = false;
        }

        // binding 3: zoom out
        if (keyBindings[3].isDown() && !pressed[3]) {
            zoom.prevZoom();
            pressed[3] = true;
        }
        if (!keyBindings[3].isDown() && pressed[3]) {
            pressed[3] = false;
        }

    }

    public inputHandler() {
        keyBindings = new KeyMapping[4];
        pressed = new boolean[4];
        keyBindings[0] = new KeyMapping("mindshaft.key.toggle.desc",  InputConstants.KEY_NUMPAD1, 
                "mindshaft.key.category");
        keyBindings[1] = new KeyMapping("mindshaft.key.fullscreen.desc", InputConstants.KEY_NUMPAD0,
                "mindshaft.key.category");
        keyBindings[2] = new KeyMapping("mindshaft.key.zoomin.desc", InputConstants.KEY_NUMPAD6,
                "mindshaft.key.category");
        keyBindings[3] = new KeyMapping("mindshaft.key.zoomout.desc", InputConstants.KEY_NUMPAD3,
                "mindshaft.key.category");

        for (int i = 0; i < keyBindings.length; ++i) {
            ClientRegistry.registerKeyBinding(keyBindings[i] );
            pressed[i] = false;
        }
    }

}