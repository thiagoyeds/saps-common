/* (C)2020 */
package saps.common.core.storage.permanent.swift;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.io.FileInputStream;
import java.util.Properties;
import org.junit.Ignore;
import org.junit.Test;

import saps.common.core.openstask.token.IdentityToken;
import saps.common.core.openstask.token.KeystoneV3IdentityRequestHelper;
import saps.common.utils.SapsPropertiesConstants;

public class KeystoneV3IdentityPluginTest {

  private static final String CONFIG_FILE_PATH = "src/test/resources/dispatcher-test.conf";

  @Test
  @Ignore
  public void createToken() throws Exception {
    Properties properties = new Properties();

    try (FileInputStream input = new FileInputStream(CONFIG_FILE_PATH)) {
      properties.load(input);

      String url =
          properties.getProperty(SapsPropertiesConstants.Openstack.IdentityService.API_URL);
      String projectId = properties.getProperty(SapsPropertiesConstants.Openstack.PROJECT_ID);
      String userId = properties.getProperty(SapsPropertiesConstants.Openstack.USER_ID);
      String password = properties.getProperty(SapsPropertiesConstants.Openstack.USER_PASSWORD);

      IdentityToken token =
          KeystoneV3IdentityRequestHelper.createIdentityToken(url, projectId, userId, password);
      assertToken(token);
    }
  }

  private void assertToken(IdentityToken token) {
    assertNotNull(token.getAccessId());
    assertFalse(token.getAccessId().isEmpty());
  }
}
