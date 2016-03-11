package org.opengeoportal.messaging;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.web.context.request.async.DeferredResult;

/**
 * Data structure sent as a request for download.
 *
 * Created by cbarne02 on 3/2/16.
 */
public class SynchronousDownloadRequest extends DownloadRequest {
    @JsonIgnore
    private DeferredResult deferredResult;

    public DeferredResult getDeferredResult() {
        return deferredResult;
    }

    public void setDeferredResult(DeferredResult deferredResult) {
        this.deferredResult = deferredResult;
    }
}
