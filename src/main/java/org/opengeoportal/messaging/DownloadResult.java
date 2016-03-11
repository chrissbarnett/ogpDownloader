package org.opengeoportal.messaging;

import org.opengeoportal.downloader.StatusValue;

/**
 * Message sent as a response once a download is complete
 *
 * Created by cbarne02 on 3/2/16.
 */
public class DownloadResult {

    public DownloadResult(){}

    public DownloadResult(StatusValue status, String url){
        this.status = status;
        this.url = url;
    }

    private StatusValue status;
    private String url;

    public StatusValue getStatus() {
        return status;
    }

    public void setStatus(StatusValue status) {
        this.status = status;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
