package org.opengeoportal.downloader;

import org.opengeoportal.messaging.DownloadResult;

/**
 * Created by cbarne02 on 3/11/16.
 */
public class RequestStatus {

    private String messageId;
    private StatusValue status = StatusValue.processing;
    private DownloadResult result;


    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public StatusValue getStatus() {
        return status;
    }

    public void setStatus(StatusValue status) {
        this.status = status;
    }

    public DownloadResult getResult() {
        return result;
    }

    public void setResult(DownloadResult result) {
        this.result = result;
    }
}
