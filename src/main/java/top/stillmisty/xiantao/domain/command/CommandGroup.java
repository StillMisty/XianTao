package top.stillmisty.xiantao.domain.command;

import java.util.List;

public interface CommandGroup {
  String groupName();

  String groupDescription();

  List<CommandEntry> commands();
}
