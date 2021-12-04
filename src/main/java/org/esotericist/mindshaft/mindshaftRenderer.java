package org.esotericist.mindshaft;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraftforge.client.gui.ForgeIngameGui;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;
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
        mapTexture.updateDynamicTexture();
    }

    public void setTextureValue(int x, int y, int val) {
        mapTexture.getTextureData().setPixelRGBA(x, y, val);
    }

    /*
     * public int[] getTextureData() { return mapTextureData; }
     */

    /*
     * public int getTextureValue(int pos) { return mapTextureData[pos]; }
     */

    public int getTextureValue(int x, int y) {
        return mapTexture.getTextureData().getPixelRGBA(x, y);
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

        mapresource = textureManager.getDynamicTextureLocation("mindshafttexture", mapTexture);
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
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, alpha);
		RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    }

	public static void disableAlpha()
	{
		RenderSystem.disableBlend();
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
	}
    
    public void doRender(RenderGameOverlayEvent.Post event, PlayerEntity player, zoomState zoom) {

        if ((!mindshaftConfig.enabled) && !(zoom.fullscreen) || (player == null)) {
            return;
        }

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder renderer = tessellator.getBuffer();
        MatrixStack stack = event.getMatrixStack();

        textureManager.bindTexture(mapresource);

        double offsetU = ((player.getPosX()) - (lastX * 16)) * texelsize;
        double offsetV = ((player.getPosZ()) - (lastZ * 16)) * texelsize;

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

        float minU = (float) (currentzoom.minU + offsetU); // 0.0;
        float minV = (float) (currentzoom.minV + offsetV); // 0.0;
        float maxU = (float) (currentzoom.maxU + offsetU); // 1.0;
        float maxV = (float) (currentzoom.maxV + offsetV); // 1.0;

        // Mindshaft.logger.info("u: " + minU + "~" + maxU + ", v: " + minV + "~" +
        // maxV);

        disableAlpha();

        renderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

        renderer.pos(minX, maxY, 0).tex(minU, maxV).endVertex();
        renderer.pos(maxX, maxY, 0).tex(maxU, maxV).endVertex();
        renderer.pos(maxX, minY, 0).tex(maxU, minV).endVertex();
        renderer.pos(minX, minY, 0).tex(minU, minV).endVertex();
        tessellator.draw();


        stack.push();

        textureManager.bindTexture(playericon); // .bindTexture(playericon);

        enableAlpha(mindshaftConfig.getCursorOpacity(zoom.fullscreen));

        stack.translate(minX + (mapsize / 2), minY + (mapsize / 2), 0.0d);
        double centeroffset = cursorsize / 16.0;
        Quaternion rotation = Vector3f.ZP.rotationDegrees(180 + player.getRotationYawHead());

        stack.rotate(rotation);
        stack.translate(-((cursorsize - centeroffset) / 2), -((cursorsize - centeroffset) / 2), 0);
        ForgeIngameGui.blit(stack, 0, 0, 0f, 0f, cursorsize, cursorsize, cursorsize, cursorsize);

        stack.pop();
        disableAlpha();
    }
}