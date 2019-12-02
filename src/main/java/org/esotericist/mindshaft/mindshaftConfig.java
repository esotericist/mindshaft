package org.esotericist.mindshaft;

/*
import net.minecraftforge.common.ForgeConfig;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

*/

// import net.minecraftforge.common.config.Config;
// import net.minecraftforge.common.config.Config.Name;
// import net.minecraftforge.common.config.Config.RangeInt;
// import net.minecraftforge.common.config.Config.Type;
// import net.minecraftforge.common.config.ConfigManager;

// @Config(modid = Mindshaft.MODID, type = Type.INSTANCE)
// @Config.LangKey("mindshaft.config.title")

public class mindshaftConfig {

    // @Config.Comment("Whether Mindshaft is currently enabled.")
    // @Name("Enabled")
    public static boolean enabled = true;

    // @Config.Comment({ "List of zoom levels available for both minimap and fullscreen.",
            // "Specified as a radius in chunks.",
            // "Notably: centered on the northwest corner of the player's current chunk.",
            // "So a radius of 6 is 12 chunks (or 192 blocks) across.",
            // "It's best if these are in numerical order (ascending or descending),",
            // "otherwise the order they cyle through may not be as expected." })
    // @RangeInt(min = 1, max = 7)
    // @Name("Zoom level list")
    public static int[] zoomlevels = { 5, 3, 1 };

    // @Config.Comment({ "Manually specify minimap zoom level without using in-game key bindings", "Default zoom levels:",
            // "0: 192 blocks across.", "1: 128 blocks across.", "2: 64 blocks across.", "3: 32 blocks across." ,
            // "Minimap zoom level when adjusted with key bindings is also saved here."})
    // @RangeInt(min = 0, max = 255)
    // @Name("Current zoom level")
    public static int zoom = 2;

    // @Config.Comment({ "Manually specify fullscreen map zoom level without using in-game key bindings",
            // "Default zoom levels:", "0: 192 blocks across.", "1: 128 blocks across.", "2: 64 blocks across.",
            // "3: 32 blocks across.",
            // "Fullscreen map zoom level when adjusted with key bindings is also saved here." })
    // @RangeInt(min = 0, max = 255)
    // @Name("Current fullscreen zoom level")
    public static int zoomfs = 0;

    // @Config.Comment({"Whether the zoom in/zoom out key bindings should wrap around.",
                     // "e.g. when zooming in at the tightest zoom level, go to the widest zoom level."})
    // @Name("Zoom Wraparound")
    public static boolean zoomwrap = false;

    // @Config.Comment({ "How many segments (chunk tiles) can be cached per tick.",
                      // "Can increase graphical stuttering if too high.",
                      // "Lower values increase the visibility of tile scanning,",
                      // "most especially at wider zoom levels." })
    // @RangeInt(min = 8, max = 256)
    // @Name("Segment processing rate")
    public static int chunkrate = 20;

    // @Config.Comment({ "How many extra ticks it takes to update the map.",
                      // "Higher numbers reduce graphical stuttering in the game itself, but increase map lag." })
    // @RangeInt(min = 0, max = 8)
    // @Name("Map refresh delay")
    public static int refreshdelay = 1;

    // @Config.Comment({ "Size of minimap as expressed in percentage of the vertical size of the screen.",
            // "Since the minimap is a square, it will be the same width as height." })
    // @RangeInt(min = 10, max = 50)
    // @Name("Minimap width")
    public static int mapwidth = 35;

    // @Config.Comment({ "Size of fullscreen map as expressed in percentage of the vertical size of the screen.",
            // "Since the map is a square, it will be the same width as height." })
    // @RangeInt(min = 30, max = 100)
    // @Name("Fullscreen minimap width")
    public static int mapwidthfs = 85;

    // @Config.Comment({ "Set false to base the minimap offset on the right side of the screen." })
    // @Name("Is offset from left?")
    public static boolean offsetfromleft = false;

    // @Config.Comment({ "The minimap edge is offset this far from the edge of the screen.",
            // "Expressed as percentage of horizontal screen width.", "0 is flush to edge.",
            // "Has no effect on the fullscreen map." })
    // @RangeInt(min = 0, max = 100)
    // @Name("Horizontal offset percentage")
    public static int offsetX = 0;

    // @Config.Comment({ "Set false to base the minimap offset on the bottom side of the screen." })
    // @Name("Is offset from top?")
    public static boolean offsetfromtop = true;

    // @Config.Comment({ "The minimap edge is offset this far from the edge of the screen.",
            // "Expressed as percentage of vertical screen width.", "0 is flush to edge.",
            // "Has no effect on the fullscreen map." })
    // @RangeInt(min = 0, max = 100)
    // @Name("Vertical offset percentage")
    public static int offsetY = 0;

    // @Config.Comment({ "This is an arbitrary measurement, set to 16 by default." })
    // @RangeInt(min = 1, max = 256)
    // @Name("Cursor size")
    public static int cursorsize = 16;

    // @Config.Comment({ "This is an arbitrary measurement, set to 32 by default." })
    // @RangeInt(min = 1, max = 256)
    // @Name("Fullscreen cursor size")
    public static int cursorsizefs = 32;

    // @Config.Comment({ "100 is fully opaque, 0 is invisible." })
    // @RangeInt(min = 0, max = 100)
    // @Name("Cursor opacity")
    public static int cursoropacity = 100;

    // @Config.Comment({ "100 is fully opaque, 0 is invisible." })
    // @RangeInt(min = 0, max = 100)
    // @Name("Fullscreen cursor opacity")
    public static int cursoropacityfs = 100;


    public static int forcedExpiry = 8000;

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
        // ConfigManager.sync(Mindshaft.MODID, Config.Type.INSTANCE);
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
