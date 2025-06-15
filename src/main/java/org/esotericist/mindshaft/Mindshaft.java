package org.esotericist.mindshaft;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import net.minecraftforge.common.MinecraftForge;

import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import net.minecraftforge.client.event.RenderGuiOverlayEvent;

import org.apache.logging.log4j.LogManager;

import org.apache.logging.log4j.Logger;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod(Mindshaft.MODID)
@Mod.EventBusSubscriber(modid = Mindshaft.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Mindshaft {
    public static final String MODID = "mindshaft";
    public static final String NAME = "Mindshaft";

    public static final Logger logger = LogManager.getLogger();

    private static Player player;

    private static mindshaftRenderer renderer = new mindshaftRenderer();

    private static inputHandler input;

    public static zoomState zoom = new zoomState();

    private static mindshaftScanner scanner = new mindshaftScanner();

    public Mindshaft() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, mindshaftConfig.CLIENT_SPEC);
        input = new inputHandler();
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
        MinecraftForge.EVENT_BUS.register(input);
        Minecraft.getInstance().tell(new assetinit());

    }

    @SubscribeEvent
    public static void onModConfigEvent(final ModConfigEvent configEvent) {
        ModConfig config = configEvent.getConfig();
        if (config != null && config.getSpec() == mindshaftConfig.CLIENT_SPEC) {
            mindshaftConfig.dirtyconfig = true;
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {

        if (mindshaftConfig.dirtyconfig == true) {
            bakeandzoom();
            mindshaftConfig.dirtyconfig = false;
        }
        if (event.phase == TickEvent.Phase.END) {
            Minecraft mc = Minecraft.getInstance();

            player = mc.player;
            Level world = mc.level;
            if (player == null) {
                return;
            }

            if (mindshaftConfig.enabled || zoom.fullscreen) {

                // this adjustment allows the player to be considered at the 'same' Y value
                // whether on a normal block, on farmland (so slightly below normal), or
                // on a slab (half a block above normal)
                int pY = (int) (Math.ceil(player.getY() - (17 / 32D)));
                BlockPos pPos = new BlockPos(((int)player.getX()), pY, ((int)player.getZ()));
                double rawV = player.getDeltaMovement().y;
                int vY = (int) (rawV > 0 ? Math.ceil(rawV) : Math.floor(rawV));

                scanner.setWorld(player.getCommandSenderWorld());
                scanner.setNow(world.getGameTime());
                scanner.setDim(world.dimension().hashCode());
                scanner.processChunks(pY, vY);
                scanner.rasterizeLayers(pPos, vY, renderer, zoom);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void RenderGameOverlayEvent(RenderGuiOverlayEvent.Post event) {
        renderer.doRender(event, player, zoom);
    }
}
