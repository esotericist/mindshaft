package org.esotericist.mindshaft;


import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Config.RangeInt;
import net.minecraftforge.common.config.Config.Type;
import net.minecraftforge.common.config.ConfigManager;


@Config(modid = Mindshaft.MODID, type = Type.INSTANCE)
@Config.LangKey("mindshaft.config.title")

public class mindshaftConfig  {

    @Config.Comment("Enable Mindshaft.")
    public static boolean enabled = false;

    @Config.Comment({"Zoom level.","0: 192 blocks across.","1: 128 blocks across.","2: 64 blocks across."})
    @RangeInt(min=0, max=2)
    public static int zoom = 2;
    
    @Config.Comment({"Minimap Size.","Size of minimap as expressed in percentage of the vertical size of the screen.",
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
    
    public static double getMapsize() {
        return (double) mapwidth / 100.0;
    }
    
    public static double getOffsetX() {
        return (double) offsetX / 100.0;
    }

    public static double getOffsetY() {
        return (double) offsetY / 100.0;
    }
    
    private static void refresh() {
        ConfigManager.sync(Mindshaft.MODID, Config.Type.INSTANCE);
    }


    public static void setEnabled(boolean value) {
        enabled = value;
        refresh();
    
    }
    
    public static void setZoom(int value, int max) {
        zoom = value;
        if (zoom >= max) {
            zoom = max - 1;
        }

        if (zoom <0) {
            zoom = 0;
        }
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
