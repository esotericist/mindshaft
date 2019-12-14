package org.esotericist.mindshaft;

import java.util.Arrays;

class zoomState {
    public boolean fullscreen = false;
    private int zoommax = 0;

    private static zoomSpec[] zoomlist;

    public zoomSpec getZoomSpec() {
        return zoomlist[getZoom()];
    }

    public void initzooms() {

        int zoomcount = mindshaftConfig.zoomlevels.length;
        zoomlist = new zoomSpec[zoomcount];
        Arrays.setAll(zoomlist, (i) -> new zoomSpec());

        for (int i = 0; i < zoomcount; ++i) {
            int zoomsize = mindshaftConfig.zoomlevels[i];
            zoomlist[i].setZoomSpec(zoomsize);
        }
        for (int i = 0; i < zoomcount; ++i) {
            Mindshaft.logger.info("zoomlist: " + i + ", x:" + zoomlist[i].x + ", z:" + zoomlist[i].z + ", w:"
                    + zoomlist[i].w + ", minU:" + zoomlist[i].minU + ", minV:" + zoomlist[i].minV + ", maxU:"
                    + zoomlist[i].maxU + ", maxV:" + zoomlist[i].maxV);
        }

        if (mindshaftConfig.zoom > zoomlist.length) {
            mindshaftConfig.setZoom(zoomlist.length - 1, false);
        }
        if (mindshaftConfig.zoomfs > zoomlist.length) {
            mindshaftConfig.setZoom(zoomlist.length - 1, true);
        }
        zoommax = zoomlist.length;

    }

    public int getZoom() {
        int curzoom = fullscreen ? mindshaftConfig.zoomfs : mindshaftConfig.zoom;

        if (curzoom >= zoommax) {
            curzoom = zoommax - 1;
        }
        return curzoom;
    }

    public void nextZoom() {
        int newzoom = fullscreen ? mindshaftConfig.zoomfs : mindshaftConfig.zoom;

        newzoom++;
        if (newzoom >= zoommax) {
            if (mindshaftConfig.zoomwrap) {
                newzoom = 0;
            } else {
                newzoom = zoommax - 1;
            }
        }
        mindshaftConfig.setZoom(newzoom, fullscreen);
    }

    public void prevZoom() {
        int newzoom = fullscreen ? mindshaftConfig.zoomfs : mindshaftConfig.zoom;

        newzoom--;
        if (newzoom < 0) {
            if (mindshaftConfig.zoomwrap) {
                newzoom = zoommax - 1;
            } else {
                newzoom = 0;                
            }
        }
        mindshaftConfig.setZoom(newzoom, fullscreen);
    }
}
