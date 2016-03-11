package org.opengeoportal.datastore;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * Injects DataStore connection parameters from properties file.
 *
 * Created by cbarne02 on 1/26/16.
 */
@Configuration
public class DataStoreConnectionSettings {
    private Map<String, Object> datastore = new HashMap<>();


    @Bean
    @ConfigurationProperties(prefix = "datastore")
    public Map<String, Object> getParams(){
        return datastore;
    }

}
