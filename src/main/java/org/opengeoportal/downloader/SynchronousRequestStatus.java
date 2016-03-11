package org.opengeoportal.downloader;


import org.springframework.web.context.request.async.DeferredResult;

/**
 * Created by cbarne02 on 3/11/16.
 */
public class SynchronousRequestStatus extends RequestStatus {

    private DeferredResult deferredResult;

    public DeferredResult getDeferredResult() {
        return deferredResult;
    }

    public void setDeferredResult(DeferredResult deferredResult) {
        this.deferredResult = deferredResult;
    }

}
