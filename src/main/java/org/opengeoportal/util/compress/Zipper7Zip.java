package org.opengeoportal.util.compress;

import net.sf.sevenzipjbinding.*;
import net.sf.sevenzipjbinding.impl.OutItemFactory;
import net.sf.sevenzipjbinding.impl.RandomAccessFileOutStream;
import net.sf.sevenzipjbinding.util.ByteArrayStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Compresses using 7zip native c++ libraries to handle archiving.
 * Created by cbarne02 on 3/2/16.
 */
@Component
public class Zipper7Zip implements Zipper {

    private Logger logger = LoggerFactory.getLogger(Zipper7Zip.class);

    private boolean use7Zip = false;

    private List<Path> items = new ArrayList<>();


    /**
     * Determine if the 7-Zip library is installed and configured properly.
     * If not, fall back to pure Java implementation. In either case, should
     * spawn a new object to do the actual zipping. Input should be a list of
     * paths. Archive name should be derived from the .shp file name.
     */
    @PostConstruct
    public void init() {
        try {
            SevenZip.initSevenZipFromPlatformJAR();
            logger.info("7-Zip-JBinding library was initialized");
            use7Zip = true;
        } catch (SevenZipNativeInitializationException e) {
            e.printStackTrace();
            logger.warn("7-Zip-JBinding Library not initialized");
            use7Zip = false;
        }
    }

    /**
     * Query whether or not the 7zip libraries are available.
     * @return boolean
     */
    @Override
    public boolean isAvailable(){
        return use7Zip;
    }


    /**
     * compresses items to archive using 7zip.
     * @param archive
     * @param items
     */
    @Override
    public boolean compress(Path archive, List<Path> items) {

        if (!isAvailable()){
            logger.error("7-zip libraries not available. archive operation failed.");
            return false;
        }

        boolean success = false;
        RandomAccessFile raf = null;
        IOutCreateArchiveZip outArchive = null;
        try {

            raf = new RandomAccessFile(archive.toFile(), "rw");

            // Open out-archive object
            outArchive = SevenZip.openOutArchiveZip();

            // Configure archive
            outArchive.setLevel(5);

            // Create archive
            ArchiveCallback callback = new ArchiveCallback();
            callback.setItems(items);

            outArchive.createArchive(new RandomAccessFileOutStream(raf),
                    items.size(), callback);

            success = true;
        } catch (SevenZipException e) {
            logger.error("7z-Error occurs:");
            // Get more information using extended method
            e.printStackTraceExtended();
        } catch (Exception e) {
            logger.error("Error occurs: " + e);
        } finally {
            if (outArchive != null) {
                try {
                    outArchive.close();
                } catch (IOException e) {
                    logger.error("Error closing archive: " + e);
                    success = false;
                }
            }
            if (raf != null) {
                try {
                    raf.close();
                } catch (IOException e) {
                    logger.error("Error closing file: " + e);
                    success = false;
                }
            }
        }
        if (success) {
            logger.debug("Compression operation succeeded");
        }

        return success;
    }


    /**
     * The callback provides information about items required for archiving, methods to get data streams.
     */
    private final class ArchiveCallback
            implements IOutCreateCallback<IOutItemZip> {

        private List<Path> items;

        public void setItems(List<Path> items){
            this.items = items;
        }

        public void setOperationResult(boolean operationResultOk)
                throws SevenZipException {
            // Track each operation result here
        }

        public void setTotal(long total) throws SevenZipException {
            // Track operation progress here
        }

        public void setCompleted(long complete) throws SevenZipException {
            // Track operation progress here
        }

        /**
         * get/set item metadata
         * @param index
         * @param outItemFactory
         * @return
         */
        public IOutItemZip getItemInformation(int index,
                                              OutItemFactory<IOutItemZip> outItemFactory) {
            int attr = PropID.AttributesBitMask.FILE_ATTRIBUTE_UNIX_EXTENSION;
            Path p = items.get(index);

            IOutItemZip item = outItemFactory.createOutItem();

            if(Files.isDirectory(p)){
                // Directory
                item.setPropertyIsDir(true);
                attr |= PropID.AttributesBitMask.FILE_ATTRIBUTE_DIRECTORY;
                attr |= 0x81ED << 16; // permissions: drwxr-xr-x
            } else {
                try {
                    item.setDataSize(Files.size(p));
                } catch (IOException e) {
                    logger.error("unable to determine size of {}", p.getFileName().toString());
                    e.printStackTrace();
                }
                attr |= 0x81a4 << 16; // permissions: -rw-r--r--

            }

            //flatten directory structure
            item.setPropertyPath(p.getFileName().toString());

            item.setPropertyAttributes(attr);

            return item;
        }

        /**
         * get the data stream fo
         * @param i
         * @return
         * @throws SevenZipException
         */
        public ISequentialInStream getStream(int i) throws SevenZipException {
            byte[] content = new byte[0];
            try {
                content = Files.readAllBytes(items.get(i));
                if (content == null){
                    return null;
                }
            } catch (IOException e) {
                e.printStackTrace();
                throw new SevenZipException(e.getMessage());
            }
            return new ByteArrayStream(content, true);
        }
    }



}
