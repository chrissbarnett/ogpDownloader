package org.opengeoportal.controller;

import org.opengeoportal.datastore.DataStoreManager;

import org.opengeoportal.downloader.*;
import org.opengeoportal.gdal.GetInfo;
import org.opengeoportal.messaging.DownloadRequest;
import org.opengeoportal.messaging.SynchronousDownloadRequest;
import org.opengis.referencing.FactoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.util.UriTemplate;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Created by cbarne02 on 1/27/16.
 */
@RestController
public class ShapefileRequestController {

    @Autowired
    private DataStoreManager dsManager;

    @Autowired
    private DownloadRequestService downloadRequestService;

    @Autowired
    private GetInfo getInfo;

    Logger logger = LoggerFactory.getLogger(ShapefileRequestController.class);

    @RequestMapping(value = "typeNames", method = RequestMethod.GET)
    public String[] getTypeList(@RequestParam("refresh") Optional<Boolean> refreshOpt) throws IOException {
        if (refreshOpt.isPresent()){
            return dsManager.getTypeList(refreshOpt.get());
        }

        return dsManager.getTypeList();
    }

    @RequestMapping(value = "info/{filename}", method = RequestMethod.GET)
    public String getInfo(@PathVariable("filename") String filename) throws IOException {
        //TODO: make this a json object
        logger.info(filename);
        getInfo.getVectorInfo("/usr/local/shapefiles/" + filename + ".shp");
        return "ok";
    }

    @RequestMapping(value = "/**/syncrequest", method = RequestMethod.GET)
    public DeferredResult<String> getShapefileSync(HttpServletRequest request,
                                              @RequestParam("bbox") Optional<List<Double>> bboxOpt) throws FactoryException, IOException {

        //workaround to get id values that might have forward slashes.
        String path = String.valueOf(request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE));
        String ogpId = "";

        //We can use UriTemplate to map the restOfTheUrl
        UriTemplate template = new UriTemplate("/{id}/syncrequest");
        boolean isTemplateMatched = template.matches(path);
        if(isTemplateMatched) {
            Map<String, String> matchTemplate = template.match(path);
            ogpId = matchTemplate.get("id");
        }


        SynchronousDownloadRequest downloadRequest = new SynchronousDownloadRequest();
        downloadRequest.setId(ogpId);
        logger.info(ogpId);

        if (bboxOpt.isPresent()){
            //build a bounding box filter query
            List<Double> bbox = bboxOpt.get();
            downloadRequest.setMinx(bbox.get(0));
            downloadRequest.setMiny(bbox.get(1));
            downloadRequest.setMaxx(bbox.get(2));
            downloadRequest.setMaxy(bbox.get(3));
        }


        DeferredResult<String> deferredResult = new DeferredResult<>();
        downloadRequest.setDeferredResult(deferredResult);
        downloadRequestService.request(downloadRequest);

        return deferredResult;

    }

    @RequestMapping(value = "/**/request", method = RequestMethod.GET)
    public RequestReceipt getShapefile(HttpServletRequest request,
                                       @RequestParam("bbox") Optional<List<Double>> bboxOpt) throws FactoryException, IOException {

        //workaround to get id values that might have forward slashes.
        String path = String.valueOf(request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE));
        String ogpId = "";

        //We can use UriTemplate to map the restOfTheUrl
        UriTemplate template = new UriTemplate("/{id}/request");
        boolean isTemplateMatched = template.matches(path);
        if(isTemplateMatched) {
            Map<String, String> matchTemplate = template.match(path);
            ogpId = matchTemplate.get("id");
        }


        DownloadRequest downloadRequest = new DownloadRequest();
        downloadRequest.setId(ogpId);

        if (bboxOpt.isPresent()){
            //build a bounding box filter query
            List<Double> bbox = bboxOpt.get();
            downloadRequest.setMinx(bbox.get(0));
            downloadRequest.setMiny(bbox.get(1));
            downloadRequest.setMaxx(bbox.get(2));
            downloadRequest.setMaxy(bbox.get(3));
        }

        return downloadRequestService.request(downloadRequest);
    }

    @RequestMapping(value = "/{requestId}/status", method = RequestMethod.GET)
    public RequestStatus getRequestStatus(@PathVariable("requestId") String requestId) throws IOException, MissingStatusException {

        return downloadRequestService.getStatus(requestId);
    }

    @RequestMapping(value = "/{requestId}/download", method = RequestMethod.GET)
    public ResponseEntity getDownload(@PathVariable("requestId") String requestId) throws IOException,
            MissingStatusException, StillProcessingException, DownloadFailedException {

        Path path = downloadRequestService.getDownload(requestId);

        ResponseEntity<InputStreamResource> entity = ResponseEntity.ok()
                .contentLength( Files.size(path))
                .contentType(MediaType.parseMediaType("application/zip"))
                .header("Content-Disposition",
                        "attachment; filename=\"" + path.getFileName().toString() +"\"")
                .body(new InputStreamResource(Files.newInputStream(path)));

        return entity;
    }
    //TODO: another async endpoint.
    //The response should be some sort of id or location. A different servlet should
    //retrieve the package. Use websockets?

}
