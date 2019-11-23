package org.esotericist.mindshaft;


import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Config.RangeInt;
import net.minecraftforge.common.config.Config.Type;
import net.minecraftforge.common.config.ConfigManager;


@Config(modid = Mindshaft.MODID, type = Type.INSTANCE)
@Config.LangKey("mindshaft.config.title")

public class mindshaftConfig  {

    @Config.Comment("Whether Mindshaft is currently enabled.")
    public static boolean enabled = false;

    @Config.Comment({"List of zoom levels available for both minimap and fullscreen.", "Wider zoom levels have substantially more performance impact."})
    @RangeInt(min = 16, max=192)
    public static int[] zoomlevels = { 192, 128, 64, 32 };

    @Config.Comment({"Current zoom level.","Default zoom levels:","0: 192 blocks across.","1: 128 blocks across.","2: 64 blocks across.","3: 32 blocks across."})
    @RangeInt(min=0, max=255)
    public static int zoom = 3;
    
    @Config.Comment({"Current fullscreen zoom level.","Default zoom levels:","0: 192 blocks across.","1: 128 blocks across.","2: 64 blocks across.","3: 32 blocks across."})
    @RangeInt(min=0, max=255)
    public static int zoomfs = 3;

    @Config.Comment({"Base rate for layer processing per tick, divided by current zoom level.", "ex: 384 (default rate) / 192 (default largest zoom level) = 2 layers per tick." , "Higher values result in greater rendering performance impact but faster update responsiveness.", "If you experience stuttering, lower this value or use smaller zoom levels."})
    @RangeInt(min=128, max=1024)
    public static int layerrate = 384;

    @Config.Comment({"Minimap size.","Size of minimap as expressed in percentage of the vertical size of the screen.",
                    "Since the minimap is a square, it'll be the same width as height."})
    @RangeInt(min=10, max=50)
    public static int mapwidth = 20;
    
    @Config.Comment({"Horizontal offset from left.","Set false to base the offset on the right side of the screen."})    
    public static boolean offsetfromleft = false;
    
    @Config.Comment({"Offset percentage.","The edge is this far from the edge of the screen.","0 is flush to edge."})
    @RangeInt(min=0,max=100)
    public static int offsetX = 0;
    
    @Config.Comment({"Vertical offset from top.","Set false to base the offset on the bottom side of the screen."})    
    public static boolean offsetfromtop = false;

    @Config.Comment({"Offset percentage.","The edge is this far from the edge of the screen.","0 is flush to edge."})
    @RangeInt(min=0,max=100)
    public static int offsetY = 0;    
    
    @Config.Comment({"Fullscreen minimap size.","Size of minimap as expressed in percentage of the vertical size of the screen.",
    "Since the minimap is a square, it'll be the same width as height."})
    @RangeInt(min=30, max=100)
    public static int fsmapwidth = 80;

    @Config.Comment({"Cursor size.","This is an arbitrary measurement, set to 16 by default."})
    @RangeInt(min=1, max=30)
    public static int cursorsize = 16;

    @Config.Comment({"Fullscreen cursor size.","This is an arbitrary measurement, set to 32 by default."})
    @RangeInt(min=1, max=30)
    public static int fscursorsize = 32;

    @Config.Comment({"Cursor opacity.","100 is fully opaque, 0 is invisible."})
    @RangeInt(min=0, max=100)
    public static int cursoropacity = 100;

    public static double getMapsize() {
        return (double) mapwidth / 100.0;
    }
    
    public static double getFSMapsize() {
        return (double) fsmapwidth / 100.0;
    }
    
    public static double getOffsetX() {
        return (double) offsetX / 100.0;
    }

    public static double getOffsetY() {
        return (double) offsetY / 100.0;
    }
    

    public static float  getCursorOpacity() {
        return (float) cursoropacity / (float) 100.0;
    }
    
    private static void refresh() {
        ConfigManager.sync(Mindshaft.MODID, Config.Type.INSTANCE);
    }


    public static void setEnabled(boolean value) {
        enabled = value;
        refresh();
    
    }

    public static void setZoom( int value ) {
        zoom = value;
        refresh();
    }

    public static void setFSZoom( int value ) {
        zoomfs = value;
        refresh();
    }

/*    @Mod.EventBusSubscriber(modid = Mindshaft.MODID)
    private static class EventHandler {
    
        @SubscribeEvent
        public static void onConfigChanged(final ConfigChangedEvent.OnConfigChangedEvent event) {
            if (event.getModID().equals(Mindshaft.MODID)) {
                ConfigManager.sync(Mindshaft.MODID, Config.Type.INSTANCE);
            }
        }
    
    }
    */
}
