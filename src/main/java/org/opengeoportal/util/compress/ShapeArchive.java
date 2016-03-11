package org.opengeoportal.util.compress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;


/**
 * Zips a shapefile.
 *
 * Created by cbarne02 on 3/7/16.
 */
public class ShapeArchive {
    @Autowired
    private Zipper7Zip zipper7Zip;

    @Autowired
    private ZipperJava zipperJava;

    private Logger logger = LoggerFactory.getLogger(ShapeArchive.class);

    public Path create(Path path) throws IOException {
        //if a directory, walk and add files to archive.
        //if a file, Path array contains just one file.

        FindFiles finder = new FindFiles("*", true);
        Files.walkFileTree(path, finder);
        List<Path> files = finder.getFiles();

        if (files.size() == 0){
            throw new IOException("No files found!");
        }

        Path arch = createArchiveFile(extractName(findShapeFile(path)));

        if (zipper7Zip.isAvailable()){
            zipper7Zip.compress(arch, files);
        } else {
            zipperJava.compress(arch, files);
        }

        logger.info("Archive at: " + arch.toAbsolutePath().toString());

        return arch;

    }

    private static String extractName(String string){
        return string.substring(0, string.lastIndexOf(".shp"));
    }

    private static String findShapeFile(Path path) throws IOException {
        if (Files.isDirectory(path)){
            FindFiles finder = new FindFiles("*.shp", false);
            Files.walkFileTree(path, finder);
            for (Path p: finder.getFiles()){
                return p.getFileName().toString();
            }
        } else {
            return path.getFileName().toString();
        }
        throw new IOException("No shapefile found!");
    }

    private static Path createArchiveFile(String name) throws IOException {

        Path dirPath = Files.createTempDirectory(name);
        Path p = Paths.get(dirPath.toString(), name + ".zip");
        return p;
    }
}
