package org.opengeoportal.datastore;

import org.geotools.data.DataStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Stores a list of FeatureTypes in a GeoTools DataStore.
 *
 * Abstracted to separate class so that we can use cacheable for the type list.
 * Created by cbarne02 on 3/8/16.
 */
@Component
public class TypeList {
    private Logger logger = LoggerFactory.getLogger(TypeList.class);

    /**
     * Returns a list of FeatureTypes in a GeoTools DataStore. Returns a cached version if it exists.
     * @param dataStore
     * @return
     * @throws IOException
     */
    @Cacheable("typeList")
    public String[] getTypeList(DataStore dataStore) throws IOException {
        logger.info("Retrieving Type List from DataStore...");
        return dataStore.getTypeNames();
    }

    /**
     * Returns a list of FeatureTypes in a GeoTools DataStore. Forces an update of the list.
     *
     * @param dataStore
     * @return
     * @throws IOException
     */
    @CacheEvict("typeList")
    public String[] refreshTypeList(DataStore dataStore) throws IOException {
        return getTypeList(dataStore);
    }
}
