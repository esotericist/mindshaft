package org.esotericist.mindshaft;

class zoomspec {
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
    
    public void setZoomSpec(int x, int z, int w, int h, 
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

    public void setZoomSpec( int size, float layerrate, int overdraw ) {
        int w = size;
        int h = size;
        int x = -(w / 2) + 1;
        int z = x;
        double minU = (128 + x) * 1 / 256D;
        double minV = minU;
        double maxU = 1D - minU;
        double maxV = maxU;
        setZoomSpec(x, z, w, h, minU, minV, maxU, maxV, layerrate, overdraw);
    }
}

class zoomstate  {
    public boolean fullscreen = false;
    public int zoommax = 0;

    public int getZoom() {
        int curzoom;
        if( fullscreen ) {
            curzoom = mindshaftConfig.zoomfs;
        } else {
            curzoom = mindshaftConfig.zoom;
        }
        if( curzoom >= zoommax ) {
            curzoom = zoommax - 1;
        }
        return curzoom;
    }

    public void nextZoom() {
        int newzoom = fullscreen ? mindshaftConfig.zoomfs : mindshaftConfig.zoom;
        newzoom++;
        if( newzoom >= zoommax ) {
            newzoom = 0;
        }
        mindshaftConfig.setZoom(newzoom, fullscreen);
    }

    public void prevZoom() {
        int newzoom = fullscreen ? mindshaftConfig.zoomfs : mindshaftConfig.zoom;
        newzoom--;
        if( newzoom < 0 ) {
            newzoom = zoommax - 1;
        }
        mindshaftConfig.setZoom(newzoom, fullscreen);
    }
}
