package org.esotericist.mindshaft;

import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

import net.minecraft.client.Minecraft;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.client.event.RenderGameOverlayEvent;

import org.lwjgl.opengl.GL11;

class mindshaftRenderer {

    public boolean initialized = false;
    private TextureManager textureManager;
    private DynamicTexture mapTexture;
    private ResourceLocation mapresource;
    private ResourceLocation playericon;

    private int lastX = 0;
    private int lastZ = 0;

    // 256 x 256 for the map texture
    // it's possible to change this but there's not a lot of benefit
    private static final int texturesize = 256;

    // the size of a single texel in the underlying texture
    // primarily used for sub-texel offsets while the player is moving
    private static final double texelsize = 1.0 / texturesize;

    public DynamicTexture getTexture() {
        return mapTexture;
    }

    public void refreshTexture() {
        mapTexture.updateDynamicTexture();
    }

    public void setTextureValue(int x, int y, int val) {
        mapTexture.getTextureData().setPixelRGBA(x, y, val);
    }

    /*
    public int[] getTextureData() {
        return mapTextureData;
    }
    */

    /*
    public int getTextureValue(int pos) {
        return mapTextureData[pos];
    }
    */

    public int getTextureValue(int x, int y) {
        return mapTexture.getTextureData().getPixelRGBA(x, y);
        //return getTextureValue(x + (y * 256));
    }

    public void updatePos(int x, int z) {
        lastX = x;
        lastZ = z;
    }

    public void initAssets() {

        textureManager = Minecraft.getInstance().getTextureManager();

        mapTexture = new DynamicTexture(texturesize, texturesize, true); // DynamicTexture(texturesize, texturesize);
        // nativeTexture = mapTexture.getTextureData();



        mapresource = textureManager.getDynamicTextureLocation("mindshafttexture", mapTexture);
        playericon = new ResourceLocation("mindshaft", "textures/playericon.png");

        for (int i = 0; i < texturesize; i++) {
            for( int j = 0; j < texturesize; j++) {
                setTextureValue(i, j, 0x002200);
            }
        }
        refreshTexture();

        initialized = true;
    }

    public void doRender(RenderGameOverlayEvent.Post event, PlayerEntity player, zoomState zoom) {

        if ((!mindshaftConfig.enabled) && !(zoom.fullscreen) || (player == null)) {
            return;
        }

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder renderer = tessellator.getBuffer();

        textureManager.bindTexture(mapresource);

        double offsetU = ((player.posX) - (lastX * 16) ) * texelsize;
        double offsetV = ((player.posZ) - (lastZ * 16) ) * texelsize;

        // Mindshaft.logger.info("U " + offsetU + ", V " + offsetV);

        double screenX = event.getWindow().getScaledWidth();
        double screenY = event.getWindow().getScaledHeight();

        double mapsize = mindshaftConfig.getMapsize() * screenY;
        double fsmapsize = mindshaftConfig.getFSMapsize() * screenY;

        double offsetX = mindshaftConfig.getOffsetX() * screenX;
        double offsetY = mindshaftConfig.getOffsetY() * screenY;

        double minX;
        double minY;
        double maxX;
        double maxY;

        int cursorsize = mindshaftConfig.cursorsize;

        if (zoom.fullscreen == true) {
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

        zoomSpec currentzoom = zoom.getZoomSpec();

        double minU = currentzoom.minU + offsetU; // 0.0;
        double minV = currentzoom.minV + offsetV; // 0.0;
        double maxU = currentzoom.maxU + offsetU; // 1.0;
        double maxV = currentzoom.maxV + offsetV; // 1.0;

        // Mindshaft.logger.info("u: " + minU + "~" + maxU + ", v: " + minV + "~" + maxV);

        GlStateManager.disableAlphaTest();
        //GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA); //disableAlpha();
        GlStateManager.disableBlend();
        GlStateManager.clearCurrentColor(); //.resetColor();
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

        // GlStateManager //.color(1f, 1f, 1f, mindshaftConfig.getCursorOpacity(zoom.fullscreen));

        textureManager.bindTexture(playericon);  //.bindTexture(playericon);

        GlStateManager.enableAlphaTest();

        GlStateManager.translated(minX + (mapsize / 2), minY + (mapsize / 2), 0.0d);

        double cminX = 0;
        double cminY = 0;
        double cmaxX = cursorsize;
        double cmaxY = cursorsize;

        double centeroffset = cursorsize / 16.0;

        GlStateManager.rotated(180 + player.getRotationYawHead(), 0, 0, 1);
        GlStateManager.translated(-((cmaxX - centeroffset) / 2), -((cmaxY - centeroffset) / 2), 0);

        renderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        renderer.pos(cminX, cmaxY, 0).tex(cminU, cmaxV).endVertex();
        renderer.pos(cmaxX, cmaxY, 0).tex(cmaxU, cmaxV).endVertex();
        renderer.pos(cmaxX, cminY, 0).tex(cmaxU, cminV).endVertex();
        renderer.pos(cminX, cminY, 0).tex(cminU, cminV).endVertex();
        tessellator.draw();

        // GlStateManager.color(1, 1, 1, 1);
        //GlStateManager.disableAlpha();

        GlStateManager.popMatrix();
        GlStateManager.disableBlend();
    }
}