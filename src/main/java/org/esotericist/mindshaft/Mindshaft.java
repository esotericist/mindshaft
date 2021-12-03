package org.esotericist.mindshaft;

import net.minecraft.client.Minecraft;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.minecraftforge.common.MinecraftForge;

import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import net.minecraftforge.client.event.RenderGameOverlayEvent;

import org.apache.logging.log4j.LogManager;

import org.apache.logging.log4j.Logger;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

@Mod(Mindshaft.MODID)
@Mod.EventBusSubscriber(modid = Mindshaft.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Mindshaft {
    public static final String MODID = "mindshaft";
    public static final String NAME = "Mindshaft";
    // public static final String VERSION = "1.0";

    public static final Logger logger = LogManager.getLogger();

    private static PlayerEntity player;

    private static mindshaftRenderer renderer = new mindshaftRenderer();

    private static inputHandler input;

    public static zoomState zoom = new zoomState();

    private static mindshaftScanner scanner = new mindshaftScanner();


    public Mindshaft() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, mindshaftConfig.CLIENT_SPEC);
    }

    public static class assetinit implements Runnable {
        public void run() {
            renderer.initAssets();
        }
    }

    static void bakeandzoom() {
        mindshaftConfig.bakeConfig();
        zoom.initzooms();
    }

    public void setup(FMLClientSetupEvent event) {

        logger.info("setup");
        MinecraftForge.EVENT_BUS.register(this);
        bakeandzoom();
        input = new inputHandler();
        MinecraftForge.EVENT_BUS.register(input);
        Minecraft.getInstance().enqueue(new assetinit());

    }

    @SubscribeEvent
	public static void onModConfigEvent(final ModConfig.ModConfigEvent configEvent) {
        ModConfig config = configEvent.getConfig();
		if ( config != null &&  config.getSpec() == mindshaftConfig.CLIENT_SPEC) {
            //logger.info("config");
            mindshaftConfig.dirtyconfig = true;
        }
	}

    /*

    @SubscribeEvent
    public static void onLoad(final ModConfig.Loading configEvent) {
        ModConfig config = configEvent.getConfig();
		if ( config != null &&  config.getSpec() == mindshaftConfig.CLIENT_SPEC) {
            logger.info("load");
            mindshaftConfig.dirtyconfig = true;
        }
    }

    @SubscribeEvent
    public static void onReload(final ModConfig.Reloading configEvent) {
        ModConfig config = configEvent.getConfig();
		if ( config != null &&  config.getSpec() == mindshaftConfig.CLIENT_SPEC) {
            logger.info("reload");
            mindshaftConfig.dirtyconfig = true;
        }
    }
    */

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        // logger.info("tick");

        if( mindshaftConfig.dirtyconfig == true ) {
            bakeandzoom();
            mindshaftConfig.dirtyconfig = false;
        }
        if (event.phase == TickEvent.Phase.END) {
            // logger.info("tick interior");
            Minecraft mc = Minecraft.getInstance();

            player = mc.player;
            World world = mc.world;
            if (player == null) {
                return;
            }

            if (mindshaftConfig.enabled || zoom.fullscreen) {
                
                // this adjustment allows the player to be considered at the 'same' Y value
                // whether on a normal block, on farmland (so slightly below normal), or
                // on a slab (half a block above normal)
                int pY = (int) (Math.ceil(player.getPosY() - (17 / 32D)));
                BlockPos pPos = new BlockPos(player.getPosX(), pY, player.getPosZ());

                scanner.processChunks(player.getEntityWorld(), pY);
                scanner.rasterizeLayers(world, pPos, renderer, zoom);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void RenderGameOverlayEvent(RenderGameOverlayEvent.Post event) {
        // logger.info("render");
        renderer.doRender(event, player, zoom);
    }
}
