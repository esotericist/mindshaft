package org.esotericist.mindshaft;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Config.Name;
import net.minecraftforge.common.config.Config.RangeInt;
import net.minecraftforge.common.config.Config.Type;
import net.minecraftforge.common.config.ConfigManager;

@Config(modid = Mindshaft.MODID, type = Type.INSTANCE)
@Config.LangKey("mindshaft.config.title")

public class mindshaftConfig {

    @Config.Comment("Whether Mindshaft is currently enabled.")
    @Name("Enabled")
    public static boolean enabled = false;

    @Config.Comment({ "List of zoom levels available for both minimap and fullscreen.",
            "Wider zoom levels have substantially more performance impact." })
    @RangeInt(min = 16, max = 192)
    @Name("Zoom level list")
    public static int[] zoomlevels = { 192, 128, 64, 32 };

    @Config.Comment({ "Manually specify minimap zoom level without using in-game key bindings", "Default zoom levels:",
            "0: 192 blocks across.", "1: 128 blocks across.", "2: 64 blocks across.", "3: 32 blocks across." })
    @RangeInt(min = 0, max = 255)
    @Name("Current zoom level")
    public static int zoom = 3;

    @Config.Comment({ "Manually specify fullscreen map zoom level without using in-game key bindings",
            "Default zoom levels:", "0: 192 blocks across.", "1: 128 blocks across.", "2: 64 blocks across.",
            "3: 32 blocks across." })
    @RangeInt(min = 0, max = 255)
    @Name("Current fullscreen zoom level")
    public static int zoomfs = 3;

    @Config.Comment({ "Base rate for layer processing per tick, divided by current zoom level.",
            "ex: 384 (default rate) / 192 (default largest zoom level) = 2 layers per tick.",
            "Higher values result in greater rendering performance impact but faster update responsiveness.",
            "If you experience stuttering, lower this value and/or use smaller zoom levels." })
    @RangeInt(min = 128, max = 1024)
    @Name("Layer processing rate")
    public static int layerrate = 384;

    @Config.Comment({ "Size of minimap as expressed in percentage of the vertical size of the screen.",
            "Since the minimap is a square, it will be the same width as height." })
    @RangeInt(min = 10, max = 50)
    @Name("Minimap width")
    public static int mapwidth = 20;

    @Config.Comment({ "Size of fullscreen map as expressed in percentage of the vertical size of the screen.",
            "Since the map is a square, it will be the same width as height." })
    @RangeInt(min = 30, max = 100)
    @Name("Fullscreen minimap width")
    public static int mapwidthfs = 80;

    @Config.Comment({ "Set false to base the minimap offset on the right side of the screen." })
    @Name("Is offset from left?")
    public static boolean offsetfromleft = false;

    @Config.Comment({ "The minimap edge is offset this far from the edge of the screen.",
            "Expressed as percentage of horizontal screen width.", "0 is flush to edge.",
            "Has no effect on the fullscreen map." })
    @RangeInt(min = 0, max = 100)
    @Name("Horizontal offset percentage")
    public static int offsetX = 0;

    @Config.Comment({ "Set false to base the minimap offset on the bottom side of the screen." })
    @Name("Is offset from top?")
    public static boolean offsetfromtop = false;

    @Config.Comment({ "The minimap edge is offset this far from the edge of the screen.",
            "Expressed as percentage of vertical screen width.", "0 is flush to edge.",
            "Has no effect on the fullscreen map." })
    @RangeInt(min = 0, max = 100)
    @Name("Vertical offset percentage")
    public static int offsetY = 0;

    @Config.Comment({ "This is an arbitrary measurement, set to 16 by default." })
    @RangeInt(min = 1, max = 256)
    @Name("Cursor size")
    public static int cursorsize = 16;

    @Config.Comment({ "This is an arbitrary measurement, set to 32 by default." })
    @RangeInt(min = 1, max = 256)
    @Name("Fullscreen cursor size")
    public static int cursorsizefs = 32;

    @Config.Comment({ "100 is fully opaque, 0 is invisible." })
    @RangeInt(min = 0, max = 100)
    @Name("Cursor opacity")
    public static int cursoropacity = 100;

    @Config.Comment({ "100 is fully opaque, 0 is invisible." })
    @RangeInt(min = 0, max = 100)
    @Name("Fullscreen cursor opacity")
    public static int cursoropacityfs = 100;

    public static double getMapsize() {
        return (double) mapwidth / 100.0;
    }

    public static double getFSMapsize() {
        return (double) mapwidthfs / 100.0;
    }

    public static double getOffsetX() {
        return (double) offsetX / 100.0;
    }

    public static double getOffsetY() {
        return (double) offsetY / 100.0;
    }

    public static float getCursorOpacity(boolean fullscreen) {
        float opacity;
        if (fullscreen) {
            opacity = cursoropacityfs;
        } else {
            opacity = cursoropacity;
        }
        return opacity / (float) 100.0;
    }

    private static void refresh() {
        ConfigManager.sync(Mindshaft.MODID, Config.Type.INSTANCE);
    }

    public static void setEnabled(boolean value) {
        enabled = value;
        refresh();

    }

    public static void setZoom(int value, boolean fullscreen) {
        if (fullscreen) {
            zoomfs = value;
        } else {
            zoom = value;
        }
        refresh();
    }
}
