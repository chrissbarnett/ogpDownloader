package org.opengeoportal.downloader;

/**
 * Created by cbarne02 on 3/11/16.
 */
public class DownloadFailedException extends Exception {
    DownloadFailedException(String message){
        super(message);
    }
}