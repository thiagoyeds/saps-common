/* (C)2020 */
package saps.common.core.dto;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class TaskRequestDTO implements Serializable {
  /** */
  private static final long serialVersionUID = 1L;

  private String label;
  private Map<String, String> requirements;
  @SerializedName("env_vars")
  private Map<String, String> envVars;
  private List<String> commands;
  private Map<String, String> metadata;

  public TaskRequestDTO(
      String label,
      Map<String, String> requirements,
      List<String> commands,
      Map<String, String> metadata,
      Map<String, String> envVars) {
    this.label = label;
    this.requirements = requirements;
    this.commands = commands;
    this.metadata = metadata;
    this.envVars = envVars;
  }

  // empty constructor required for Gson.
  public TaskRequestDTO() {}

  public List<String> getCommands() {
    return this.commands;
  }

  public Map<String, String> getRequirements() {
    return requirements;
  }

  public Map<String, String> getEnvVars() {
    return envVars;
  }

  public Map<String, String> getMetadata() {
    return this.metadata;
  }

  @Override
  public String toString() {
    return "TaskRequestDTO [id="
        + label
        + ", requirements="
        + requirements
        + ", commands="
        + commands
        + ", metadata="
        + metadata
        + ", envVars="
        + envVars
        + "]";
  }
}
