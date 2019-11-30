package org.esotericist.mindshaft;

import net.minecraft.client.Minecraft;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

import net.minecraftforge.common.MinecraftForge;

import net.minecraftforge.fml.client.event.ConfigChangedEvent;

import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.TextureStitchEvent;

import org.apache.logging.log4j.LogManager;

import org.apache.logging.log4j.Logger;

@Mod(Mindshaft.MODID)
public class Mindshaft {
    public static final String MODID = "mindshaft";
    public static final String NAME = "Mindshaft";
    // public static final String VERSION = "1.0";

    public static final Logger logger = LogManager.getLogger();

    private static PlayerEntity player;

    public static mindshaftConfig config = new mindshaftConfig();

    private static mindshaftRenderer renderer = new mindshaftRenderer();

    private static inputHandler input;

    public static zoomState zoom = new zoomState();

    private static mindshaftScanner scanner = new mindshaftScanner();

    public Mindshaft() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
    }


    public void setup(FMLClientSetupEvent event ) {

        MinecraftForge.EVENT_BUS.register(this);

        input = new inputHandler();
        MinecraftForge.EVENT_BUS.register(input); 
        zoom.initzooms();
        // logger.info("setup");

    }

    @SubscribeEvent
    public void textureStichEvent(TextureStitchEvent.Post event) {
        logger.info("stitch");
        renderer.initAssets();
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        // logger.info("tick");
        if (event.phase == TickEvent.Phase.END) {
            // logger.info("tick interior");
            Minecraft mc = Minecraft.getInstance();

            player = mc.player;
            World world = mc.world;
            if (player == null) {
                return;
            }

            //input.processKeys(zoom);

            if (mindshaftConfig.enabled || zoom.fullscreen) {

                scanner.processChunks(player.getEntityWorld(), player.posY);
                scanner.rasterizeLayers(world, player, renderer, zoom);
                //scanner.processBlocks(world, player, renderer, zoom);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void RenderGameOverlayEvent(RenderGameOverlayEvent.Post event) {
        if(! renderer.initialized) {
            logger.info("init");
            renderer.initAssets();
        }
        // logger.info("render");
        renderer.doRender(event, player, zoom);
    }

    @SubscribeEvent
    public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {

        if (event.getModID().equals(Mindshaft.MODID)) {
            //ConfigManager.sync(Mindshaft.MODID, Config.Type.INSTANCE);
            zoom.initzooms();
        }
    }

    /*
    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();

    }
    */

    /*
    @EventHandler
    public void init(FMLInitializationEvent event) {
        input.initBindings();

        ConfigManager.sync(MODID, Config.Type.INSTANCE);

        zoom.initzooms();
    }

    @EventHandler
    public void PostInit(FMLPostInitializationEvent event) {

        renderer.initAssets();
    }
    */
}
