package org.esotericist.mindshaft;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraftforge.client.gui.ForgeIngameGui;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.ResourceLocation;
import com.mojang.math.Quaternion;
import net.minecraftforge.client.event.RenderGameOverlayEvent;

import org.lwjgl.opengl.GL11;

class mindshaftRenderer {

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
        mapTexture.upload();
    }

    public void setTextureValue(int x, int y, int val) {
        mapTexture.getPixels().setPixelRGBA(x, y, val);
    }

    /*
     * public int[] getTextureData() { return mapTextureData; }
     */

    /*
     * public int getTextureValue(int pos) { return mapTextureData[pos]; }
     */

    public int getTextureValue(int x, int y) {
        return mapTexture.getPixels().getPixelRGBA(x, y);
        // return getTextureValue(x + (y * 256));
    }

    public void updatePos(int x, int z) {
        lastX = x;
        lastZ = z;
    }

    public void initAssets() {

        textureManager = Minecraft.getInstance().getTextureManager();

        mapTexture = new DynamicTexture(texturesize, texturesize, true); // DynamicTexture(texturesize, texturesize);
        // nativeTexture = mapTexture.getTextureData();

        mapresource = textureManager.register("mindshafttexture", mapTexture);
        playericon = new ResourceLocation("mindshaft", "textures/playericon.png");

        for (int i = 0; i < texturesize; i++) {
            for (int j = 0; j < texturesize; j++) {
                setTextureValue(i, j, 0x002200);
            }
        }
        refreshTexture();
    }

    public static void enableAlpha( float alpha) {
		RenderSystem.enableBlend();
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
		RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    }

    
    public void doRender(RenderGameOverlayEvent.Post event, Player player, zoomState zoom) {

        if ((!mindshaftConfig.enabled) && !(zoom.fullscreen) || (player == null)) {
            return;
        }

        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder renderer = tessellator.getBuilder();
        PoseStack stack = event.getMatrixStack();

        stack.pushPose();

		RenderSystem.disableBlend();
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, mapresource);

        double offsetU = ((player.getX()) - (lastX * 16)) * texelsize;
        double offsetV = ((player.getZ()) - (lastZ * 16)) * texelsize;

        double screenX = event.getWindow().getGuiScaledWidth();
        double screenY = event.getWindow().getGuiScaledHeight();

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

        float minU = (float) (currentzoom.minU + offsetU); // 0.0;
        float minV = (float) (currentzoom.minV + offsetV); // 0.0;
        float maxU = (float) (currentzoom.maxU + offsetU); // 1.0;
        float maxV = (float) (currentzoom.maxV + offsetV); // 1.0;

        // Mindshaft.logger.info("u: " + minU + "~" + maxU + ", v: " + minV + "~" +
        // maxV);

        renderer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

        renderer.vertex(minX, maxY, 0).uv(minU, maxV).endVertex();
        renderer.vertex(maxX, maxY, 0).uv(maxU, maxV).endVertex();
        renderer.vertex(maxX, minY, 0).uv(maxU, minV).endVertex();
        renderer.vertex(minX, minY, 0).uv(minU, minV).endVertex();
        tessellator.end();


        stack.popPose();
        stack.pushPose();

		RenderSystem.enableBlend();
		RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, mindshaftConfig.getCursorOpacity(zoom.fullscreen));
        RenderSystem.setShaderTexture(0, playericon);

        stack.translate(minX + (mapsize / 2), minY + (mapsize / 2), 0.0d);
        double centeroffset = cursorsize / 16.0;
        //Quaternion rotation = Vector3f.ZP.rotationDegrees(180 + player.getYHeadRot());

        stack.mulPose(Quaternion.fromXYZ(0f, 0f, (180 + player.getYHeadRot()) * ((float)Math.PI / 180F)));
        stack.translate(-((cursorsize - centeroffset) / 2), -((cursorsize - centeroffset) / 2), 0);
        ForgeIngameGui.blit(stack, 0, 0, 0f, 0f, cursorsize, cursorsize, cursorsize, cursorsize);

        stack.popPose();
    }
}