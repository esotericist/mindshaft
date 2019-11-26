package org.esotericist.mindshaft;

class zoomSpec {
    public int r;
    public int x;
    public int z;
    public int w;
    public int h;
    public double minU;
    public double minV;
    public double maxU;
    public double maxV;

    public void setZoomSpec(int r, int x, int z, int w, int h, double minU, double minV, double maxU, double maxV) {

        this.r = r;
        this.x = x;
        this.z = z;
        this.w = w;
        this.h = h;
        this.minU = minU;
        this.minV = minV;
        this.maxU = maxU;
        this.maxV = maxV;
    }

    public void setZoomSpec(int r) {
        int size = r * 16;
        int w = size;
        int h = size;
        int x = -(w / 2) + 1;
        int z = x;
        double minU = (128 + x) * 1 / 256D;
        double minV = minU;
        double maxU = 1D - minU;
        double maxV = maxU;
        setZoomSpec(r, x, z, w, h, minU, minV, maxU, maxV);
    }
}