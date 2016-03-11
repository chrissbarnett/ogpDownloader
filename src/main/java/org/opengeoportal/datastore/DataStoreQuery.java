package org.opengeoportal.datastore;

import org.geotools.data.*;
import org.geotools.factory.CommonFactoryFinder;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.spatial.BBOX;
import org.opengis.geometry.BoundingBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Constructs GeoTools Query with a bounding box filter.
 *
 * Created by cbarne02 on 1/26/16.
 */
public class DataStoreQuery {
    private Logger logger = LoggerFactory.getLogger(DataStoreQuery.class);

    public Query create(String typeName, String geomAttr, BoundingBox bounds){
        logger.debug("Requested geometry attribute: " + geomAttr);
        logger.debug("Requested bounds: " + bounds.toString());
        BBOX filter = getFilter(geomAttr, bounds);

        return new Query(typeName, filter);
    }

    private BBOX getFilter(String geomAttr, BoundingBox bounds){
        FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2();
        BBOX filter = ff.bbox(ff.property(geomAttr), bounds);
        return filter;
    }


}
