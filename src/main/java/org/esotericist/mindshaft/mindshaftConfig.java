package org.esotericist.mindshaft;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;
import java.util.List;

import com.google.common.collect.ImmutableList;
@Mod.EventBusSubscriber(modid = Mindshaft.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class mindshaftConfig {

    public static boolean dirtyconfig = true;

	public static final ClientConfig CLIENT;
	public static final ForgeConfigSpec CLIENT_SPEC;
	static {
		final Pair<ClientConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(ClientConfig::new);
		CLIENT_SPEC = specPair.getRight();
		CLIENT = specPair.getLeft();
	}

    public static boolean enabled = true;

    static ForgeConfigSpec.ConfigValue<Boolean> v_enabled;
    static ForgeConfigSpec.ConfigValue<Integer> v_zoom;
    static ForgeConfigSpec.ConfigValue<Integer> v_zoomfs;
    static ForgeConfigSpec.ConfigValue<Boolean> v_zoomwrap;
    static ForgeConfigSpec.ConfigValue<Integer> v_chunkrate;
    static ForgeConfigSpec.ConfigValue<Integer> v_refreshdelay;
    static ForgeConfigSpec.ConfigValue<Integer> v_mapwidth;
    static ForgeConfigSpec.ConfigValue<Integer> v_mapwidthfs;
    static ForgeConfigSpec.ConfigValue<Boolean> v_offsetfromleft;
    static ForgeConfigSpec.ConfigValue<Integer> v_offsetX;
    static ForgeConfigSpec.ConfigValue<Boolean> v_offsetfromtop;
    static ForgeConfigSpec.ConfigValue<Integer> v_offsetY;
    static ForgeConfigSpec.ConfigValue<Integer> v_cursorsize;
    static ForgeConfigSpec.ConfigValue<Integer> v_cursorsizefs;
    static ForgeConfigSpec.ConfigValue<Integer> v_cursoropacity;
    static ForgeConfigSpec.ConfigValue<Integer> v_cursoropacityfs;
    static ForgeConfigSpec.ConfigValue<Integer> v_forcedExpiry;
    static ForgeConfigSpec.ConfigValue<List<? extends String>> v_zoomlevels;

    public static int[] zoomlevels = { 5, 3, 2, 1 };
    public static int zoom = 2;
    public static int zoomfs = 0;
    public static boolean zoomwrap = false;
    public static int chunkrate = 35;
    public static int refreshdelay = 1;
    public static int mapwidth = 35;
    public static int mapwidthfs = 85;
    public static boolean offsetfromleft = false;
    public static int offsetX = 0;
    public static boolean offsetfromtop = false;
    public static int offsetY = 0;
    public static int cursorsize = 16;
    public static int cursorsizefs = 32;
    public static int cursoropacity = 100;
    public static int cursoropacityfs = 100;
    public static int forcedExpiry = 8000;

    public static void bakeConfig() {
        enabled = v_enabled.get();
        zoom = v_zoom.get();
        zoomfs = v_zoomfs.get();
        zoomwrap = v_zoomwrap.get();
        chunkrate = v_chunkrate.get();
        refreshdelay = v_refreshdelay.get();
        mapwidth = v_mapwidth.get();
        mapwidthfs = v_mapwidthfs.get();
        offsetfromleft = v_offsetfromleft.get();
        offsetX = v_offsetX.get();
        offsetfromtop = v_offsetfromtop.get();
        offsetY = v_offsetY.get();
        cursorsize = v_cursorsize.get();
        cursorsizefs = v_cursorsizefs.get();
        cursoropacity = v_cursoropacity.get();
        cursoropacityfs = v_cursoropacityfs.get();
        forcedExpiry = v_forcedExpiry.get();
        zoomlevels = (v_zoomlevels.get().stream().mapToInt( s -> Integer.parseInt(s))).toArray();
        for( int i = 0; i < zoomlevels.length; i++  ) {
            if( zoomlevels[i] < 1) {
                zoomlevels[i] = 1;
            }
            if( zoomlevels[i] > 7) {
                zoomlevels[i] = 7;
            }
        }
    }

    public static class ClientConfig {
        public ClientConfig(final ForgeConfigSpec.Builder builder) {
            
            v_enabled = builder.comment("whether mindshaft is currently enabled.").define( "enabled", false );

            builder.push( "behavior" );
            v_zoom = builder.comment("manually specify minimap zoom level without using in-game key bindings",
                "refer to zoom level list for details.",
                "minimap zoom level when adjusted with key bindings is also saved here.").define("minimap zoom level", 2 );

            v_zoomlevels = builder.comment("list of zoom levels available for both minimap and fullscreen.",
                "specified as a radius in chunks.",
                "notably: centered on the northwest corner of the player's current chunk.",
                "so a radius of 6 is 12 chunks (or 192 blocks) across.",
                "it's best if these are in numerical order (ascending or descending),",
                "otherwise the order they cyle through may not be as expected.",
                "values currently constrained to be between 1 and 7.",
                "high values (over 5) will greatly impact update times unless you adjust the segment rate.",
                "whether you adjust the segment rate or not, high values will strongly impact performance.")
                .defineList("zoom level list", ImmutableList.of( "5", "3", "2", "1" ), a-> true );
            v_zoomfs = builder.comment("manually specify fullscreen map zoom level without using in-game key bindings",
                "refer to zoom level list for details.",
                "fullscreen map zoom level when adjusted with key bindings is also saved here.").define("fullscreen zoom level", 0 );
            v_zoomwrap = builder.comment("whether the zoom in/zoom out key bindings should wrap around.",
                "e.g. when zooming in at the tightest zoom level, go to the widest zoom level."
                ).define( "zoom wraparound", false );

            builder.pop();
            builder.push("appearance");
            v_mapwidth = builder.comment("size of minimap as expressed in percentage of the vertical size of the screen.",
                "since the minimap is a square, it will be the same width as height.")
                .defineInRange("minimap width", 35, 5, 50);
            v_mapwidthfs = builder.comment("size of fullscreen map as expressed in percentage of the vertical size of the screen.",
                "since the minimap is a square, it will be the same width as height.")
                .defineInRange("fullscreen map width", 85, 5, 100);
            v_offsetfromleft = builder.comment("set false to base the minimap offset on the right side of the screen.")
                .define("is offset from left?", false);
            v_offsetX = builder.comment("the minimap edge is offset this far from the edge of the screen.",
                "expressed as percentage of horizontal screen width.", "0 is flush to edge.",
                "has no effect on the fullscreen map, which is always centered.")
                .defineInRange("horizontal offset percentage", 0, 0, 100);
            v_offsetfromtop = builder.comment("set false to base the minimap offset on the bottom side of the screen.")
                .define("is offset from top?", false);
            v_offsetY = builder.comment("the minimap edge is offset this far from the edge of the screen.",
                "expressed as percentage of vertical screen width.", "0 is flush to edge.",
                "has no effect on the fullscreen map, which is always centered.")
                .defineInRange("vertical offset percentage", 0, 0, 100);
            v_cursorsize = builder.comment("this is an arbitrary measurement, set to 16 by default.")
                .defineInRange("cursor size", 16, 1, 256);
            v_cursorsizefs = builder.comment("this is an arbitrary measurement, set to 32 by default.")
                .defineInRange("fullscreen cursor size", 32, 1, 256);
            v_cursoropacity = builder.comment("100 is fully opaque, 0 is invisible.")
                .defineInRange("cursor opacity", 100, 0, 100);
            v_cursoropacityfs = builder.comment("100 is fully opaque, 0 is invisible.")
                .defineInRange("fullscreen cursor opacity", 100, 0, 100);
            builder.pop();
            builder.push("performance");

            v_chunkrate = builder.comment("how many segments (chunk tiles) can be cached per tick.",
                "can increase graphical stuttering if too high.",
                "lower values increase the visibility of tile scanning,",
                "most especially at wider zoom levels." )
                .defineInRange("segment processing rate", 24, 8, 256);
            v_refreshdelay = builder.comment("how many extra ticks it takes to update the map.",
                "higher numbers reduce graphical stuttering in the game itself, but increase map lag.")
                .defineInRange("map refresh delay", 1, 0, 8);
            v_forcedExpiry = builder.comment( "time in ticks before a segment is forcibly removed.",
                "setting this too low can cause performance issues.",
                "setting this too high can increase memory load.",
                "you probably don't need to change this.")
                .defineInRange("segment forced expiration time", 8000, 1500, 20000);
            }
        }

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
        mindshaftConfig.v_enabled.set(enabled);
        mindshaftConfig.v_zoom.set(zoom);
        mindshaftConfig.v_zoomfs.set(zoomfs);

        mindshaftConfig.dirtyconfig = true;
        mindshaftConfig.CLIENT_SPEC.save();
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
        Mindshaft.logger.info("zoom: " + Integer.toString(zoom));
        refresh();
    }
}
