package net.esotericist.mindshaft;


import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.state.IBlockProperties;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldServer;

import net.minecraftforge.fluids.*;


import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Configuration;

import net.minecraftforge.common.config.ConfigManager;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.client.event.RenderGameOverlayEvent;


import org.lwjgl.opengl.GL11;
import org.lwjgl.input.Keyboard;

import org.apache.logging.log4j.Logger;

import java.lang.Math;
import java.util.Arrays;






@Mod(modid = Mindshaft.MODID, name = Mindshaft.NAME, //version = Mindshaft.VERSION, 
    clientSideOnly = true, dependencies = "after:forge@[14.23.4.2705,)")
public class Mindshaft
{
    public static final String MODID = "mindshaft";
    public static final String NAME = "Mindshaft";
    //public static final String VERSION = "1.0";

    private static Logger logger;
    
    public static KeyBinding[] keyBindings;
    private boolean[] pressed;
    
    //public static mindshaftConfig config;
        
    private TextureManager textureManager;
    private DynamicTexture mapTexture;
    private ResourceLocation location;
    private ResourceLocation playericon;
    private int[] mapTextureData;
    private EntityPlayer player;
    
    private int layer = 0;
    
    private int startX = 0;
    private int startZ = 0;
    private int lastX = 0;
    private int lastZ = 0;
    
    private float nextlayer = 0;
    
    private zoomstate[] zoomlist; 
    
    
    class zoomstate {
        public int x;
        public int z;
        public int w;
        public int h;
        public double minU;
        public double minV;
        public double maxU;
        public double maxV;
        public float layerrate;
        public int overdraw;
        
        public void setZoomState(int x, int z, int w, int h, 
            double minU, double minV, double maxU, double maxV, float layerrate, int overdraw) {
            
            this.x = x;
            this.z = z;
            this.w = w;
            this.h = h;
            this.minU = minU;
            this.minV = minV;
            this.maxU = maxU;
            this.maxV = maxV;
            this.layerrate = layerrate;
            this.overdraw = overdraw;
        }
    }
    
    
    
    // not currently used, keeping it around in case that changes again
    private boolean isLiquid(World world, BlockPos pos) {
        Block blockID = world.getBlockState(pos).getBlock();
        boolean liquid = false;
        if (Block.isEqualTo(blockID, Blocks.WATER) || Block.isEqualTo(blockID, Blocks.FLOWING_WATER)) {
            liquid = true;
        }
        if (Block.isEqualTo(blockID, Blocks.LAVA) || Block.isEqualTo(blockID, Blocks.FLOWING_LAVA)) {
            liquid = true;
        }
        if (blockID instanceof IFluidBlock) {
            liquid = true;
        }
        return liquid;    
    }
    
    private int clamp (int value, int min, int max) {
        return Math.min(Math.max(value, min), max);
    }
    
    private boolean isLit(World world, BlockPos pos) {
        
        if (((player.getEntityWorld().getLightFor(EnumSkyBlock.BLOCK, pos) > 0) 
            || (player.getEntityWorld().provider.isSurfaceWorld())
            && (player.getEntityWorld().getLightFor(EnumSkyBlock.SKY,pos) > 0))
            
            ) {
            return true;
        }
        return false;
    }
    
    private void processKeys() {
        // binding 0: enable/disable toggle
        if (keyBindings[0].isPressed() && !pressed[0]) {
            if (mindshaftConfig.enabled) {
                mindshaftConfig.setEnabled(false);
            } else {
                mindshaftConfig.setEnabled(true);
            }
            pressed[0] = true;
        }
        if (!keyBindings[0].isPressed() && pressed[0]) {
            pressed[0] = false;
        }

        // binding 2: zoom in
        if (keyBindings[2].isPressed() && !pressed[2]) {
            mindshaftConfig.setZoom(mindshaftConfig.zoom + 1, zoomlist.length);
            pressed[2] = true;
        }
        if (!keyBindings[2].isPressed() && pressed[2]) {
            pressed[2] = false;
        }

        // binding 3: zoom out
        if (keyBindings[3].isPressed() && !pressed[3]) {
            mindshaftConfig.setZoom(mindshaftConfig.zoom - 1, zoomlist.length);
            pressed[3] = true;
        }
        if (!keyBindings[3].isPressed() && pressed[3]) {
            pressed[3] = false;
        }
        
    }
    
