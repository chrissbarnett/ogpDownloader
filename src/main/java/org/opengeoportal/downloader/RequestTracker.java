package org.opengeoportal.downloader;

import org.opengeoportal.messaging.DownloadResult;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Stores a DeferredResult associated with a MessageId in memory. (Concurrent HashMap). Should be thread safe.
 *
 * Created by cbarne02 on 3/8/16.
 */
@Component
public class RequestTracker {

    private ConcurrentMap<String, RequestStatus> statusMap = new ConcurrentHashMap();

    /**
     * Stores id and RequestStatus as a key value pair.
     *
     * @param requestStatus
     */
    public void add(RequestStatus requestStatus){
        statusMap.putIfAbsent(requestStatus.getMessageId(), requestStatus);
    }

    /**
     * Retrieves a RequestStatus given a message id.
     *
     * @param id
     * @return
     * @throws MissingStatusException
     */
    public RequestStatus retrieve(String id) throws MissingStatusException {
        RequestStatus result = statusMap.get(id);
        if (result == null){
            throw new MissingStatusException("MessageId/RequestStatus not found!");
        }
        return result;
    }

    /**
     * Removes the entry from the HashMap.
     *
     * @param id
     * @throws MissingStatusException
     */
    public void remove(String id) throws MissingStatusException {
        RequestStatus result = statusMap.get(id);
        if (result == null){
            throw new MissingStatusException("MessageId/RequestStatus not found!");
        } else {
            statusMap.remove(id);
        }
    }


}
