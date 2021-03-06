package org.esotericist.mindshaft;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

class inputHandler {

    private static KeyBinding[] keyBindings;
    private boolean[] pressed;

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {

        Minecraft mc = Minecraft.getInstance();
        if (!mc.isGameFocused()) {
            return;
        }

        zoomState zoom = Mindshaft.zoom;

        // binding 0: enable/disable toggle
        if (keyBindings[0].isKeyDown() && !pressed[0]) {
            if (mindshaftConfig.enabled) {
                mindshaftConfig.setEnabled(false);
            } else {
                mindshaftConfig.setEnabled(true);
            }
            pressed[0] = true;
        }
        if (!keyBindings[0].isKeyDown() && pressed[0]) {
            pressed[0] = false;
        }

        // binding 1: fullscreen toggle
        // this doesn't have a config entry because it isn't meant to be persistent
        // across sessions.
        if (keyBindings[1].isKeyDown() && !pressed[1]) {
            if (zoom.fullscreen == false) {
                zoom.fullscreen = true;
            } else {
                zoom.fullscreen = false;
            }
        }
        if (!keyBindings[1].isKeyDown() && pressed[1]) {
            pressed[1] = false;
        }

        // binding 2: zoom in
        if (keyBindings[2].isKeyDown() && !pressed[2]) {
            zoom.nextZoom();
            pressed[2] = true;
        }
        if (!keyBindings[2].isKeyDown() && pressed[2]) {
            pressed[2] = false;
        }

        // binding 3: zoom out
        if (keyBindings[3].isKeyDown() && !pressed[3]) {
            zoom.prevZoom();
            pressed[3] = true;
        }
        if (!keyBindings[3].isKeyDown() && pressed[3]) {
            pressed[3] = false;
        }

    }

    public inputHandler() {
        keyBindings = new KeyBinding[4];
        pressed = new boolean[4];

        keyBindings[0] = new KeyBinding("mindshaft.key.toggle.desc", GLFW.glfwGetKeyScancode(GLFW.GLFW_KEY_KP_1),
                "mindshaft.key.category");
        keyBindings[1] = new KeyBinding("mindshaft.key.fullscreen.desc", GLFW.glfwGetKeyScancode(GLFW.GLFW_KEY_KP_2),
                "mindshaft.key.category");
        keyBindings[2] = new KeyBinding("mindshaft.key.zoomin.desc", GLFW.glfwGetKeyScancode(GLFW.GLFW_KEY_KP_6),
                "mindshaft.key.category");
        keyBindings[3] = new KeyBinding("mindshaft.key.zoomout.desc", GLFW.glfwGetKeyScancode(GLFW.GLFW_KEY_KP_3),
                "mindshaft.key.category");

        for (int i = 0; i < keyBindings.length; ++i) {
            ClientRegistry.registerKeyBinding(keyBindings[i]);
            pressed[i] = false;
        }
    }

}