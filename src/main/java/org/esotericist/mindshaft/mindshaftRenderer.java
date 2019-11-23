package org.esotericist.mindshaft;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

import net.minecraft.client.Minecraft;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.client.event.RenderGameOverlayEvent;

import org.lwjgl.opengl.GL11;


public class mindshaftRenderer {

    private TextureManager textureManager;
    private DynamicTexture mapTexture;
    private int[] mapTextureData;
    private ResourceLocation mapresource;
    private ResourceLocation playericon;

    private int lastX = 0;
    private int lastZ = 0;

    public DynamicTexture getTexture() {
        return mapTexture;
    }

    public void refreshTexture() {
        mapTexture.updateDynamicTexture();
    }

    public void setTextureValue(int pos, int val) {
        mapTextureData[pos] = val;
    }

    public int[] getTextureData() {
        return mapTextureData;
    }

    public int getTextureData(int pos) {
        return mapTextureData[pos];
    }

    public void updatePos( int x, int z) {
        lastX = x;
        lastZ = z;
    }

    public void initAssets() {
        mapTexture = new DynamicTexture(256, 256);
        mapTextureData = mapTexture.getTextureData();
        textureManager = Minecraft.getMinecraft().getTextureManager();

        mapresource = textureManager.getDynamicTextureLocation("mindshafttexture", mapTexture);
        playericon = new ResourceLocation("mindshaft","textures/playericon.png");

        for (int i = 0; i < getTextureData().length; ++i) {
            setTextureValue(i, 0x002200);
        }
        refreshTexture();
    }

    public void doRender(RenderGameOverlayEvent.Post event, EntityPlayer player ) {
    
        if ((!mindshaftConfig.enabled) && !( Mindshaft.zoom.fullscreen ) || (player == null )) {
            return;
        }
        
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder renderer = tessellator.getBuffer();

        textureManager.bindTexture(mapresource);
        
        double fudge = 1 / 256D; // the tx size of the underlying texture

        double offsetU = (player.posX - lastX - 1) * fudge;
        double offsetV = (player.posZ - lastZ - 1) * fudge;

        double screenX = event.getResolution().getScaledWidth();
        double screenY = event.getResolution().getScaledHeight();
        
        double mapsize = mindshaftConfig.getMapsize() * screenY;
        double fsmapsize = mindshaftConfig.getFSMapsize() * screenY;

        double offsetX = mindshaftConfig.getOffsetX() * screenX;
        double offsetY = mindshaftConfig.getOffsetY() * screenY;    
        
        double minX;// = 0.0;
        double minY;// = 0.0;
        double maxX;// = event.getResolution().getScaledHeight() * 0.20; // 127.0;
        double maxY;// = maxX; // 127.0;

        int cursorsize = mindshaftConfig.cursorsize;

        if ( Mindshaft.zoom.fullscreen == true) {
            offsetX = (screenX - fsmapsize) / 2;
            offsetY = (screenY - fsmapsize) / 2;
            mapsize = fsmapsize;
            cursorsize = mindshaftConfig.cursorsizefs;
        }

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

        zoomspec currentzoom = Mindshaft.zoom.getZoomSpec();

        double minU = currentzoom.minU + offsetU; // 0.0;
        double minV = currentzoom.minV + offsetV; // 0.0;
        double maxU = currentzoom.maxU + offsetU; // 1.0;
        double maxV = currentzoom.maxV + offsetV; // 1.0;
        
        GlStateManager.disableAlpha();
        GlStateManager.disableBlend();
        GlStateManager.resetColor();
        GlStateManager.disableLighting();

        renderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

        renderer.pos(minX, maxY, 0).tex(minU, maxV).endVertex();
        renderer.pos(maxX, maxY, 0).tex(maxU, maxV).endVertex();
        renderer.pos(maxX, minY, 0).tex(maxU, minV).endVertex();
        renderer.pos(minX, minY, 0).tex(minU, minV).endVertex();
        tessellator.draw();

        GlStateManager.enableBlend();

        double cminU = 0.0;
        double cminV = 0.0;
        double cmaxU = 1.0;
        double cmaxV = 1.0;
        
        GlStateManager.pushMatrix();

        GlStateManager.color(1f,1f,1f, mindshaftConfig.getCursorOpacity(Mindshaft.zoom.fullscreen));

        textureManager.bindTexture(playericon);

        GlStateManager.enableAlpha();

        GlStateManager.translate(minX + (mapsize / 2 ),
                                 minY + (mapsize / 2), 0);
        
        double cminX = 0;
        double cminY = 0;
        double cmaxX = cursorsize;
        double cmaxY = cursorsize;

        double centeroffset = cursorsize / 16.0;

        GlStateManager.rotate(180 + player.getRotationYawHead(), 0, 0, 1);
        GlStateManager.translate( -( (cmaxX - centeroffset) / 2 ), -( (cmaxY - centeroffset) / 2), 0);
        
        renderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        renderer.pos(cminX, cmaxY, 0).tex(cminU, cmaxV).endVertex();
        renderer.pos(cmaxX, cmaxY, 0).tex(cmaxU, cmaxV).endVertex();
        renderer.pos(cmaxX, cminY, 0).tex(cmaxU, cminV).endVertex();
        renderer.pos(cminX, cminY, 0).tex(cminU, cminV).endVertex();
        tessellator.draw();

        GlStateManager.color(1,1,1,1);
        GlStateManager.disableAlpha();
        GlStateManager.popMatrix();
    }
}