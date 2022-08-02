/* (C)2020 */
package saps.common.core.openstask.token;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.codec.Charsets;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.eclipse.jetty.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class KeystoneV3IdentityRequestHelper {

  public static final String PROJECT_ID = "projectId";
  public static final String PASSWORD = "password";
  public static final String USER_ID = "userId";
  public static final String AUTH_URL = "authUrl";
  private static final Logger LOGGER = Logger.getLogger(KeystoneV3IdentityRequestHelper.class);
  private static final String CONTENT_TYPE = "Content-Type";
  private static final String ACCEPT = "Accept";
  private static final String JSON_CONTENT_TYPE = "application/json";
  private static final String X_SUBJECT_TOKEN = "X-Subject-Token";
  private static final String IDENTITY_PROP = "identity";
  private static final String PROJECT_PROP = "project";
  private static final String METHODS_PROP = "methods";
  private static final String SCOPE_PROP = "scope";
  private static final String AUTH_PROP = "auth";
  private static final String USER_PROP = "user";
  private static final String ID_PROP = "id";
  private static final String V3_TOKENS_ENDPOINT_PATH = "/v3/auth/tokens";

  public static IdentityToken createIdentityToken(
      String url, String projectId, String userId, String userPassword)
      throws JSONException, IOException {
    LOGGER.debug("Creating new access id");
    checkValues(new String[] {url, projectId, userId, userPassword});
    JSONObject json;
    json = mountJson(projectId, userId, userPassword);

    String keyStoneUrl = url + V3_TOKENS_ENDPOINT_PATH;
    StringEntity body = new StringEntity(json.toString(), Charsets.UTF_8);
    HttpPost request = new HttpPost(keyStoneUrl);
    request.addHeader(CONTENT_TYPE, JSON_CONTENT_TYPE);
    request.addHeader(ACCEPT, JSON_CONTENT_TYPE);
    request.setEntity(body);
    HttpResponse response;
    try {
      response = HttpClients.createMinimal().execute(request);
    } catch (IOException e) {
      throw new IOException("Error while execute access id request", e);
    }
    StatusLine status = response.getStatusLine();
    if (status.getStatusCode() != HttpStatus.CREATED_201) {
      throw new IOException(
          "Access id request failed; "
              + "Status ["
              + status.getStatusCode()
              + " - "
              + status.getReasonPhrase()
              + "]");
    }
    IdentityToken token = getTokenFromResponse(response);
    return token;
  }

  private static JSONObject mountJson(String projectId, String userId, String password)
      throws JSONException {
    JSONObject userJson = new JSONObject();
    userJson.put(PASSWORD, password);
    userJson.put(ID_PROP, userId);

    JSONObject passwordJson = new JSONObject();
    passwordJson.put(USER_PROP, userJson);

    JSONObject identity = new JSONObject();
    identity.put(METHODS_PROP, new JSONArray(new String[] {PASSWORD}));
    identity.put(PASSWORD, passwordJson);

    JSONObject projectIdJson = new JSONObject();
    projectIdJson.put(ID_PROP, projectId);
    JSONObject projectJson = new JSONObject();
    projectJson.put(PROJECT_PROP, projectIdJson);

    JSONObject auth = new JSONObject();
    auth.put(IDENTITY_PROP, identity);
    auth.put(SCOPE_PROP, projectJson);

    JSONObject root = new JSONObject();
    root.put(AUTH_PROP, auth);
    return root;
  }

  private static void checkValues(String[] values) {
    for (String value : values) {
      if (Objects.isNull(value) || value.trim().isEmpty()) {
        throw new IllegalArgumentException("Some field is empty or null");
      }
    }
  }

  private static void checkKeyStoneCredentials(Map<String, String> credentials) {
    String[] credentialsSet = {AUTH_URL, PROJECT_ID, PASSWORD, USER_ID};
    for (String credential : credentialsSet) {
      String value = credentials.get(credential);
      if (Objects.isNull(value) || value.trim().isEmpty()) {
        throw new IllegalArgumentException(
            "Not found value to Keystone credential [" + credential + "]");
      }
    }
  }

  private static IdentityToken getTokenFromResponse(HttpResponse response)
      throws JSONException, IOException {
    String accessId = response.getFirstHeader(X_SUBJECT_TOKEN).getValue();
    JSONObject jsonResponse = new JSONObject(EntityUtils.toString(response.getEntity()));
    JSONObject jsonToken = jsonResponse.getJSONObject("token");
    String expiresAt = jsonToken.getString("expires_at");
    String issuedAt = jsonToken.getString("issued_at");
    IdentityToken token = new IdentityToken(accessId, issuedAt, expiresAt);
    return token;
  }
}
