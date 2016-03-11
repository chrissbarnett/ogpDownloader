package org.opengeoportal.shapefile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.geotools.data.*;
import org.geotools.data.shapefile.ShapefileDataStore;

import org.geotools.data.simple.SimpleFeatureSource;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;


import static org.opengeoportal.shapefile.ShapefileSchemaBuilder.translateSchema;

/**
 * Writes features from the DataStore to the Shapefile.
 *
 * Some parts of this class (specifically the shapefile schema transform) based on Ian
 */

public class ShapefileBuilder {

    @Value("${data.fetchsize}")
    private int MAX_FEATURES;

    private ShapefileDataStore shpDataStore;

    private ConcurrentLinkedQueue<Integer> queue = new ConcurrentLinkedQueue();

    @Autowired
    private ObjectFactory<ShapefileCreator> shapefileCreatorFactory;

    private boolean schemaSet = false;

    private SimpleFeatureType schema;

    private Path out;

    private Logger logger = LoggerFactory.getLogger(ShapefileBuilder.class);


    public void build(String typeName) throws IOException {
        logger.debug("Building shapefile...");
        ShapefileCreator shapefileCreator = shapefileCreatorFactory.getObject();
        out = shapefileCreator.create(typeName);
        shpDataStore = shapefileCreator.getDataStore();

    }

    public Path write(DataStore src, Query q) throws IOException {
        pagingPlanner(src, q);

        writeFeatures(src, q);

        return out;
    }

    private void writeSchema(FeatureReader<SimpleFeatureType,SimpleFeature> reader) throws IOException {
        if (shpDataStore == null) {
            throw new IllegalStateException("Datastore can not be null when writing");
        }

        schema = translateSchema(reader);

        shpDataStore.createSchema(schema);

        schemaSet = true;
    }

    private boolean hasNext(){
        return queue.peek() != null;
    }

    private void writeFeatures(DataStore src, Query q) throws IOException {

        while (hasNext()) {
            Integer idx = queue.poll();

            q.setStartIndex(idx);
            FeatureReader reader = src.getFeatureReader(q, Transaction.AUTO_COMMIT);


            if (!schemaSet) {
                writeSchema(reader);
            }

            writeFromCollectionToShape(reader, schema);
        }

        close();
    }

    private void pagingPlanner(DataStore src, Query q) throws IOException {

        SimpleFeatureSource featureSource = src.getFeatureSource(q.getTypeName());

        int count = featureSource.getCount(q);

        q.setMaxFeatures(MAX_FEATURES);

        int i = 0;
        while (i <= count){
            queue.add(i);
            i += MAX_FEATURES;
        }

    }


    private SimpleFeature transformFeature(SimpleFeature f, SimpleFeatureType newSchema){
        SimpleFeature reType = DataUtilities.reType(newSchema, f, true);
        //set the default Geom (the_geom) from the original Geom

        //reType.setAttribute("the_geom", f.getAttribute(srcGeomAttrib));
        reType.setDefaultGeometryProperty(f.getDefaultGeometryProperty());
        return reType;
    }


    public boolean writeFromCollectionToShape(FeatureReader<SimpleFeatureType,SimpleFeature> reader, SimpleFeatureType schema) throws IOException {
        FeatureWriter<SimpleFeatureType, SimpleFeature> writer = null;

        try {

            writer = shpDataStore.getFeatureWriterAppend(schema.getTypeName(), Transaction.AUTO_COMMIT);

            while (reader.hasNext()) {
                SimpleFeature f = reader.next();

                SimpleFeature copy = writer.next();
                copy.setDefaultGeometry(f.getDefaultGeometry());
                //will this be a problem if the attributes change names? (truncation, etc?)
                copy.setAttributes(f.getAttributes());
                writer.write();
            }
             //how to make this adaptive? or just configurable
            return true;

        } catch (Exception problem) {
            problem.printStackTrace();
            throw new IOException("Problem writing features.");
        } finally {

            try {
                reader.close();
            } catch (Exception e){
                e.printStackTrace();
            }
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (Exception e){
                e.printStackTrace();
            }

        }

    }

    public void close(){
        if (shpDataStore != null) {
            logger.info("Disposing shapefile data store");
            shpDataStore.dispose();
        }
    }

}
