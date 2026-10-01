package top.stillmisty.xiantao.service;

import java.util.ArrayList;
import java.util.Comparator;
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
        CommandEntry previous = triggerIndex.putIfAbsent(entry.trigger(), entry);
        if (previous != null) {
          throw new IllegalStateException(
              String.format(
                  "命令触发词冲突: 「%s」同时由 %s 与 %s 声明",
                  entry.trigger(), previous.trigger(), entry.trigger()));
        }
      }
    }
  }

  public List<CommandGroup> getAllGroups() {
    return groups;
  }

  public Optional<CommandGroup> findByGroupName(String name) {
    return groups.stream().filter(g -> g.groupName().equals(name)).findFirst();
  }

  public Optional<CommandEntry> findByTrigger(String trigger) {
    return Optional.ofNullable(triggerIndex.get(trigger));
  }

  public List<CommandEntry> search(String keyword) {
    return triggerIndex.values().stream()
        .filter(e -> e.trigger().contains(keyword) || e.description().contains(keyword))
        .toList();
  }

  /** 近似建议：按编辑距离给出可能想找的子系统/命令（用于错别字容错）。 */
  public List<String> suggest(String keyword) {
    String query = keyword.strip();
    if (query.isEmpty()) {
      return List.of();
    }
    int maxDistance = query.length() <= 3 ? 1 : 2;
    record Scored(String name, int distance) {}
    List<Scored> scored = new ArrayList<>();
    for (CommandGroup group : groups) {
      scored.add(new Scored(group.groupName(), distance(query, group.groupName())));
    }
    for (CommandEntry entry : triggerIndex.values()) {
      String bare = bareTrigger(entry.trigger());
      scored.add(new Scored(bare, distance(query, bare)));
    }
    return scored.stream()
        .filter(s -> s.distance() > 0 && s.distance() <= maxDistance)
        .sorted(Comparator.comparingInt(Scored::distance).thenComparing(Scored::name))
        .map(Scored::name)
        .distinct()
        .limit(3)
        .toList();
  }

  /** 触发词的字面部分（去掉参数占位描述）。 */
  private static String bareTrigger(String trigger) {
    for (int i = 0; i < trigger.length(); i++) {
      char c = trigger.charAt(i);
      if (c == ' ' || c == '「' || c == '（') {
        return trigger.substring(0, i).strip();
      }
    }
    return trigger.strip();
  }

  /** Levenshtein 编辑距离。 */
  private static int distance(String a, String b) {
    int[][] dp = new int[a.length() + 1][b.length() + 1];
    for (int i = 0; i <= a.length(); i++) dp[i][0] = i;
    for (int j = 0; j <= b.length(); j++) dp[0][j] = j;
    for (int i = 1; i <= a.length(); i++) {
      for (int j = 1; j <= b.length(); j++) {
        int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
        dp[i][j] = Math.min(Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1), dp[i - 1][j - 1] + cost);
      }
    }
    return dp[a.length()][b.length()];
  }
}
