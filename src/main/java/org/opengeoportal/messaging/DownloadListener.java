package org.opengeoportal.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.opengeoportal.downloader.ShapefileRequestService;
import org.opengeoportal.downloader.StatusValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Receives the DownloadMessage and kicks off the download process. Replies with a message that contains a status and
 * a path to a zip file.
 *
 * Created by cbarne02 on 3/1/16.
 */
@Component
public class DownloadListener {

    private Logger logger = LoggerFactory.getLogger(DownloadListener.class);

    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private ShapefileRequestService shapefileRequestService;

    @JmsListener(destination = "download.request")
    @SendTo("download.status")
    public String processMessage(String message$) {

        DownloadRequest message = null;
        try {
            message = mapper.readValue(message$, DownloadRequest.class);
            logger.info("Received download request for feature type with layerId: " + message.getId());

        } catch (IOException e) {
            e.printStackTrace();
        }

        StatusValue status = StatusValue.fail;
        Path p = null;
        try {

            p = shapefileRequestService.download(message);
            status = StatusValue.success;
        } catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
            status = StatusValue.fail;
        }

        try {

            String path$ = "";
            if (p != null){
                path$ = p.toAbsolutePath().toString();
            }
            return mapper.writeValueAsString(new DownloadResult(status, path$));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return status.name();
    }
}
