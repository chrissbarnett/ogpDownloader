package org.opengeoportal.solr;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/**
 * Inject the URL for an ogp solr endpoint, create an HttpSolrClient
 * Created by cbarne02 on 3/3/16.
 */
@Configuration
public class SolrConfig {

    @Value("${solr.url}")
    private String solr_url;

    @Bean
    public SolrClient getSolrClient(){
        return new HttpSolrClient(solr_url);
    }


}
