/* (C)2020 */
package saps.common.core.model.swift;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import saps.common.core.model.SapsImage;
import saps.common.core.model.SapsTask;

public class SwiftSapsTask extends SapsTask {

  private static final String DATE_FORMAT = "yyyy-MM-dd";

  public SwiftSapsTask(String id, Map<String, String> requirements,
      Map<String, String> metadata, Map<String, String> envVars) {
    super(id, requirements, new LinkedList<String>(), metadata, envVars);
  }

  @Override
  public List<String> buildCommands(SapsImage task, String phase) {
    DateFormat dateFormater = new SimpleDateFormat(DATE_FORMAT);
    String taskDir = task.getTaskId();
    String phaseDirPath = taskDir + File.separator + phase;
    List<String> commands = new LinkedList<String>();

    // Create dirs
    String createDirectory = String.format("mkdir -p %s %s", taskDir, phaseDirPath);
    commands.add(createDirectory);

    // Get previous stage outputs
    String getPreviousStageOutputFilesCommand = String.format("python getter.py %s > %s/get-previous-results.log", taskDir, phaseDirPath);
    commands.add(getPreviousStageOutputFilesCommand);

    // Run command
    String runCommand =
        String.format(
            "bash /home/saps/run.sh %s %s %s %s",
            taskDir,
            task.getDataset(),
            task.getRegion(),
            dateFormater.format(task.getImageDate()));
    commands.add(runCommand);

    // Send results
    String sendOutputFileCommand = String.format("python sender.py %s > %s/send-result.log", phaseDirPath, phaseDirPath);
    commands.add(sendOutputFileCommand);

    return commands;
  }
}
