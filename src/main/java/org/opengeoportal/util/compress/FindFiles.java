package org.opengeoportal.util.compress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

/**
 * recursively search a directory tree to find files
 *
 * @author cbarne02
 *
 */
public class FindFiles extends SimpleFileVisitor<Path>{

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final PathMatcher matcher;

    private final Boolean matchAll;

    private List<Path> files = new ArrayList<>();


    public FindFiles(String match, boolean matchAll) {
        this.matchAll = matchAll;
        matcher = FileSystems.getDefault().getPathMatcher("glob:" + match);
    }

    // Compares the glob pattern against
    // the file or directory name.
    boolean find(Path file) throws IOException {
        Path name = file.getFileName();
        if (name != null){
            if (matcher.matches(name)) {
                //add to matches
                files.add(file);
                return true;
            }
        }
        return false;
    }

    public List<Path> getFiles() { return files;}

    // Invoke the pattern matching
    // method on each file.
    @Override
    public FileVisitResult visitFile(Path file,
                                     BasicFileAttributes attrs) {
        try {
            boolean found = find(file);

            //terminate walk on first match if matchAll is false
            if (!matchAll && found) {
                return FileVisitResult.TERMINATE;
            }

        } catch (IOException e) {
            visitFileFailed(file, e);
        }

        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file,
                                           IOException e) {
        logger.error(e.getMessage());
        return FileVisitResult.CONTINUE;
    }

}
