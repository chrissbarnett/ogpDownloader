package org.opengeoportal.util.compress;

import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Pure java implementation of Zipper.
 *
 * Created by cbarne02 on 3/7/16.
 */
@Component
public class ZipperJava implements Zipper {

    private static Logger LOGGER = LoggerFactory.getLogger(ZipperJava.class.getName());
    /**
     * Returns a zip file system
     * @param archive to construct the file system from
     * @param create true if the zip file should be created
     * @return a zip file system
     * @throws IOException
     */
    private static FileSystem createZipFileSystem(Path archive, boolean create)
            throws IOException {
        // convert the filename to a URI
        final URI uri = URI.create("jar:file:" + archive.toUri().getPath());

        final Map<String, String> env = new HashMap<>();
        if (create) {
            env.put("create", "true");
        }
        return FileSystems.newFileSystem(uri, env);
    }

    @Override
    public boolean isAvailable(){
        return true;
    }

    @Override
    public boolean compress(Path archive, List<Path> items) {

        try (FileSystem zipFileSystem = createZipFileSystem(archive, true)) {
            final Path root = zipFileSystem.getPath("/");

            //iterate over the files we need to add
            for (Path src : items) {
                //add a file to the zip file system
                if (!Files.isDirectory(src)) {
                    //flattens directory structure
                    final Path dest = zipFileSystem.getPath(root.toString(),
                            src.getFileName().toString());
                    final Path parent = dest.getParent();
                    if (Files.notExists(parent)) {
                        LOGGER.debug("Creating directory %s\n", parent);
                        Files.createDirectories(parent);
                    }
                    Files.copy(src, dest, StandardCopyOption.REPLACE_EXISTING);
                } else {
                    //for directories, walk the file tree
                    Files.walkFileTree(src, new SimpleFileVisitor<Path>() {
                        @Override
                        public FileVisitResult visitFile(Path file,
                                                         BasicFileAttributes attrs) throws IOException {
                            final Path dest = zipFileSystem.getPath(root.toString(),
                                    file.toString());
                            Files.copy(file, dest, StandardCopyOption.REPLACE_EXISTING);
                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult preVisitDirectory(Path dir,
                                                                 BasicFileAttributes attrs) throws IOException {
                            final Path dirToCreate = zipFileSystem.getPath(root.toString(),
                                    dir.toString());
                            if (Files.notExists(dirToCreate)) {
                                LOGGER.debug("Creating directory %s\n", dirToCreate);
                                Files.createDirectories(dirToCreate);
                            }
                            return FileVisitResult.CONTINUE;
                        }
                    });
                }
            }
            return true;

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

    }

}
