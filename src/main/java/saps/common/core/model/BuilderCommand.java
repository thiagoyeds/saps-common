package saps.common.core.model;

import java.util.List;

public interface BuilderCommand {
    List<String> buildCommands(SapsImage task, String phase);
}
