package org.esotericist.mindshaft;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.lang.Math;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
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

    // maximum time in ticks before a segment is normally considered stale
    // actual expiry can be sooner due to distance factors
    // e.g. chunks closest to center will be expiry - 256
    // midrange chunks (dist 4 on two axes) will be expiry - 64
    // greater distance chunks will use raw expiry time
    private static final int expiry = 246;

    // random addition in ticks to expiry
    // this helps prevent a bunch of segments from expiring all at once
    // expiry 246 - 256 (closest segments) = -10
    // random fudge of 20 == ~ 50% chance adjacent segment expires next tick
    private static final int expiryFudge = 20;

    // default color for empty layers. dark green.
    private static final int defaultColor = 0x002200;

    private static Level world;

    public void setWorld(Level newWorld) {
        world = newWorld;
    }

    public void setNow(long time) {
        now = time;
    }

    public void setDim(int ID) {
        currentDim = ID;
    }

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

    static class requestID extends segmentID implements Comparable<requestID> {
        int priority;

        public requestID(segmentID ID, int newPriority) {

            super(ID.dimension, ID.x, ID.y, ID.z);
            priority = newPriority;
        }

        public int compareTo(requestID other) {
            int c = 0;
            
            // higher priorities come before lower priorities.
            c = other.priority - this.priority;
            
            if (c == 0) {
                c = this.x - other.x;
            }
            if (c == 0) {
                c = this.z - other.z;
            }
            if (c == 0) {
                c = this.y - other.y;
            }
            return c;
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

        public void markStale() {
            stale = true;
            expiration = now + mindshaftConfig.forcedExpiry;
        }
    }

    static class segmentCache extends LinkedHashMap<segmentID, layerSegment> {
        protected boolean removeEldestEntry(Map.Entry<segmentID, layerSegment> eldest) {
            layerSegment thisSegment = eldest.getValue();
            if ((thisSegment.stale && thisSegment.expiration <= mindshaftScanner.now)) {
                return true;
            } else {
                return false;
            }
        }

        public segmentCache(Integer i) {
            super(i);
        }
    }

    static segmentCache segmentsKnown = new segmentCache(512);
    static TreeSet<requestID> requestednewsegments = new TreeSet<requestID>();
    static TreeSet<requestID> requestedstalesegments = new TreeSet<requestID>();

    private int clamp(int value, int min, int max) {
        return Math.min(Math.max(value, min), max);
    }

    private boolean isLit(BlockPos pos) {

        return (world.getMaxLocalRawBrightness(pos) > 0);
    }

    private boolean isTransparent(BlockPos pos) {
        return !(world.getBlockState(pos).canOcclude());
    }

    private boolean isIntangible(BlockPos pos) {
        return (world.getBlockState(pos).getBlockSupportShape(world, pos).isEmpty());
    }

    private boolean isAir(BlockPos pos) {
        return world.getBlockState(pos).isAir();
    }

    private int worldMin() {
        return world.getMinBuildHeight();
    }

    private int worldMax() {
        return world.getMaxBuildHeight();
    }

    private int coloroutput(int red, int green, int blue) {
        return 0xFF << 24 | clamp(blue, 0, 255) << 16 | clamp(green, 0, 255) << 8 | clamp(red, 0, 255);
    }

    int processColumn(segmentID ID, int x, int z) {
        int red = 0;
        int blue = 0;
        int green = 0x22;

        int dist;

        // segment X and Z contains chunk coordinates
        // we need to translate to worldspace for our operations here
        int colX = (ID.x * 16) + x;
        int colZ = (ID.z * 16) + z;
        int colY = ID.y;

        // 32 block range, with the player in the center two blocks.
        for (int y = -15; y < 17; y++) {
            int blockY = colY + y;
            int intensity = 0;
            dist = Math.abs(y);

            boolean lit = false;
            boolean solid = true;
            boolean intangible = false;
            boolean empty = false;

            if (blockY > worldMax()) {
                intangible = false;
                solid = false;
                empty = true;
                lit = true;
            } else if (blockY >= worldMin()) {

                BlockPos pos = new BlockPos(colX, blockY, colZ);

                lit = isLit(pos);
                if (isTransparent(pos)) {
                    solid = false;
                    if (isIntangible(pos)) {
                        intangible = true;
                        if (isAir(pos)) {
                            empty = true;
                        }
                    }
                }
            }

            // the block volumes a standing player would occupy (both legs and head)
            // are both considered a distance of 0. all other distances are relative that.
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

        return coloroutput(red, green, blue);
    }

    layerSegment processSegment(segmentID ID) {
        layerSegment segment = new layerSegment(defaultColor);

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {

                int c = processColumn(ID, x, z);
                segment.setColor(x, z, c);
            }
        }

        return segment;
    }

    layerSegment addLayerSegment(segmentID ID) {
        layerSegment segment = processSegment(ID);
        segment.expiration = now + expiry + random.nextInt(expiryFudge);
        segmentsKnown.put(ID, segment);

        return segment;

    }

    layerSegment getLayerSegment(segmentID ID, int priority) {
        if( priority < 0 ) {
            segmentsKnown.remove(ID);
            return addLayerSegment(ID);
        }
        layerSegment thisSegment = segmentsKnown.get(ID);
        if (thisSegment == null) {
                requestednewsegments.add(new requestID(ID, priority));
        } else if (thisSegment.stale == true) {
            requestedstalesegments.add(new requestID(ID, priority));
        } else {
            if (thisSegment.stale == false && thisSegment.expiration - priority <= now) {
                thisSegment.markStale();
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

    public void rasterizeLayers(BlockPos pPos, int vY, mindshaftRenderer renderer, zoomState zoom) {
        if (currentTick == 0) {
            pX = pPos.getX() >> 4;
            pZ = pPos.getZ() >> 4;
        }
        int pY = pPos.getY();

        int pcX = (pX) - 8;
        int pcZ = (pZ) - 8;

        int dX = 0;
        int dZ = 0;
        int distFactor = 0;

        int radius = zoom.getZoomSpec().r + 2;
        if (radius > 8) {
            radius = 8;
        }

        int segmentrate = (int) Math.ceil(256.0 / mindshaftConfig.refreshdelay);
        int segmentcount = segmentrate * currentTick;

        for (int i = segmentcount; i < segmentcount + segmentrate; i++) {
            if (i >= 256) {
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

            // standard distance algo is a^2 + b^2 = c^2.
            // squaring a value helpfully gets us a positive value regardless of original sign
            // and 16^2 is 256
            // the bitshift is to help increase the magnitude of the values relative to 256.
            dX = (cX - 8) * (cX - 8);
            dZ = (cZ - 8) * (cZ - 8);
            int dist = dX + dZ;
            distFactor = 256 - (dist << 1);

            for( int yoffset = -1 + vY; yoffset < 1 + vY  ; yoffset++ ) {
                segmentID ID = new segmentID(currentDim, cX + pcX, pY + yoffset, cZ + pcZ);
                getLayerSegment(ID, distFactor );
            }
            segmentID ID = new segmentID(currentDim, cX + pcX, pY, cZ + pcZ);
            layerSegment resultSegment = getLayerSegment(ID, dist < 2 ? -1 : distFactor);

            if (resultSegment == null) {
                continue;
            }
            copyLayer(renderer, resultSegment, cX, cZ);
        }

        if (currentTick++ >= mindshaftConfig.refreshdelay) {
            renderer.refreshTexture();
            renderer.updatePos(pX, pZ);
            currentTick = 0;
        }

    }

    private int processqueue(TreeSet<requestID> queue, int pY, int vY, int cacheRemainder, boolean allowStale ) {
        Iterator<requestID> itr = queue.iterator();
        while (itr.hasNext() && cacheRemainder > 0) {
            requestID ID = itr.next();
            itr.remove();
            if (Math.abs(pY - ID.y) > (3 + vY) || (Math.abs(pX - ID.x) > 12) || (Math.abs(pZ - ID.z) > 12)) {
                continue;
            }
            if (segmentsKnown.containsKey(ID)) {
                if( allowStale ) { 
                    segmentsKnown.remove(ID);
                } else {
                    continue;
                }
            }
            addLayerSegment(ID);
            cacheRemainder--;
        }
        return cacheRemainder;
    }

    public void processChunks(int pY, int vY) {

        int cacheRemainder = mindshaftConfig.chunkrate;

        if (!requestednewsegments.isEmpty()) {
            cacheRemainder = processqueue(requestednewsegments, pY, vY, cacheRemainder, false);
        }
        if (!requestedstalesegments.isEmpty()) {

            cacheRemainder = processqueue(requestedstalesegments, pY, vY, cacheRemainder, true);

        }
        if (!segmentsKnown.isEmpty()) {
            int removeCount = 0;
            Set<Map.Entry<segmentID, layerSegment>> entryset = segmentsKnown.entrySet();
            Iterator<Map.Entry<segmentID, layerSegment>> itr = entryset.iterator();
            while (itr.hasNext() && removeCount++ <= mindshaftConfig.chunkrate) {
                Map.Entry<segmentID, layerSegment> entry = itr.next();
                layerSegment segment = entry.getValue();
                if (segment.stale && segment.expiration <= now) {
                    itr.remove();
                } else if (segment.expiration <= now) {
                    segment.markStale();
                }
            }
        }
    }
}