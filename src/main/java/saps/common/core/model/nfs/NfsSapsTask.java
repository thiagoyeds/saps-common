/* (C)2020 */
package saps.common.core.model.nfs;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import saps.common.core.model.SapsImage;
import saps.common.core.model.SapsTask;

public class NfsSapsTask extends SapsTask {

  private static final String DATE_FORMAT = "yyyy-MM-dd";

  public NfsSapsTask(String id, Map<String, String> requirements,
      Map<String, String> metadata, Map<String, String> envVars) {
    super(id, requirements, new LinkedList<String>(), metadata, envVars);
  }

  @Override
  public List<String> buildCommands(SapsImage task, String phase) {
    // info shared dir between host (with NFS) and container
    // ...

    DateFormat dateFormater = new SimpleDateFormat(DATE_FORMAT);
    String taskDir = task.getTaskId();
    String rootPath = "/nfs/" + taskDir;
    String phaseDirPath = "/nfs/" + taskDir + File.separator + phase;
    List<String> commands = new LinkedList<String>();

    // Remove dirs
    String removeThings = String.format("rm -rf %s", phaseDirPath);
    commands.add(removeThings);

    // Create dirs
    String createDirectory = String.format("mkdir -p %s", phaseDirPath);
    commands.add(createDirectory);

    // Run command
    String runCommand =
        String.format(
            "bash /home/saps/run.sh %s %s %s %s",
            rootPath,
            task.getDataset(),
            task.getRegion(),
            dateFormater.format(task.getImageDate()));
    commands.add(runCommand);

    return commands;
  }
}
