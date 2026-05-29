package top.stillmisty.xiantao.handle.command;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.beast.entity.Beast;
import top.stillmisty.xiantao.domain.beast.vo.BeastStatusVO;
import top.stillmisty.xiantao.domain.command.CommandEntry;
import top.stillmisty.xiantao.domain.command.CommandGroup;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.handle.CommandHandlerHelper;
import top.stillmisty.xiantao.handle.TextFormat;
import top.stillmisty.xiantao.service.beast.BeastCombatService;

@Slf4j
@Component
@RequiredArgsConstructor
public class BeastCommandHandler implements CommandGroup {

  private final BeastCombatService beastCombatService;

  public String handleGetDeployedBeasts(PlatformType platform, String openId, TextFormat fmt) {
    return CommandHandlerHelper.safeCall(
        () -> beastCombatService.getDeployedBeasts(platform, openId),
        fmt,
        beasts -> formatDeployedBeasts(beasts, fmt));
  }

  public String handleBeastList(PlatformType platform, String openId, TextFormat fmt) {
    return CommandHandlerHelper.safeCall(
        () -> beastCombatService.getBeastList(platform, openId),
        fmt,
        beasts -> formatBeastList(beasts, fmt));
  }

  private String formatDeployedBeasts(List<BeastStatusVO> beasts, TextFormat fmt) {
    if (beasts.isEmpty()) {
      return fmt.heading("出战灵兽（空）", "🐾") + "没有出战的灵兽。";
    }
    StringBuilder sb = new StringBuilder(fmt.heading("出战灵兽", "🐾"));
    for (int i = 0; i < beasts.size(); i++) {
      BeastStatusVO beast = beasts.get(i);
      sb.append(
          String.format(
              "%d. %s %s（%s %s）\n",
              i + 1,
              beast.beastName(),
              beast.gender(),
              Beast.getTierName(beast.tier()),
              beast.quality()));
      sb.append(fmt.listItem("等级：" + beast.level()));
      sb.append(fmt.listItem("HP：" + beast.hpCurrent() + "/" + beast.maxHp()));
      sb.append(fmt.listItem("攻击：" + beast.attack() + " 防御：" + beast.defense()));
      if (beast.skills() != null && !beast.skills().isEmpty()) {
        sb.append(fmt.listItem("技能：" + beast.skills().size() + "个"));
      }
      if (i < beasts.size() - 1) sb.append(fmt.separator());
    }
    return sb.toString();
  }

  private String formatBeastList(List<BeastStatusVO> beasts, TextFormat fmt) {
    if (beasts.isEmpty()) {
      return fmt.heading("灵兽列表（空）", "📋") + "你还没有任何灵兽。";
    }
    StringBuilder sb = new StringBuilder(fmt.heading("灵兽列表", "📋"));
    sb.append(fmt.listItem("共 " + beasts.size() + " 只")).append(fmt.separator());
    for (int i = 0; i < beasts.size(); i++) {
      BeastStatusVO beast = beasts.get(i);
      StringBuilder status = new StringBuilder();
      if (beast.isDeployed()) status.append("⚔️出战");
      else if (beast.needsRecovery()) status.append("🛌休养");
      else if (beast.pennedCellId() > 0) status.append("🏠在栏");
      else status.append("💤待命");
      if (beast.breedCooldown()) status.append(" 🔥繁育冷却");
      sb.append(
          "%d. %s %s %s %s Lv%d %s\n"
              .formatted(
                  i + 1,
                  beast.beastName(),
                  beast.gender(),
                  Beast.getTierName(beast.tier()),
                  beast.quality(),
                  beast.level(),
                  status));
    }
    return sb.toString();
  }

  @Override
  public String groupName() {
    return "灵兽";
  }

  @Override
  public String groupSummary() {
    return "查看与管理灵兽";
  }

  @Override
  public String groupDescription() {
    return "灵兽查看";
  }

  @Override
  public List<CommandEntry> commands() {
    return List.of(
        new CommandEntry("灵兽", "查看当前出战的灵兽", "灵兽"), new CommandEntry("灵兽列表", "查看拥有的所有灵兽", "灵兽列表"));
  }
}
