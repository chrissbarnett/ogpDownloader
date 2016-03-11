package org.opengeoportal.util.compress;

import java.nio.file.Path;
import java.util.List;

/**
 * Interface for archive classes.
 * Created by cbarne02 on 3/7/16.
 */
public interface Zipper {
    /**
     * Checks to see whether a particular zipper implementation is available.
     * @return
     */
    boolean isAvailable();

    /**
     * Zips items to the given archive.
     *
     * @param archive
     * @param items
     * @return
     */
    boolean compress(Path archive, List<Path> items);
}
