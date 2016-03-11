package org.opengeoportal.downloader;

import org.geotools.data.*;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.opengeoportal.datastore.DataStoreManager;
import org.opengeoportal.datastore.DataStoreQuery;
import org.opengeoportal.messaging.DownloadRequest;
import org.opengeoportal.shapefile.ShapefileBuilder;
import org.opengeoportal.solr.IdQuery;
import org.opengeoportal.solr.OgpRecord;
import org.opengeoportal.util.compress.ShapeArchive;
import org.opengeoportal.xml.MetadataFileWriter;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.geometry.BoundingBox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Service that abstracts the entire process of ShapeFile creation, data retrieval, and compression. Additionally adds
 * a copy of an XML metadata document from an OGP instance.
 *
 * Created by cbarne02 on 2/8/16.
 */
@Service
public class ShapefileRequestService implements VectorDownloader {

    @Autowired
    private ObjectFactory<DataStoreQuery> dataStoreQueryFactory;

    @Autowired
    private ObjectFactory<ShapefileBuilder> shapefileBuilderFactory;

    @Autowired
    private DataStoreManager dsManager;

    @Autowired
    private ObjectFactory<IdQuery> queryFactory;

    @Autowired
    private ObjectFactory<ShapeArchive> shapeArchiveFactory;

    @Autowired
    private ObjectFactory<MetadataFileWriter> metadataWriterFactory;

    private Logger logger = LoggerFactory.getLogger(ShapefileRequestService.class);



    @Override
    public String findFeatureType(String requestedFeatureType) throws Exception {
        String typeName = null;
        try {
            typeName = dsManager.findTypeName(requestedFeatureType, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (typeName == null){
            throw new Exception("TypeName '" + requestedFeatureType + "' not found.");
        }
        return typeName;

    }



    private void logint(String desc, Integer integer){
        logger.info(desc + ": " + Integer.toString(integer));
    }

    private void printDSInfo(DataStore src, Query q) throws IOException {

        //TODO: do we need to validate here? If so, what?
        SimpleFeatureSource featureSource = src.getFeatureSource(q.getTypeName());

        ResourceInfo info = featureSource.getInfo();
        logger.info(info.getName());
        logger.info(info.getDescription());
        logger.info(info.getTitle());
        logger.info(info.getBounds().toString());
/*        if (info.getCRS() != null) {
            logger.info(info.getCRS().toWKT());
        } else {
            if ()
            throw new IOException("The specified layer has no CRS. Can't continue.");
        }*/

        logger.info(featureSource.getName().getLocalPart());
        SimpleFeatureType s = featureSource.getSchema();
        if (s.getGeometryDescriptor() != null) {
            logger.info(s.getGeometryDescriptor().getLocalName());
        }

        int count = featureSource.getCount(q);
        logint("feature count", count);

        logint("max features", q.getMaxFeatures());

    }

    /**
     * Downloads a shapefile. Returns the Path to the created zip file.
     *
     * @param request
     * @return
     * @throws Exception
     */
    @Override
    public Path download(DownloadRequest request) throws Exception {
        if (request.getMaxx() == null){
            return download(request.getId());
        } else {
            ReferencedEnvelope envelope = new ReferencedEnvelope(request.getMinx(), request.getMaxx(),
                    request.getMiny(), request.getMaxy(), CRS.decode("EPSG:4326"));
            return download(request.getId(), envelope);
        }
    }


    /**
     * Downloads a shapefile given an OGP Layer Id.
     *
     * Looks up the OGP record given an OGP Layer Id. Gets TypeName from the record (field "Name") and the XML metadata
     * as a String. Looks up the FeatureType in the DataStore, do the download, create and insert a metadata file, then
     * archive.
     *
     * @param id
     * @return
     * @throws Exception
     */
    @Override
    public Path download(String id) throws Exception {
        logger.debug("Requested id: " + id);
        IdQuery query = queryFactory.getObject();
        OgpRecord record = query.find(id);
        String typeName = record.getTypeName();
        logger.info("Registered OGP typeName: " + typeName);
        typeName = findFeatureType(typeName);
        logger.info("Matched datastore typeName: " + typeName);
        Query q = new Query(typeName);
        DataStore src = dsManager.getDataStore();
        //printDSInfo(src, q);
        Path shapePath = doDownload(src, q);
        return insertMetadataAndArchive(shapePath, record);
    }

    /**
     * Downloads a shapefile given an OGP Layer Id, with features selected by bounding box.
     *
     * Looks up the OGP record given an OGP Layer Id. Gets TypeName from the record (field "Name") and the XML metadata
     * as a String. Looks up the FeatureType in the DataStore, do the download, create and insert a metadata file, then
     * archive.
     *
     * @param id
     * @param bbox
     * @return
     * @throws Exception
     */
    @Override
    public Path download(String id, BoundingBox bbox) throws Exception {
        logger.info("bbox Requested id: " + id);
        OgpRecord record = queryFactory.getObject().find(id);
        String typeName = record.getTypeName();
        logger.info("Registered OGP typeName: " + typeName);
        typeName = findFeatureType(typeName);
        logger.info("Matched datastore typeName: " + typeName);
        DataStore src = dsManager.getDataStore();
        String geomAttr = src.getSchema(typeName).getGeometryDescriptor().getLocalName();
        logger.info("creating query: geom attr: " + geomAttr);
        Query q = dataStoreQueryFactory.getObject().create(typeName, geomAttr, bbox);
        //printDSInfo(src, q);

        Path shapePath = doDownload(src, q);

        return insertMetadataAndArchive(shapePath, record);

    }

    /**
     * Builds a shapefile given a GeoTools DataStore and Query.
     *
     * @param src
     * @param q
     * @return
     * @throws IOException
     */
    private Path doDownload(DataStore src, Query q) throws IOException {

        ShapefileBuilder shapefileBuilder = shapefileBuilderFactory.getObject();
        shapefileBuilder.build(q.getTypeName());
        Path out = shapefileBuilder.write(src, q);
        return out;

    }



    /**
     * Writes XML metadata document retrieved from an OGP instance and Zips the shapefile.
     *
     * @param shapePath
     * @param record
     * @return
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws TransformerException
     * @throws SAXException
     */
    private Path insertMetadataAndArchive(Path shapePath, OgpRecord record) throws IOException,
            ParserConfigurationException, TransformerException, SAXException {
        String xmlName = shapePath.getFileName().toString() + ".xml";
        Path dir = shapePath.getParent();
        //writes XML metadata document retrieved from an OGP instance
        logger.info("Writing metadata...");
        metadataWriterFactory.getObject().write(record.getMetadataText(), Paths.get(dir.toAbsolutePath().toString(), xmlName));

        logger.info("Archiving shapefile...");
        //zip the shapefile
        return shapeArchiveFactory.getObject().create(dir);
    }


}
