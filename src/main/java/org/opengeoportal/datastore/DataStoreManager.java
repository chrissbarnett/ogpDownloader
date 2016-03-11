package org.opengeoportal.datastore;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.Map;

/**
 * Service that handles connections to a Database through the GeoTools DataStore abstraction. Also provides access to a
 * list of FeatureTypes in that DataStore.
 *
 * Created by cbarne02 on 1/26/16.
 */
@Service
public class DataStoreManager {

    private DataStore dataStore;

    @Autowired
    private TypeList typeList;

    private Logger logger = LoggerFactory.getLogger(DataStoreManager.class);

    @Autowired
    private DataStoreConnectionSettings params;

    /**
     * Tries to connect to the DataStore and populate the FeatureType list proactively.
     */
    @PostConstruct
    public void init(){

        try {
            connect();
        } catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
            return;
        }

        try {
            String[] list = getTypeList();
            if (list == null){
                logger.error("Type List is null!");
            } else if (list.length == 0) {
                logger.error("Type List is empty!");
            }
        } catch (IOException e) {
            logger.error("Unable to retrieve Type List.");
            e.printStackTrace();
        }

    }


    /**
     * Connect to a GeoTools DataStore via JDBC.
     * @throws Exception
     */
    public void connect() throws Exception {
        if (params == null){
            throw new Exception("params object is null");
        }
        Map<String, Object> connectionParameters = params.getParams();

        try {
            dataStore = DataStoreFinder.getDataStore(connectionParameters);
        } catch (RuntimeException e){
            throw new Exception("Could not connect - check database", e);
        }

        if (dataStore == null) {
            throw new Exception("Could not connect - check parameters");
        }

    }

    public String[] getTypeList() throws IOException {
        return getTypeList(false);
    }

    /**
     * Returns a list of FeatureTypes from the DataStore.
     * Proxies methods from TypeList so that we can use the cacheable annotation.
     *
     * @param refresh if true queries the database for the list. if false, returns the cached version.
     * @return
     * @throws IOException
     */
    public String[] getTypeList(Boolean refresh) throws IOException {
        if (refresh){
            return typeList.refreshTypeList(dataStore);
        }
        return typeList.getTypeList(dataStore);
    }

    /**
     * Returns the GeoTools DataStore.
     *
     * @return
     */
    public DataStore getDataStore(){
        return dataStore;
    }

    //TODO: add code to see if there is more than 1 match if the search is not strict

    /**
     * Searches the FeatureType list from the DataStore for a particular FeatureType.
     *
     * @param typeName
     * @param strict if true, the match must be exact. if false, attempts to find a match ignoring case and namespacing.
     * @return the matched typeName
     * @throws Exception
     */
    public String findTypeName(String typeName, boolean strict) throws Exception {
        String[] types = getTypeList();
        //exact match
        for (String s: types){
            if (s.equals(typeName)){
                return s;
            }
        }

        if (!strict) {

            //case-insensitive
            for (String s : types) {
                if (s.equalsIgnoreCase(typeName)) {
                    logger.debug("match: " + s);
                    return s;
                }
            }

            //ignore namespacing
            String localTypeName = typeName.substring(typeName.lastIndexOf(".") + 1);
            logger.debug("Ignoring Namespacing: " + localTypeName);
            for (String s : types) {
                String local = s;
                if (s.lastIndexOf(".") == -1){
                    local = s.substring(s.lastIndexOf(".") + 1);
                }

                if (local.equalsIgnoreCase(localTypeName)) {
                    logger.debug("loose match: " + s);
                    return s;
                }
            }
        }

        //create Not found exception type
        throw new Exception("TypeName not found.");
    }


    @PreDestroy
    public void destroy(){
        if (dataStore != null){
            dataStore.dispose();
        }
    }


}
