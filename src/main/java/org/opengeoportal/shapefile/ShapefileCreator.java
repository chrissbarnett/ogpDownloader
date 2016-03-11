package org.opengeoportal.shapefile;

import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Creates the actual File/directory for the Shapefile, as well as the GeoTools DataStore
 *
 * Created by cbarne02 on 3/1/16.
 */

public class ShapefileCreator {

    private File outfile;
    private ShapefileDataStore shpDataStore;

    private Logger logger = LoggerFactory.getLogger(ShapefileCreator.class);

    public Path create(String typeName) throws IOException {
        Path dirPath = Files.createTempDirectory(typeName);

        Path p = Paths.get(dirPath.toString(), typeName + ".shp");
        setOutfile(p.toFile());
        createShapefileDatastore();
        return p;
    }

    public ShapefileDataStore getDataStore() {
        return shpDataStore;
    }

    private void setOutfile(File f){
        outfile = f;
    }

    private void createShapefileDatastore(){
        ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();

        Map<String, Serializable> params = new HashMap<String, Serializable>();
        try {
            logger.debug("Writing Shapefile to: ");
            logger.debug(outfile.toURI().toURL().toString());
            params.put("url", outfile.toURI().toURL());
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        params.put("create spatial index", Boolean.TRUE);

        try {
            shpDataStore = (ShapefileDataStore) dataStoreFactory.createNewDataStore(params);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


}
