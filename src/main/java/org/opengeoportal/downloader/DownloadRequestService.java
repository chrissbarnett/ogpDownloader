package org.opengeoportal.downloader;

import org.opengeoportal.messaging.DownloadRequest;
import org.opengeoportal.messaging.DownloadRequestSender;
import org.opengeoportal.messaging.DownloadResult;
import org.opengeoportal.messaging.SynchronousDownloadRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Handles Download request and response process
 *
 * Created by cbarne02 on 3/8/16.
 */
@Service
public class DownloadRequestService {

    @Autowired
    private DownloadRequestSender sender;

    @Autowired
    private RequestTracker tracker;

    /**
     * Sends the DownloadRequest message to a message queue and associates the RequestStatus created in the controller
     * with the message id. This way the result can be passed back to the controller once a message response is returned.
     *
     * @param request
     */
    public RequestReceipt request(DownloadRequest request){
        //the sender sends the message with the download request params and returns a message id.

        String messageId = sender.send(request);
        RequestStatus status = new RequestStatus();
        status.setMessageId(messageId);

        tracker.add(status);

        return new RequestReceipt(messageId);
    }

    /**
     * Sends the SynchronousDownloadRequest message to a message queue and associates the RequestStatus created in the controller
     * with the message id. This way the result can be passed back to the controller once a message response is returned.
     *
     * @param request
     */
    public RequestReceipt request(SynchronousDownloadRequest request){
        //the sender sends the message with the download request params and returns a message id.
        String id = sender.send(request);
        //the tracker stores the deferred result object (from the controller) and associates it with the message id.
        SynchronousRequestStatus status = new SynchronousRequestStatus();
        status.setMessageId(id);
        status.setDeferredResult(request.getDeferredResult());

        tracker.add(status);

        return new RequestReceipt(id);
    }





    public RequestStatus getStatus(String messageId) throws MissingStatusException {
        return tracker.retrieve(messageId);
    }

    public Path getDownload(String messageId) throws MissingStatusException, DownloadFailedException, StillProcessingException {
        RequestStatus status = getStatus(messageId);
        if (status.getStatus().equals(StatusValue.success)){
            return Paths.get(status.getResult().getUrl());
        } else if (status.getStatus().equals(StatusValue.processing)){
            throw new StillProcessingException("Still processing the request.");
        } else {
            throw new DownloadFailedException("Download failed.");
        }
    }


    /**
     *
     * @param messageId
     * @param result
     * @throws MissingStatusException
     * @throws IOException
     */
    public void resolve(String messageId, DownloadResult result) throws MissingStatusException, IOException {
        RequestStatus status = getStatus(messageId);
        updateStatus(messageId, result);

        if (status instanceof SynchronousRequestStatus){
            resolveDeferred(result, (SynchronousRequestStatus) status);
        }
    }


    public void updateStatus(String messageId, DownloadResult result) throws MissingStatusException {
        RequestStatus status = getStatus(messageId);
        status.setStatus(result.getStatus());
        status.setResult(result);
    }

    /**
     * Takes the correlation id of the JMS message and the DownloadResult, which contains the location of the requested
     * zip file, and creates a Response Entity that streams the zip file. Using the correlation id, we look up the
     * DeferredResult from the controller and resolve it with the ResponseEntity.
     *
     * If the DownloadResult reports an error, takes the correlation id of the JMS message and the DownloadResult,
     * and creates a 404 Response Entity. Using the correlation id, we look up the
     * DeferredResult from the controller and resolve it with the ResponseEntity.
     *
     * @param status The request status
     * @param result the message sent as a response to the download request
     * @throws MissingStatusException
     * @throws IOException
     */
    private void resolveDeferred(DownloadResult result, SynchronousRequestStatus status) throws MissingStatusException, IOException {
        try {
            if (status.getStatus().equals(StatusValue.success)) {
                //the DownloadResult contains
                Path path = Paths.get(result.getUrl());
                ResponseEntity<InputStreamResource> entity = ResponseEntity.ok()
                        .contentLength( Files.size(path))
                        .contentType(MediaType.parseMediaType("application/zip"))
                        .header("Content-Disposition",
                                "attachment; filename=\"" + path.getFileName().toString() +"\"")
                        .body(new InputStreamResource(Files.newInputStream(path)));



                status.getDeferredResult().setResult(entity);

            } else if (status.getStatus().equals(StatusValue.fail)){
                //we could have a more nuanced response if we had more info in result
                ResponseEntity<String> entity = ResponseEntity.status(404).body("Not found.");
                status.getDeferredResult().setErrorResult(entity);
            }

        } finally {
            tracker.remove(status.getMessageId());
        }

    }


}
