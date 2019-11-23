package org.esotericist.mindshaft;

import java.util.Arrays;

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
    private int zoommax = 0;

    private static zoomspec[] zoomlist;

    public zoomspec getZoomSpec() {
        int currentzoom = fullscreen ? mindshaftConfig.zoomfs : mindshaftConfig.zoom;

        return zoomlist[currentzoom];
    }

    public void initzooms() {

        int zoomcount = mindshaftConfig.zoomlevels.length;
        zoomlist = new zoomspec[ zoomcount ];
        Arrays.setAll(zoomlist, (i) -> new zoomspec());

        for( int i = 0; i < zoomcount; ++i ) {
            int zoomsize = mindshaftConfig.zoomlevels[i];
            int layerrate = mindshaftConfig.layerrate / zoomsize;
            if( layerrate <= 0 ) {
                layerrate = 1;
            }
            zoomlist[i].setZoomSpec( zoomsize, layerrate, 30);
        }
        for( int i = 0; i < zoomcount; ++i ) {
            Mindshaft.logger.info("zoomlist: " + i +  ", x:" + zoomlist[i].x + ", z:" + zoomlist[i].z + ", w:" + zoomlist[i].w + ", minU:" + zoomlist[i].minU + ", minV:" + zoomlist[i].minV + ", maxU:" + zoomlist[i].maxU + ", maxV:" + zoomlist[i].maxV );
        }

        if( mindshaftConfig.zoom > zoomlist.length ) {
            mindshaftConfig.setZoom(zoomlist.length - 1, false );
        }
        if( mindshaftConfig.zoomfs > zoomlist.length ) {
            mindshaftConfig.setZoom(zoomlist.length - 1, true );
        }
        zoommax = zoomlist.length;

    }

    public int getZoom() {
        int curzoom = fullscreen ? mindshaftConfig.zoomfs : mindshaftConfig.zoom;

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
