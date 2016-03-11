package org.opengeoportal.solr;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Simple solr query that retrieves a record given the OGP layer id. Uses a converter to return an OgpRecord.
 *
 * Created by cbarne02 on 3/3/16.
 */

public class IdQuery {

    @Autowired
    private SolrClient solrClient;

    @Autowired
    private SolrDocumentToOgpRecordConverter converter;

    private Logger logger = LoggerFactory.getLogger(IdQuery.class);

    public OgpRecord find(String id) throws IOException, SolrServerException {
        logger.info("trying to find..." + id);
        SolrDocument doc = solrClient.getById(id);
        logger.debug(doc.getFieldNames().toString());
        return converter.convert(doc);
    }
}
