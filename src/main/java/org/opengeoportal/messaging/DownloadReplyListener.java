package org.opengeoportal.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.opengeoportal.downloader.DownloadRequestService;
import org.opengeoportal.downloader.MissingStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Created by cbarne02 on 3/8/16.
 *
 * Handles a reply from a Download request.
 *
 * Listens for the JMS queue 'download.status'. This should be the reply to a download request. Payload is a json string.
 *
 */
@Component
public class DownloadReplyListener {

    private ObjectMapper mapper = new ObjectMapper();

    private Logger logger = LoggerFactory.getLogger(DownloadReplyListener.class);

    @Autowired
    private DownloadRequestService downloadRequestService;

    @JmsListener(destination = "download.status")
    public void processMessage(@Payload String message$, @Header("jms_correlationId") String correlationId) {
        //TODO: only the sending instance should be able to receive this status message
        DownloadResult message = null;
        try {
            message = mapper.readValue(message$, DownloadResult.class);
            logger.debug("Received download request status: " + message.getStatus());
            logger.debug("CorrelationId: " + correlationId);
            downloadRequestService.resolve(correlationId, message);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (MissingStatusException e) {
            e.printStackTrace();
        }

    }
}
