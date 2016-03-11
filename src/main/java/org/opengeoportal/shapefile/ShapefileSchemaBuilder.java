package org.opengeoportal.shapefile;

import org.geotools.data.FeatureReader;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.GeometryDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * Converts a FeatureType's schema to one compatible with the Shapefile format.
 *
 * Created by cbarne02 on 3/1/16.
 */
public class ShapefileSchemaBuilder {
    private static Logger logger = LoggerFactory.getLogger(ShapefileSchemaBuilder.class);

    public static SimpleFeatureType translateSchema(FeatureReader<SimpleFeatureType,SimpleFeature> reader){
        SimpleFeatureType schema = reader.getFeatureType();
        return translateSchema(schema);
    }

    public static SimpleFeatureType translateSchema(SimpleFeatureType schema){

        /*
         * The Shapefile format has a couple limitations: - "the_geom" is always
         * first, and used for the geometry attribute name - "the_geom" must be of
         * type Point, MultiPoint, MuiltiLineString, MultiPolygon - Attribute
         * names are limited in length - Not all data types are supported (example
         * Timestamp represented as Date)
         *
         * Because of this we have to rename the geometry element and then rebuild
         * the features to make sure that it is the first attribute.
         */
        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();

        builder.setName(schema.getTypeName());


        GeometryDescriptor geomDesc = schema.getGeometryDescriptor();
        if (geomDesc != null){
            builder.setCRS(geomDesc.getCoordinateReferenceSystem()); // <- Coordinate reference system

            // add attributes in order
            builder.add("the_geom", geomDesc.getType().getBinding());
        } else {
            //TODO: add behavior for missing GeometryDescriptor
        }


        List<AttributeDescriptor> attributes = schema.getAttributeDescriptors();
        for (AttributeDescriptor attrib : attributes) {
            AttributeType type = attrib.getType();
            if (type.getBinding().equals(String.class)){
                logger.debug("adding string attribute: " + attrib.getLocalName());
                builder = addStringAttribute(builder, attrib.getLocalName());
            } else if (type.getBinding().equals(BigDecimal.class)){
                logger.debug("adding float attribute: " + attrib.getLocalName());
                builder = addFloatAttribute(builder, attrib.getLocalName());
            } else {
                logger.info("another type: " + type.getBinding().toString() + ": " + attrib.getLocalName());
            }
        }

        // build the type
        final SimpleFeatureType FT = builder.buildFeatureType();

        return FT;
    }

    private static SimpleFeatureTypeBuilder addStringAttribute(SimpleFeatureTypeBuilder builder, String name){
        builder.add(name, String.class);
        return builder;
    }

    private static SimpleFeatureTypeBuilder addLongIntAttribute(SimpleFeatureTypeBuilder builder, String name){
        builder.length(9).add(name, Integer.class);
        return builder;
    }

    private static SimpleFeatureTypeBuilder addFloatAttribute(SimpleFeatureTypeBuilder builder, String name){
        builder.length(13).add(name, Float.class);
        return builder;
    }

    private static SimpleFeatureTypeBuilder addDateAttribute(SimpleFeatureTypeBuilder builder, String name){
        builder.length(8).add(name, Date.class);
        return builder;
    }

}
