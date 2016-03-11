package org.opengeoportal.downloader;

/**
 * Created by cbarne02 on 3/8/16.
 */
public class MissingStatusException extends Exception {
    MissingStatusException(String message){
        super(message);
    }
}
