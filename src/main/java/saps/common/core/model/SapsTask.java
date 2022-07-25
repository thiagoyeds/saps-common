/* (C)2020 */
package saps.common.core.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SapsTask implements Serializable, BuilderCommand {

  private static final Logger LOGGER = Logger.getLogger(SapsTask.class);

  private static final String JSON_HEADER_ID = "id";
  private static final String JSON_HEADER_REQUIREMENTS = "requirements";
  private static final String JSON_HEADER_ENV_VARS = "envVars";
  private static final String JSON_HEADER_COMMANDS = "commands";
  private static final String JSON_HEADER_METADATA = "metadata";

  private String id;
  private Map<String, String> requirements;
  private List<String> commands;
  private Map<String, String> metadata;
  private Map<String, String> envVars;

  public SapsTask(
      String id,
      Map<String, String> requirements,
      List<String> commands,
      Map<String, String> metadata,
      Map<String, String> envVars) {
    this.id = id;
    this.requirements = requirements;
    this.commands = commands;
    this.metadata = metadata;
    this.envVars = envVars;
  }

  public SapsTask(String id, Map<String, String> requirements) {
    this(id, requirements, new LinkedList<String>(), new HashMap<String, String>(), new HashMap<String, String>());
  }

  public SapsTask(String id) {
    this(id, new HashMap<String, String>());
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Map<String, String> getRequirements() {
    return requirements;
  }

  public Map<String, String> getEnvVars() {
    return envVars;
  }

  public void setRequirements(Map<String, String> requirements) {
    this.requirements = requirements;
  }

  public void setEnvVars(Map<String, String> envVars) {
    this.envVars = envVars;
  }

  public void addRequirement(String key, String value) {
    this.requirements.put(key, value);
  }

  public void addEnvVar(String key, String value) {
    this.envVars.put(key, value);
  }

  public List<String> getCommands() {
    return commands;
  }

  public void setCommands(List<String> commands) {
    this.commands = commands;
  }

  public void addCommand(String command) {
    this.commands.add(command);
  }

  public Map<String, String> getMetadata() {
    return metadata;
  }

  public void setMetadata(Map<String, String> metadata) {
    this.metadata = metadata;
  }

  public void addMetadata(String key, String value) {
    this.metadata.put(key, value);
  }

  public JSONObject toJSON() {
    try {
      JSONObject sapsTask = new JSONObject();
      sapsTask.put(JSON_HEADER_ID, this.getId());

      JSONObject requirements = new JSONObject();
      for (Map.Entry<String, String> entry : this.getRequirements().entrySet())
        requirements.put(entry.getKey(), entry.getValue());
      sapsTask.put(JSON_HEADER_REQUIREMENTS, requirements);

      JSONObject envVars = new JSONObject();
      for (Map.Entry<String, String> entry : this.getEnvVars().entrySet())
        requirements.put(entry.getKey(), entry.getValue());
      sapsTask.put(JSON_HEADER_ENV_VARS, envVars);

      JSONArray commands = new JSONArray();
      for (String command : this.getCommands()) commands.put(command);
      sapsTask.put(JSON_HEADER_COMMANDS, commands);

      JSONObject metadata = new JSONObject();
      for (Map.Entry<String, String> entry : this.getMetadata().entrySet())
        metadata.put(entry.getKey(), entry.getValue());
      sapsTask.put(JSON_HEADER_METADATA, metadata);

      return sapsTask;
    } catch (JSONException e) {
      LOGGER.debug("Error while trying to create a JSON from task", e);
      return null;
    }
  }

  public static SapsTask fromJSON(JSONObject taskJSON) {
    SapsTask sapsTask = new SapsTask(taskJSON.optString(JSON_HEADER_ID));

    JSONObject requirements = taskJSON.optJSONObject(JSON_HEADER_REQUIREMENTS);
    Iterator<?> requirementsKeys = requirements.keys();
    while (requirementsKeys.hasNext()) {
      String key = (String) requirementsKeys.next();
      sapsTask.addRequirement(key, requirements.optString(key));
    }

    JSONObject envVars = taskJSON.optJSONObject(JSON_HEADER_ENV_VARS);
    Iterator<?> envVarsKeys = envVars.keys();
    while (envVarsKeys.hasNext()) {
      String key = (String) envVarsKeys.next();
      sapsTask.addEnvVar(key, envVars.optString(key));
    }

    JSONArray commands = taskJSON.optJSONArray("commands");
    for (int i = 0; i < commands.length(); i++)
      try {
        sapsTask.addCommand((String) commands.toString(i));
      } catch (JSONException e) {
        e.printStackTrace();
      }

    JSONObject metadata = taskJSON.optJSONObject("metadata");
    Iterator<?> metadataKeys = metadata.keys();
    while (metadataKeys.hasNext()) {
      String key = (String) metadataKeys.next();
      sapsTask.addMetadata(key, metadata.optString(key));
    }
    return sapsTask;
  }

  @Override
  public List<String> buildCommands(SapsImage task, String phase) {
    return null;
  }
}