    private void processLayer(World world, BlockPos playerPos) {

        int y = layer - 15;
        int overdraw = zoomlist[mindshaftConfig.zoom].overdraw;
        int minX = zoomlist[mindshaftConfig.zoom].x - overdraw;
        int minZ = zoomlist[mindshaftConfig.zoom].z - overdraw;
        int maxX = zoomlist[mindshaftConfig.zoom].w / 2 + overdraw;
        int maxZ = zoomlist[mindshaftConfig.zoom].h / 2 + overdraw;

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {

                int dist = 0;
                int color = 0;
                int red = 0;
                int blue = 0;
                int green = 0;
                int oldcolor = 0;
                int oldred = 0;
                int oldblue = 0;
                int oldgreen = 0;
                
                int diffX = 0;
                int diffZ = 0;
                
                diffX = playerPos.getX() - startX;
                diffZ = playerPos.getZ() - startZ;
                
                int adjX = x + diffX;
                int adjZ = z + diffZ;
                
                if ((adjX < minX) || (adjX > maxX) || (adjZ < minZ) || (adjZ > maxZ)) {
                    continue;
                }


                BlockPos pos = new BlockPos(playerPos.getX()+x,playerPos.getY()+y,playerPos.getZ()+z);
                IBlockState state = world.getBlockState(pos);
                Block blockID = state.getBlock();
                boolean solid = true;
                boolean intangible = false;
                boolean empty = false;
                boolean lit = false;
                int intensity = 0;
                dist = Math.abs(y);

                if (y>1) {
                    dist--;
                }
                if (y==1) {
                    dist = 0;
                }
                if (dist>10) {
                    dist = 10;
                }
                
                lit = isLit(world, pos);
                
                if (state.isOpaqueCube() != true) {
                    solid=false;
                    
                    if(state.getCollisionBoundingBox(world,pos) == null) {
                        intangible = true;
                    
                        if(blockID == Blocks.AIR) {
                        empty = true;
                        }
                    }
                }
                
                if (!intangible || !lit) {
                    if (dist > 0) {
                        intensity = (int) (11 - dist);
                    } else {
                        intensity = 17;
                    }
                    if (!solid && lit) {
                        intensity = intensity - 3;
                    }
                }
                
                intensity = Math.max(intensity,0);

                green = green + intensity;
                if (empty && lit) {
                    if (y < 0 ) {
                        red = red + (int) (15-dist);
                    }
                    if (y > 1 ) {
                        blue = blue + (int) (16-dist);
                    }
                }
                
            
                int offset = (adjX+127)+((adjZ+127)*256);
            
                oldcolor = mapTextureData[offset];
                oldblue = oldcolor & 0xFF;
                oldgreen = (oldcolor >> 8 ) & 0xFF;
                oldred = (oldcolor >> 16 ) & 0xFF;

                color = clamp(red+oldred,0,255) << 16 | clamp(green+oldgreen,0,255) << 8 | clamp(blue+oldblue,0,255);
                mapTextureData[offset] = color;
            }
        }
    
    }
    
    private void processBlocks(World world, BlockPos playerPos) {
    
        nextlayer = nextlayer + zoomlist[mindshaftConfig.zoom].layerrate;
        
        while (nextlayer >= 1.0F) {
        
            nextlayer = nextlayer - 1.0F;
            
            if (layer >= 32) {
                lastX = startX;
                lastZ = startZ;
                mapTexture.updateDynamicTexture();
                layer = 0;
                for (int i = 0; i < this.mapTextureData.length; ++i) {
                    mapTextureData[i] = 0x002200;
                }
                
                startX = playerPos.getX();
                startZ = playerPos.getZ();
                
            }
            
            processLayer(world, playerPos);
            layer++;
        
        }
        
        
    }
    

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            Minecraft mc = Minecraft.getMinecraft();

             player = mc.player;
            World world = mc.world;
            if (player == null) {
                return;
            }
            BlockPos playerPos = new BlockPos(
            Math.floor(player.posX), 
            Math.floor(player.posY), 
            Math.floor(player.posZ));
            
            processKeys();

            
            if (mindshaftConfig.enabled) {
                processBlocks(world, playerPos);
            }
        }
            
    }


    @SubscribeEvent //(priority = EventPriority.NORMAL)
    public void eventHandler(RenderGameOverlayEvent.Post event) {
    
        Minecraft mc = Minecraft.getMinecraft();
        
        if ((!mindshaftConfig.enabled) || (player == null )) {
            return;
        }
        
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder renderer = tessellator.getBuffer();

        textureManager.bindTexture(location);
        
        double fudge = 0.00390625; // 1 / 256;
        
        double offsetU = (player.posX - lastX) * fudge;
        double offsetV = (player.posZ - lastZ) * fudge;
        
        double screenX = event.getResolution().getScaledWidth();
        double screenY = event.getResolution().getScaledHeight();
        
        double mapsize = mindshaftConfig.getMapsize() * screenY;

        double offsetX = mindshaftConfig.getOffsetX() * screenX;
        double offsetY = mindshaftConfig.getOffsetY() * screenY;    
        
        double leftX;
        double rightX;
        
        double minX;// = 0.0;
        double minY;// = 0.0;
        double maxX;// = event.getResolution().getScaledHeight() * 0.20; // 127.0;
        double maxY;// = maxX; // 127.0;

        if (mindshaftConfig.offsetfromleft) {
            minX = offsetX;
            maxX = offsetX + mapsize;
        } else {
            maxX = screenX - offsetX;
            minX = maxX - mapsize;
        }
        
        if (mindshaftConfig.offsetfromtop) {
        
            minY = offsetY;
            maxY = offsetY + mapsize;
        } else {
            maxY = screenY - offsetY;
            minY = maxY - mapsize;

        }
        
        
        
        
        double minU = zoomlist[mindshaftConfig.zoom].minU + offsetU; // 0.0;
        double minV = zoomlist[mindshaftConfig.zoom].minV + offsetV; // 0.0;
        double maxU = zoomlist[mindshaftConfig.zoom].maxU + offsetU; // 1.0;
        double maxV = zoomlist[mindshaftConfig.zoom].maxV + offsetV; // 1.0;
        

        renderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        renderer.pos(minX, maxY, 0).tex(minU, maxV).endVertex();
        renderer.pos(maxX, maxY, 0).tex(maxU, maxV).endVertex();
        renderer.pos(maxX, minY, 0).tex(maxU, minV).endVertex();
        renderer.pos(minX, minY, 0).tex(minU, minV).endVertex();
        tessellator.draw();
        
        minU = 0.0;
        minV = 0.0;
        maxU = 1.0;
        maxV = 1.0;

        
        GlStateManager.pushMatrix();

        textureManager.bindTexture(playericon);

        GlStateManager.enableAlpha();

        GlStateManager.translate(minX + (mapsize /2)-(mapsize/64),minY + (mapsize / 2)-(mapsize/64), 0);
        
        
        minX = 0;
        minY = 0;
        maxX = 16;
        maxY = 16;

        GlStateManager.rotate(180 + player.getRotationYawHead(), 0, 0, 1);
        GlStateManager.translate(-7.5, -7.5, 0);
        
        renderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        renderer.pos(minX, maxY, 0).tex(minU, maxV).endVertex();
        renderer.pos(maxX, maxY, 0).tex(maxU, maxV).endVertex();
        renderer.pos(maxX, minY, 0).tex(maxU, minV).endVertex();
        renderer.pos(minX, minY, 0).tex(minU, minV).endVertex();
        tessellator.draw();

        GlStateManager.disableAlpha();
        GlStateManager.popMatrix();
       
    }

    


    @SubscribeEvent
    public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
    
        if (event.getModID().equals(Mindshaft.MODID)) {
        logger.debug("enabled: ", mindshaftConfig.enabled);
            ConfigManager.sync(Mindshaft.MODID, Config.Type.INSTANCE);
        logger.debug("enabled: ", mindshaftConfig.enabled);
        }
    }


