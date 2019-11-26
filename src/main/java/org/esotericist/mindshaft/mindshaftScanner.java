package org.esotericist.mindshaft;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;

import java.lang.Math;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Random;
import java.util.Arrays;
import java.util.Iterator;

class mindshaftScanner {

    Random random = new Random();

    private int layer = 0;

    private int startX = 0;
    private int startZ = 0;

    private float nextlayer = 0;

    private static long now = 0;
    private static int chunkRadius = 8;
    private static int currentDim = 0;

    // fudge for player's current Y level
    private static final double fudgeY = 17 / 32D;

    // how many chunks can be cached per tick
    private static final int chunkCacheMax = 3;

    // minimum time in ticks before a chunk is considered stale
    private static final int expiry = 20000;

    // random addition in ticks to expiry
    private static final int expiryFudge = 100;

    // minimum time in ticks before a chunk is forcibly removed
    // actual forced expiration time is forcedExpiry + expiry
    private static final int forcedExpiry = 160000;

    // random addition in ticks to forcedExpiry
    private static final int forcedExpiryFudge = 2000;

    // default color for empty layers. dark green.
    private static final int defaultColor = 0x002200;

    private layerSegment emptyLayer = new layerSegment(defaultColor);

    static class chunkID {
        int dimension, x, z;

        @Override
        public int hashCode() {
            return Objects.hash(dimension, x, z);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (!(obj instanceof chunkID))
                return false;
            chunkID other = (chunkID) obj;
            return dimension == other.dimension && x == other.x && z == other.z;
        }

        public chunkID(int dim, int newX, int newZ) {
            dimension = dim;
            x = newX;
            z = newZ;
        }
    }

    static class layerSegment {
        private int[] color = new int[256];

        public layerSegment(Integer v) {
            Arrays.fill(color, v);
        }

        public void setColor(int x, int y, int c) {
            color[x + (y * 16)] = c;
        }

        public int getColor(int x, int y) {
            return color[x + (y * 16)];
        }
    }

    static class block {
        boolean solid = true;
        boolean intangible = false;
        boolean empty = false;
        boolean lit = false;
    }

    static class chunkData {
        long expiration = 0;
        block[][][] blockData = new block[16][256][16];
        boolean stale = false;
        boolean expired = false;
        LinkedHashMap<Integer, layerSegment> layers = new LinkedHashMap<Integer, layerSegment>(16, 0.75f, true);

        public chunkData() {
            for (int i = 0; i < 16; i++) {
                for (int j = 0; j < 256; j++) {
                    for (int k = 0; k < 16; k++) {
                        blockData[i][j][k] = new block();
                    }
                }
            }
        }
    }

    static class chunkCache extends LinkedHashMap<chunkID, chunkData> {
        protected boolean removeEldestEntry(Map.Entry<chunkID, chunkData> eldest) {
            chunkData thisChunk = eldest.getValue();
            if (thisChunk.expired || (thisChunk.stale && thisChunk.expiration <= mindshaftScanner.now)) {
                // Mindshaft.logger.info(now + ": removed chunk: " + eldest.getKey().x + ", " +
                // eldest.getKey().z);
                return true;
            } else {
                return false;
            }
        }

        public chunkCache(Integer i) {
            super();
        }
    }

    static chunkCache chunksKnown = new chunkCache(32);
    static LinkedList<chunkID> requestedChunks = new LinkedList<chunkID>();

    private int clamp(int value, int min, int max) {
        return Math.min(Math.max(value, min), max);
    }

    private boolean isLit(World world, BlockPos pos) {

        if (((world.getLightFor(EnumSkyBlock.BLOCK, pos) > 0)
                || (world.provider.isSurfaceWorld()) && (world.getLightFor(EnumSkyBlock.SKY, pos) > 0))) {
            return true;
        }
        return false;
    }

    private void requestChunk(chunkID chunk) {
        if (!requestedChunks.contains(chunk)) {
            // Mindshaft.logger.info(now+ ": requested chunk: " + chunk.x + ", " + chunk.z);
            requestedChunks.add(chunk);
        }
    }

    chunkData getChunk(chunkID chunk) {
        chunkData thischunk = chunksKnown.get(chunk);
        if (thischunk != null) {
            if (!thischunk.stale && thischunk.expiration <= now) {
                // Mindshaft.logger.info(now + ": stale chunk: " + chunk.x + ", " + chunk.z + ",
                // stale at:" + thischunk.expiration );
                thischunk.stale = true;
                thischunk.expiration = now + forcedExpiry + random.nextInt(forcedExpiryFudge);
                requestChunk(chunk);
            }
            // Mindshaft.logger.info(now + ": fetched chunk: " + chunk.x + ", " + chunk.z +
            // ", stale at:" + thischunk.expiration );
            return chunksKnown.get(chunk);
        } else {
            requestChunk(chunk);
        }
        return null;
    }

