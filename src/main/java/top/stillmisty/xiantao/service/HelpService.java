package top.stillmisty.xiantao.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.stillmisty.xiantao.domain.command.CommandEntry;
import top.stillmisty.xiantao.domain.command.CommandGroup;

@Service
public class HelpService {

  private final List<CommandGroup> groups;
  private final Map<String, CommandEntry> triggerIndex;

  public HelpService(@Autowired List<CommandGroup> groups) {
    this.groups = groups;
    this.triggerIndex = new LinkedHashMap<>();
    for (CommandGroup group : groups) {
      for (CommandEntry entry : group.commands()) {
        triggerIndex.put(entry.trigger(), entry);
      }
    }
  }

  public List<CommandGroup> getAllGroups() {
    return groups;
  }

  public Optional<CommandEntry> findByTrigger(String trigger) {
    return Optional.ofNullable(triggerIndex.get(trigger));
  }

  public List<CommandEntry> search(String keyword) {
    return triggerIndex.values().stream()
        .filter(e -> e.trigger().contains(keyword) || e.description().contains(keyword))
        .toList();
  }
}