/*
    @SubscribeEvent
    public void onConfigChangedEvent(ConfigChangedEvent.OnConfigChangedEvent event)
    {
        if (event.getModID().equals(MODID))
        {
            ConfigManager.sync(MODID, ConfigChangedEvent.Type.INSTANCE);
        }
    }

*/
    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        logger = event.getModLog();
        
        MinecraftForge.EVENT_BUS.register(this);
        
        
    }

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        keyBindings = new KeyBinding[4];
        pressed = new boolean[4];
        
        keyBindings[0] = new KeyBinding("key.toggle.desc", Keyboard.KEY_NUMPAD1, "key.mindshaft.category");
        keyBindings[1] = new KeyBinding("key.location.desc", Keyboard.KEY_NUMPAD2, "key.mindshaft.category");
        keyBindings[2] = new KeyBinding("key.zoomin.desc", Keyboard.KEY_NUMPAD3, "key.mindshaft.category");
        keyBindings[3] = new KeyBinding("key.zoomout.desc", Keyboard.KEY_NUMPAD4, "key.mindshaft.category");
        
        for (int i = 0; i < keyBindings.length; ++i) {
            ClientRegistry.registerKeyBinding(keyBindings[i]);
            pressed[i] = false;
        }
        
        logger.debug("enabled: ", mindshaftConfig.enabled);
        ConfigManager.sync(MODID, Config.Type.INSTANCE);
        logger.debug("enabled: ", mindshaftConfig.enabled);
        
        
        zoomlist = new zoomstate[3];
        Arrays.setAll(zoomlist, (i) -> new zoomstate());
        
        zoomlist[0].setZoomState(-96,-96,192,192,0.125D,0.125D,0.875D,0.875D,1.20F, 30);
        zoomlist[1].setZoomState(-63,-63,128,128,0.25D,0.25D,0.75D,0.75D, 3.5F, 24);
        zoomlist[2].setZoomState(-31,-31,64,64,0.375D,0.375D,0.625D,0.625D, 8.0F, 16);
    }


    @EventHandler
    public void PostInit(FMLPostInitializationEvent event) {
        mapTexture = new DynamicTexture(256, 256);
        mapTextureData = mapTexture.getTextureData();

        textureManager = Minecraft.getMinecraft().getTextureManager();
        location = textureManager.getDynamicTextureLocation("mindshafttexture", mapTexture);
        playericon = new ResourceLocation("mindshaft","textures/playericon.png");

        for (int i = 0; i < this.mapTextureData.length; ++i) {
            mapTextureData[i] = 0x002200;
        }
        mapTexture.updateDynamicTexture();
    }
    
}
