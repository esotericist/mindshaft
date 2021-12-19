package org.esotericist.mindshaft;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.client.event.RenderGameOverlayEvent;

import org.apache.logging.log4j.Logger;

@Mod(modid = Mindshaft.MODID, name = Mindshaft.NAME, // version = Mindshaft.VERSION,
        clientSideOnly = true, dependencies = "after:forge@[14.23.4.2705,)")
public class Mindshaft {
    public static final String MODID = "mindshaft";
    public static final String NAME = "Mindshaft";
    // public static final String VERSION = "1.0";

    static Logger logger;

    private static EntityPlayer player;

    private static mindshaftRenderer renderer = new mindshaftRenderer();

    private static inputHandler input = new inputHandler();

    private static zoomState zoom = new zoomState();

    private static mindshaftScanner scanner = new mindshaftScanner();

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            Minecraft mc = Minecraft.getMinecraft();

            player = mc.player;
            World world = mc.world;
            if (player == null) {
                return;
            }

            input.processKeys(zoom);

            if (mindshaftConfig.enabled || zoom.fullscreen) {

                // this adjustment allows the player to be considered at the 'same' Y value
                // whether on a normal block, on farmland (so slightly below normal), or
                // on a slab (half a block above normal)
                int pY = (int) (Math.ceil(player.posY - (17 / 32D)));
                BlockPos pPos = new BlockPos(player.posX, pY, player.posZ);
                double rawV = player.posY - player.prevPosY;
                int vY = (int) (rawV > 0 ? Math.ceil(rawV) : Math.floor(rawV));

                scanner.setWorld(player.getEntityWorld());
                scanner.setNow(world.getTotalWorldTime());
                scanner.setDim(world.provider.getDimension());
                scanner.processChunks(pY, vY);
                scanner.rasterizeLayers(pPos, vY, renderer, zoom);
            }
        }
    }

    @SubscribeEvent // (priority = EventPriority.NORMAL)
    public void eventHandler(RenderGameOverlayEvent.Post event) {
        renderer.doRender(event, player, zoom);
    }

    @SubscribeEvent
    public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {

        if (event.getModID().equals(Mindshaft.MODID)) {
            ConfigManager.sync(Mindshaft.MODID, Config.Type.INSTANCE);
            zoom.initzooms();
        }
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();

        MinecraftForge.EVENT_BUS.register(this);
    }

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
}
