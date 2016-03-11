package org.opengeoportal.solr;

/**
 * POJO to contain OGP record info retrieved from Solr.
 *
 * Created by cbarne02 on 3/3/16.
 */
public class OgpRecord {
    private String id;
    private String typeName;
    private String dataType;
    private String metadataText;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getMetadataText() {
        return metadataText;
    }

    public void setMetadataText(String metadataText) {
        this.metadataText = metadataText;
    }

}
