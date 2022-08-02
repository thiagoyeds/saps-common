/* (C)2020 */
package saps.common.core.openstack.swift;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import org.json.JSONException;
import org.junit.Ignore;
import org.junit.Test;

import saps.common.core.openstask.swift.SwiftAPIClient;
import saps.common.core.storage.exceptions.InvalidPropertyException;
import saps.common.utils.SapsPropertiesConstants;

public class SwiftAPIClientTest {

  private static final String CONFIG_FILE = "src/test/resources/config/archiver/normal-mode.conf";

  private Properties loadConfigFile(String path) throws IOException {
    Properties properties = new Properties();
    try (FileInputStream input = new FileInputStream(path)) {
      properties.load(input);
      return properties;
    }
  }

  @Test
  @Ignore
  public void testListFiles() throws IOException, InvalidPropertyException, JSONException {
    Properties properties = loadConfigFile(CONFIG_FILE);
    SwiftAPIClient client = new SwiftAPIClient(properties);
    String containerName =
        properties.getProperty(SapsPropertiesConstants.Openstack.ObjectStoreService.CONTAINER_NAME);
    String path = properties.getProperty(SapsPropertiesConstants.PERMANENT_STORAGE_TASKS_DIR);
    client.listObjects(containerName, path);
  }
}
