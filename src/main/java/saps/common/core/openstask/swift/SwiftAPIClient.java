/* (C)2020 */
package saps.common.core.openstask.swift;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.json.JSONException;

import saps.common.core.openstask.token.IdentityToken;
import saps.common.core.openstask.token.KeystoneV3IdentityRequestHelper;
import saps.common.core.storage.exceptions.InvalidPropertyException;
import saps.common.utils.SapsPropertiesConstants;
import saps.common.utils.SapsPropertiesUtil;

public class SwiftAPIClient implements SwiftAPI {

  private static final Logger LOGGER = Logger.getLogger(SwiftAPIClient.class);
  private static final String CONTAINER_URL_PATTERN = "%s/%s?path=%s";

  private final String swiftUrl;

  private IdentityToken token;

  public SwiftAPIClient(Properties properties)
      throws InvalidPropertyException, IOException, JSONException {
    if (!checkProperties(properties))
      throw new InvalidPropertyException(
          "Error on validate the file. Missing properties for start Swift API Client.");

    String projectId = properties.getProperty(SapsPropertiesConstants.Openstack.PROJECT_ID);
    String userId = properties.getProperty(SapsPropertiesConstants.Openstack.USER_ID);
    String userPassword = properties.getProperty(SapsPropertiesConstants.Openstack.USER_PASSWORD);
    String tokenAuthUrl =
        properties.getProperty(SapsPropertiesConstants.Openstack.IdentityService.API_URL);
    swiftUrl = properties.getProperty(SapsPropertiesConstants.Openstack.ObjectStoreService.API_URL);

    this.token =
        KeystoneV3IdentityRequestHelper.createIdentityToken(
            tokenAuthUrl, projectId, userId, userPassword);
  }

  public SwiftAPIClient(String projectId, String userId, String userPassword, String tokenAuthUrl,
      String swiftUrl) throws InvalidPropertyException, IOException, JSONException {
    this.swiftUrl = swiftUrl;
    this.token =
        KeystoneV3IdentityRequestHelper.createIdentityToken(
            tokenAuthUrl, projectId, userId, userPassword);
  }

  private boolean checkProperties(Properties properties) {
    String[] propertiesSet = {
      SapsPropertiesConstants.Openstack.PROJECT_ID,
      SapsPropertiesConstants.Openstack.USER_ID,
      SapsPropertiesConstants.Openstack.USER_PASSWORD,
      SapsPropertiesConstants.Openstack.IdentityService.API_URL,
      SapsPropertiesConstants.Openstack.ObjectStoreService.API_URL
    };

    return SapsPropertiesUtil.checkProperties(properties, propertiesSet);
  }

  public boolean createContainer(String containerName) throws Exception {
    LOGGER.debug("Creating container " + containerName);
    ProcessBuilder builder =
        new ProcessBuilder(
            "swift",
            "--os-auth-token",
            token.getAccessId(),
            "--os-storage-url",
            swiftUrl,
            "post",
            containerName);

    LOGGER.debug("Executing command " + builder.command());

    Process p = builder.start();
    p.waitFor();

    return p.exitValue() != 0;
  }

  public boolean uploadObject(String containerName, File file, String pseudFolder) throws Exception {
    String completeFileName = pseudFolder + File.separator + file.getName();

    LOGGER.debug("Uploading " + completeFileName + " to " + containerName);
    ProcessBuilder builder =
        new ProcessBuilder(
            "swift",
            "--os-auth-token",
            token.getAccessId(),
            "--os-storage-url",
            swiftUrl,
            "upload",
            containerName,
            file.getAbsolutePath(),
            "--object-name",
            completeFileName);
            
    LOGGER.debug("Executing command " + builder.command());

    Process p = builder.start();
    p.waitFor();

    return p.exitValue() == 0;
  }

  public boolean deleteObject(String containerName, String filePath) throws Exception {
    LOGGER.debug("Deleting " + filePath + " from " + containerName);

    ProcessBuilder builder =
        new ProcessBuilder(
            "swift",
            "--os-auth-token",
            token.getAccessId(),
            "--os-storage-url",
            swiftUrl,
            "delete",
            containerName,
            filePath);

    LOGGER.debug("Executing command " + builder.command());
            
    Process p = builder.start();
    p.waitFor();

    return p.exitValue() == 0;
  }

  public boolean deleteObjects(String containerName, String prefix) throws Exception {
    LOGGER.debug("Deleting objects with " + prefix + " prefix from " + containerName);

    ProcessBuilder builder =
        new ProcessBuilder(
            "swift",
            "--os-auth-token",
            token.getAccessId(),
            "--os-storage-url",
            swiftUrl,
            "delete",
            containerName,
            "--prefix",
            prefix);

    LOGGER.debug("Executing command " + builder.command());
            
    Process p = builder.start();
    p.waitFor();

    return p.exitValue() == 0;
  }

  public boolean downloadObjects(String containerName, String prefix) throws Exception {
    LOGGER.debug("Download objects with " + prefix + " prefix from " + containerName);

    ProcessBuilder builder =
        new ProcessBuilder(
            "swift",
            "--os-auth-token",
            token.getAccessId(),
            "--os-storage-url",
            swiftUrl,
            "download",
            containerName,
            "--prefix",
            prefix);

    LOGGER.debug("Executing command " + builder.command());
            
    Process p = builder.start();
    p.waitFor();

    return p.exitValue() == 0;
  }

  public List<String> listObjects(String containerName, String dirPath) throws IOException {
    List<String> files = new ArrayList<>();
    String url = String.format(CONTAINER_URL_PATTERN, swiftUrl, containerName, dirPath);
    HttpClient client = HttpClients.createDefault();
    HttpGet httpget = new HttpGet(url);
    httpget.addHeader("X-Auth-Token", token.getAccessId());
    HttpResponse response = client.execute(httpget);
    if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK
        && response.getStatusLine().getStatusCode() != HttpStatus.SC_NO_CONTENT) {
      throw new IOException(
          "The request to list files on object storage was failed: "
              + EntityUtils.toString(response.getEntity()));
    }
    if (Objects.nonNull(response.getEntity())) {
      files = Arrays.asList(EntityUtils.toString(response.getEntity()).split("\n"));
    }
    return files;
  }

  public boolean existsObject(String containerName, String basePath, String name)
      throws IOException {
    List<String> files = this.listObjects(containerName, basePath);
    for (String filePath : files) {
      try {
        if (Paths.get(filePath).getFileName().toString().equals(name)) {
          return true;
        }
      } catch (NullPointerException np) {
        LOGGER.error("Error while checking if task exists", np);
        return false;
      }
    }
    return false;
  }
}
