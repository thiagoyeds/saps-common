package saps.common.core.storage.temporary.swift;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.json.JSONException;

import saps.common.core.model.SapsImage;
import saps.common.core.openstask.swift.SwiftAPI;
import saps.common.core.openstask.swift.SwiftAPIClient;
import saps.common.core.storage.temporary.TemporaryStorage;
import saps.common.core.storage.exceptions.InvalidPropertyException;
import saps.common.utils.SapsPropertiesConstants;
import saps.common.utils.SapsPropertiesUtil;

public class SwiftTemporaryStorage implements TemporaryStorage {

    private String swiftContainerName;

    private SwiftAPI swiftClient;

    public SwiftTemporaryStorage(Properties properties) throws InvalidPropertyException, IOException, JSONException {
        if (!checkProperties(properties))
        throw new InvalidPropertyException(
            "Error on validate the file. Missing properties for start Saps Controller.");

        String openstackAuthUrl = properties.getProperty(SapsPropertiesConstants.SWIFT_TEMPORARY_STORAGE_OPENSTACK_AUTH_URL);
        String openstackProjectId = properties.getProperty(SapsPropertiesConstants.SWIFT_TEMPORARY_STORAGE_OPENSTACK_PROJECT_ID);
        String openstackUserId = properties.getProperty(SapsPropertiesConstants.SWIFT_TEMPORARY_STORAGE_OPENSTACK_USER_ID);
        String openstackPassword = properties.getProperty(SapsPropertiesConstants.SWIFT_TEMPORARY_STORAGE_OPENSTACK_USER_PASSWORD);
        String openstackSwiftUrl = properties.getProperty(SapsPropertiesConstants.Openstack.ObjectStoreService.API_URL);
        this.swiftContainerName = properties.getProperty(SapsPropertiesConstants.SWIFT_TEMPORARY_STORAGE_CONTAINER_NAME);

        this.swiftClient = new SwiftAPIClient(openstackProjectId, openstackUserId, openstackPassword, openstackAuthUrl, openstackSwiftUrl);
    }

    private boolean checkProperties(Properties properties) {
        String[] propertiesSet = {
            SapsPropertiesConstants.SWIFT_TEMPORARY_STORAGE_OPENSTACK_AUTH_URL,
            SapsPropertiesConstants.SWIFT_TEMPORARY_STORAGE_OPENSTACK_PROJECT_ID,
            SapsPropertiesConstants.SWIFT_TEMPORARY_STORAGE_OPENSTACK_USER_ID,
            SapsPropertiesConstants.SWIFT_TEMPORARY_STORAGE_OPENSTACK_USER_PASSWORD,
            SapsPropertiesConstants.SWIFT_TEMPORARY_STORAGE_CONTAINER_NAME,
            SapsPropertiesConstants.Openstack.ObjectStoreService.API_URL,
        };

        return SapsPropertiesUtil.checkProperties(properties, propertiesSet);
    }

    // private static String GETTER_SCRIPT_PATTERN = "bash scripts/getter.sh %s %s %s %s %s %s %s";

    @Override
    public boolean prepareToArchive(SapsImage task) throws Exception {
        // String run = String.format(GETTER_SCRIPT_PATTERN, task.getTaskId(), this.swiftContainerName,
        //     this.openstackUsername, this.openstackPassword, this.openstackProjectName, this.openstackDomainName,
        //     this.openstackAuthUrl);

        // Process builder = new ProcessBuilder(run).start();
        // builder.waitFor();

        // return builder.exitValue() != 0;

        return this.swiftClient.downloadObjects(swiftContainerName, task.getTaskId());
    }

    // private static String CLEANER_SCRIPT_PATTERN = "bash scripts/cleaner.sh %s %s %s %s %s %s %s";

    @Override
    public boolean delete(SapsImage task) throws Exception {
        // String run = String.format(CLEANER_SCRIPT_PATTERN, task.getTaskId(), this.swiftContainerName,
        // this.openstackUsername, this.openstackPassword, this.openstackProjectName, this.openstackDomainName,
        // this.openstackAuthUrl);

        // Process builder = new ProcessBuilder(run).start();
        // builder.waitFor();

        // return builder.exitValue() != 0;

        File tempTaskDir = new File(task.getTaskId());
        if (tempTaskDir.exists()) {
            FileUtils.deleteDirectory(tempTaskDir);
        }

        return this.swiftClient.deleteObjects(swiftContainerName, task.getTaskId());
    }
    
}
