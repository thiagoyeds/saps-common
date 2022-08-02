package saps.common.core.storage.temporary.nfs;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.commons.io.FileUtils;
import saps.common.core.model.SapsImage;
import saps.common.core.storage.temporary.TemporaryStorage;
import saps.common.core.storage.exceptions.InvalidPropertyException;
import saps.common.utils.SapsPropertiesConstants;
import saps.common.utils.SapsPropertiesUtil;


public class NfsTemporaryStorage implements TemporaryStorage {
    
    private static final Logger LOGGER = Logger.getLogger(NfsTemporaryStorage.class);

    private String tempStoragePath;

    public NfsTemporaryStorage(Properties properties) throws InvalidPropertyException {
        if (!checkProperties(properties))
        throw new InvalidPropertyException(
            "Error on validate the file. Missing properties for start Saps Controller.");

        this.tempStoragePath = properties.getProperty(SapsPropertiesConstants.SAPS_TEMP_STORAGE_PATH);
    }

    private boolean checkProperties(Properties properties) {
        String[] propertiesSet = {
            SapsPropertiesConstants.SAPS_TEMP_STORAGE_PATH,
        };

        return SapsPropertiesUtil.checkProperties(properties, propertiesSet);
    }

    @Override
    public boolean prepareToArchive(SapsImage task) throws Exception {
        return true;
    }

    @Override
    public boolean delete(SapsImage task) throws Exception {
        String taskDirPath = tempStoragePath + File.separator + task.getTaskId();

        File taskDir = new File(taskDirPath);
        if (!taskDir.exists() || !taskDir.isDirectory()) {
            LOGGER.error("Path " + taskDirPath + " does not exist or is not a directory!");
            return false;
        }

        try {
            FileUtils.deleteDirectory(taskDir);
        } catch (IOException e) {
            LOGGER.error("Error while delete task [" + task.getTaskId() + "] files from disk: ", e);
            return false;
        }

        return true;
    }
    
}