    layerSegment addLayerSegment(chunkID chunk) {
        random.setSeed(chunk.x * chunk.z);
        layerSegment testSegment = new layerSegment(random.nextInt(0xFFFFFF));
        return testSegment;
    }

    layerSegment getLayerSegment(chunkID chunk, Integer y) {
        chunkData thisChunk = getChunk(chunk);
        if (thisChunk == null) {
            return emptyLayer;
        }
        layerSegment thisSegment = chunksKnown.get(chunk).layers.get(y);
        if (thisSegment == null) {
            thisSegment = addLayerSegment(chunk);
        }

        return thisSegment;
    }

    void scanChunk(World world, chunkID chunk) {
        chunkData newChunk = new chunkData();
        newChunk.expiration = now + expiry + random.nextInt(expiryFudge);
        // Mindshaft.logger.info(now + ": new chunk: " + chunk.x + ", " + chunk.z + ",
        // stale at: " + newChunk.expiration);
        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 256; y++) {
                for (int z = 0; z < 16; z++) {
                    BlockPos pos = new BlockPos(chunk.x + x, y, chunk.z + z);
                    IBlockState state = world.getBlockState(pos);
                    Block blockID = state.getBlock();
                    block thisBlock = newChunk.blockData[x][y][z];

                    thisBlock.lit = true;
                    thisBlock.lit = isLit(world, pos);

                    if (state.isOpaqueCube() != true) {
                        thisBlock.solid = false;

                        if (state.getCollisionBoundingBox(world, pos) == null) {
                            thisBlock.intangible = true;

                            if (blockID.isAir(state, world, pos)) {
                                thisBlock.empty = true;
                            }
                        }
                    }
                }
            }
        }

        chunksKnown.put(chunk, newChunk);
    }

    void copyLayer(mindshaftRenderer renderer, layerSegment segment, int cX, int cZ) {
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int col = segment.getColor(x, z);
                renderer.setTextureValue((cX * 16) + x, (cZ * 16) + z, col);
            }
        }
    }

    public void rasterizeLayers(World world, EntityPlayer player, mindshaftRenderer renderer, zoomState zoom) {
        int pcX = (int) Math.floor(player.posX / 16.0) - 8;
        int pcZ = (int) Math.floor(player.posZ / 16.0) - 8;

        for (int cX = 0; cX < 16; cX++) {
            for (int cZ = 0; cZ < 16; cZ++) {
                chunkID thisChunk = new chunkID(currentDim, cX + pcX, cZ + pcZ);
                layerSegment thisSegment = getLayerSegment(thisChunk, (int) (player.posY - fudgeY));
                // Mindshaft.logger.info("cX: " + cX + ", cZ: " + cZ + "");
                copyLayer(renderer, thisSegment, cX, cZ);
            }
        }
        renderer.refreshTexture();
    }

    public void processChunks(World world) {
        now = world.getTotalWorldTime();
        currentDim = world.provider.getDimension();
        if (!requestedChunks.isEmpty()) {
            int cacheCount = 0;
            Iterator<chunkID> itr = requestedChunks.iterator();
            while (itr.hasNext() && cacheCount++ <= chunkCacheMax) {
                scanChunk(world, itr.next());
                itr.remove();
            }
            // Mindshaft.logger.info("requested count: " + requestedChunks.size());
            /*
             * for (chunkID thisChunk : requestedChunks) { if (cacheCount++ > chunkCacheMax)
             * { break; } scanChunk(world, player, thisChunk); ; }
             */
        }
    }

    public void processLayer(World world, EntityPlayer player, BlockPos playerPos, mindshaftRenderer renderer,
            zoomState zoom) {

        int y = layer - 15;
        zoomSpec curzoom = zoom.getZoomSpec();
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

                BlockPos pos = new BlockPos(playerPos.getX() + x, player.posY - fudgeY + y, playerPos.getZ() + z);
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

                lit = isLit(world, pos);

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

                int tX = adjX + 127;
                int tZ = adjZ + 127;

                oldcolor = renderer.getTextureValue(tX, tZ);
                oldblue = oldcolor & 0xFF;
                oldgreen = (oldcolor >> 8) & 0xFF;
                oldred = (oldcolor >> 16) & 0xFF;

                color = clamp(red + oldred, 0, 255) << 16 | clamp(green + oldgreen, 0, 255) << 8
                        | clamp(blue + oldblue, 0, 255);
                renderer.setTextureValue(tX, tZ, color);
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