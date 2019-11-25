package org.esotericist.mindshaft;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;

import java.lang.Math;

class mindshaftScanner {

    private int layer = 0;

    private int startX = 0;
    private int startZ = 0;

    private float nextlayer = 0;

    private int clamp(int value, int min, int max) {
        return Math.min(Math.max(value, min), max);
    }

    private boolean isLit(World world, EntityPlayer player, BlockPos pos) {

        if (((player.getEntityWorld().getLightFor(EnumSkyBlock.BLOCK, pos) > 0)
                || (player.getEntityWorld().provider.isSurfaceWorld())
                        && (player.getEntityWorld().getLightFor(EnumSkyBlock.SKY, pos) > 0))

        ) {
            return true;
        }
        return false;
    }

    public void processLayer(World world, EntityPlayer player, BlockPos playerPos, mindshaftRenderer renderer,
            zoomState zoom) {

        int y = layer - 15;
        zoomSpec curzoom = Mindshaft.zoom.getZoomSpec();
        int overdraw = curzoom.overdraw;
        int minX = curzoom.x - overdraw;
        int minZ = curzoom.z - overdraw;
        int maxX = curzoom.w / 2 + overdraw;
        int maxZ = curzoom.h / 2 + overdraw;

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

                BlockPos pos = new BlockPos(playerPos.getX() + x, player.posY - (17 / 32D) + y, playerPos.getZ() + z);
                IBlockState state = world.getBlockState(pos);
                Block blockID = state.getBlock();
                boolean solid = true;
                boolean intangible = false;
                boolean empty = false;
                boolean lit = false;
                int intensity = 0;
                dist = Math.abs(y);

                if (y > 1) {
                    dist--;
                }
                if (y == 1) {
                    dist = 0;
                }
                if (dist > 10) {
                    dist = 10;
                }

                lit = isLit(world, player, pos);

                if (state.isOpaqueCube() != true) {
                    solid = false;

                    if (state.getCollisionBoundingBox(world, pos) == null) {
                        intangible = true;

                        if (blockID == Blocks.AIR) {
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

                intensity = Math.max(intensity, 0);

                green = green + intensity;
                if (empty && lit) {
                    if (y < 0) {
                        red = red + (int) (15 - dist);
                    }
                    if (y > 1) {
                        blue = blue + (int) (16 - dist);
                    }
                }

                int offset = (adjX + 127) + ((adjZ + 127) * 256);

                oldcolor = renderer.getTextureData(offset);
                oldblue = oldcolor & 0xFF;
                oldgreen = (oldcolor >> 8) & 0xFF;
                oldred = (oldcolor >> 16) & 0xFF;

                color = clamp(red + oldred, 0, 255) << 16 | clamp(green + oldgreen, 0, 255) << 8
                        | clamp(blue + oldblue, 0, 255);
                renderer.setTextureValue(offset, color);
            }
        }
    }

    public void processBlocks(World world, EntityPlayer player, mindshaftRenderer renderer, zoomState zoom) {

        BlockPos playerPos = new BlockPos(Math.floor(player.posX), Math.floor(player.posY), Math.floor(player.posZ));

        nextlayer = nextlayer + zoom.getZoomSpec().layerrate;

        while (nextlayer >= 1.0F) {

            nextlayer = nextlayer - 1.0F;

            if (layer >= 32) {
                renderer.updatePos(startX, startZ);
                renderer.refreshTexture();
                layer = 0;

                for (int i = 0; i < renderer.getTextureData().length; ++i) {

                    renderer.setTextureValue(i, 0x002200);
                }

                startX = playerPos.getX();
                startZ = playerPos.getZ();
            }

            processLayer(world, player, playerPos, renderer, zoom);
            layer++;
        }
    }
}