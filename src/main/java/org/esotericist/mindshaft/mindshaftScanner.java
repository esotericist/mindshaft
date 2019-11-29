package org.esotericist.mindshaft;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;

import java.lang.Math;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.Arrays;
import java.util.Iterator;

class mindshaftScanner {

    Random random = new Random();

    private static long now = 0;
    private static int currentDim = 0;
    private static int currentTick = 0;

    // cache values for player X and Z chunk coordinate
    // this is necessary for temporal stability in texture offsets while rendering
    private static int pX;
    private static int pZ;

    // fudge for player's current Y level
    private static final double fudgeY = 17 / 32D;

    // minimum time in ticks before a chunk is considered stale
    private static final int expiry = 80;

    // random addition in ticks to expiry
    private static final int expiryFudge = 100;

    // minimum time in ticks before a chunk is forcibly removed
    // actual forced expiration time is forcedExpiry + expiry
    private static final int forcedExpiry = 800;

    // default color for empty layers. dark green.
    private static final int defaultColor = 0x002200;

    // private layerSegment emptyLayer = new layerSegment(defaultColor);

    static class segmentID {
        int dimension, x, y, z;

        @Override
        public int hashCode() {
            return Objects.hash(dimension, x, y, z);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (!(obj instanceof segmentID))
                return false;
            segmentID other = (segmentID) obj;
            return dimension == other.dimension && x == other.x && y == other.y && z == other.z;
        }

        public segmentID(int dim, int newX, int newY, int newZ) {
            dimension = dim;
            x = newX;
            y = newY;
            z = newZ;
        }

    }

    static class layerSegment {
        long expiration = 0;
        boolean stale = false;

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


    static class segmentCache extends LinkedHashMap<segmentID, layerSegment> {
        protected boolean removeEldestEntry(Map.Entry<segmentID, layerSegment> eldest) {
            layerSegment thisSegment = eldest.getValue();
            if ((thisSegment.stale && thisSegment.expiration <= mindshaftScanner.now)) {
                // Mindshaft.logger.info(now + ": removed chunk: " + eldest.getKey().x + ", " +
                // eldest.getKey().z);
                return true;
            } else {
                return false;
            }
        }

        public segmentCache(Integer i) {
            super();
        }
    }

    static segmentCache segmentsKnown = new segmentCache(512);
    static LinkedList<segmentID> requestedSegments = new LinkedList<segmentID>();

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

    int processColumn(World world, segmentID ID, int x, int z) {
        int color = defaultColor;
        int red = 0;
        int blue = 0;
        int green = 0x22;

        int dist;

        int segX = ID.x * 16;
        int segY = ID.y;
        int segZ = ID.z * 16;

        for (int y = -15; y < 17; y++) {
            int intensity = 0;
            dist = Math.abs(y);



            boolean lit = false;
            boolean solid = true;
            boolean intangible = false;
            boolean empty = false;

            if (y + segY > 255) {
                intangible = false;
                solid = false;
                empty = true;
                lit = true;
            } else if( y + segY >= 0 ) {

                BlockPos pos = new BlockPos(segX + x, segY + y, segZ + z);

                IBlockState state = world.getBlockState(pos);
                Block blockID = state.getBlock();
                lit = isLit(world, pos);
    
                if (state.isOpaqueCube() != true) {
                    solid = false;
    
                    if (state.getCollisionBoundingBox(world, pos) == null) {
                        intangible = true;
    
                        if (blockID.isAir(state, world, pos)) {
                            empty = true;
                        }
                    }
                }
            }

            if (y > 1) {
                dist--;
            }
            if (y == 1) {
                dist = 0;
            }
            if (dist > 10) {
                dist = 10;
            }

            if (!intangible || !lit) {
                if (dist > 0) {
                    intensity = (11 - dist);
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
                    red = red + (15 - dist);
                }
                if (y > 1) {
                    blue = blue + (16 - dist);
                }
            }
        }

        color = clamp(red, 0, 255) << 16 | clamp(green, 0, 255) << 8 | clamp(blue, 0, 255);
        return color;
    }

    layerSegment processSegment(World world, segmentID ID) {
        layerSegment segment = new layerSegment(defaultColor);

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {

                int c = processColumn(world, ID, x, z);
                segment.setColor(x, z, c);
            }
        }

        return segment;
    }

    layerSegment addLayerSegment(World world, segmentID ID) {
        layerSegment segment = processSegment(world, ID);
        segment.expiration = now + expiry + random.nextInt(expiryFudge);
        segmentsKnown.put(ID, segment);

        return segment;

    }

    layerSegment getLayerSegment(World world, segmentID ID) {
        layerSegment thisSegment = segmentsKnown.get(ID);
        if (thisSegment == null || thisSegment.stale == true ) {
            thisSegment = addLayerSegment(world, ID);
        } else {
            if(thisSegment.stale == false && thisSegment.expiration <= now ) {
                thisSegment.stale = true;
                thisSegment.expiration = now + forcedExpiry;
            }
        }

        return thisSegment;
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
        if( currentTick == 0 ) {
            pX = (int)(player.posX) >> 4;
            pZ = (int)(player.posZ) >> 4;
        }

        int pcX = (pX) - 8;
        int pcZ = (pZ) - 8;

        int radius = zoom.getZoomSpec().r + 2;
        if (radius > 8) {
            radius = 8;
        }

        int segmentrate = (int) Math.ceil(256.0 / mindshaftConfig.refreshdelay);
        int segmentcount = segmentrate * currentTick;
        

        for(int i = segmentcount; i < segmentcount + segmentrate; i++) {
            if( i >= 256) {
                break;
            }
            int cX = i / 16;
            int cZ = i % 16;
            if (cX < 8 - radius || cX > 8 + radius) {
                continue;
            }
            if (cZ < 8 - radius || cZ > 8 + radius) {
                continue;
            }
            segmentID ID = new segmentID(currentDim, cX + pcX, (int) (player.posY - fudgeY), cZ + pcZ);
            layerSegment thisSegment = getLayerSegment(world, ID);
            if (thisSegment == null) {
                continue;
            }
            copyLayer(renderer, thisSegment, cX, cZ);
        }

        if( currentTick++ >= mindshaftConfig.refreshdelay ) {
            renderer.refreshTexture();
            renderer.updatePos(pX, pZ);
            currentTick = 0;
        }

    }

    public void processChunks(World world) {
        now = world.getTotalWorldTime();
        currentDim = world.provider.getDimension();
        /*
        if (!requestedChunks.isEmpty()) {
            int cacheCount = 0;
            Iterator<chunkID> itr = requestedChunks.iterator();
            while (itr.hasNext() && cacheCount++ <= mindshaftConfig.chunkrate) {
                scanChunk(world, itr.next());
                itr.remove();
            }
        }
        */
        if (!segmentsKnown.isEmpty()) {
            int removeCount = 0;
            Set<Map.Entry<segmentID, layerSegment>> entryset = segmentsKnown.entrySet();
            Iterator<Map.Entry<segmentID, layerSegment>> itr = entryset.iterator();
            while (itr.hasNext() && removeCount++ <= mindshaftConfig.chunkrate * 10) {
                Map.Entry<segmentID, layerSegment> entry = itr.next();
                layerSegment segment = entry.getValue();
                if (segment.stale && segment.expiration <= now) {
                    itr.remove();
                }
            }
        }
    }
}