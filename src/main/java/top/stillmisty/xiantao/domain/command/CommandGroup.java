package top.stillmisty.xiantao.domain.command;

import java.util.List;

public interface CommandGroup {
  String groupName();

  String groupSummary();

  String groupDescription();

  List<CommandEntry> commands();
}
