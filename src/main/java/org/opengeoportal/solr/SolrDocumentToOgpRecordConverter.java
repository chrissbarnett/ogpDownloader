package org.opengeoportal.solr;

import org.apache.solr.common.SolrDocument;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * Converts SolrJ SolrDocument to internally used OgpRecord
 * Created by cbarne02 on 3/3/16.
 */
@Component
public class SolrDocumentToOgpRecordConverter implements Converter<SolrDocument, OgpRecord> {

    @Override
    public OgpRecord convert(SolrDocument doc) {
        //These are the only values we care about
        OgpRecord record = new OgpRecord();
        record.setId(get(doc, "LayerId"));

        record.setTypeName(get(doc, "Name"));

        record.setDataType(get(doc, "DataType"));

        record.setMetadataText(get(doc, "FgdcText"));

        return record;
    }


    private static String get(SolrDocument solrDocument, String value) {
        Object val = solrDocument.get(value);
        if (val == null) {
            return null;
        } else {
            String val$ = (String) val;
            if (val$.isEmpty()) {
                return null;
            }
            return val$;
        }
    }

}

