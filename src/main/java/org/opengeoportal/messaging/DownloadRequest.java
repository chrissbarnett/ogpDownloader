package org.opengeoportal.messaging;

/**
 * Data structure sent as a request for download.
 *
 * Created by cbarne02 on 3/2/16.
 */
public class DownloadRequest {
    private String id;
    private Double minx;
    private Double miny;
    private Double maxx;
    private Double maxy;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Double getMinx() {
        return minx;
    }

    public void setMinx(Double minx) {
        this.minx = minx;
    }

    public Double getMiny() {
        return miny;
    }

    public void setMiny(Double miny) {
        this.miny = miny;
    }

    public Double getMaxx() {
        return maxx;
    }

    public void setMaxx(Double maxx) {
        this.maxx = maxx;
    }

    public Double getMaxy() {
        return maxy;
    }

    public void setMaxy(Double maxy) {
        this.maxy = maxy;
    }
}
