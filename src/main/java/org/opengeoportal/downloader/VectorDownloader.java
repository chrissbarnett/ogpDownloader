package org.opengeoportal.downloader;

import org.opengeoportal.messaging.DownloadRequest;
import org.opengis.geometry.BoundingBox;

import java.nio.file.Path;


/**
 * Created by cbarne02 on 2/8/16.
 */
public interface VectorDownloader {
    String findFeatureType(String requestedFeatureType) throws Exception;

    Path download(DownloadRequest request) throws Exception;

    Path download(String typeName) throws Exception;
    Path download(String typeName, BoundingBox bbox) throws Exception;
}
