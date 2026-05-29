package top.stillmisty.xiantao.handle.command;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.command.CommandEntry;
import top.stillmisty.xiantao.domain.command.CommandGroup;
import top.stillmisty.xiantao.handle.CommandHandlerHelper;
import top.stillmisty.xiantao.handle.TextFormat;
import top.stillmisty.xiantao.service.UserContext;
import top.stillmisty.xiantao.service.inventory.ItemUseService;

/** 统一使用物品命令处理器 支持：丹药、法决玉简、丹方卷轴 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UseItemCommandHandler implements CommandGroup {

  private final ItemUseService itemUseService;

  public String handleUseItem(String itemName, String args, TextFormat fmt) {
    Long userId = UserContext.requireCurrentUserId();
    log.debug("处理使用物品 - UserId: {}, ItemName: {}, Args: {}", userId, itemName, args);
    return CommandHandlerHelper.safeCall(
        () -> itemUseService.useItem(userId, itemName, args), fmt, result -> result);
  }

  @Override
  public String groupName() {
    return "物品";
  }

  @Override
  public String groupSummary() {
    return "使用丹药、玉简等道具";
  }

  @Override
  public String groupDescription() {
    return "使用物品";
  }

  @Override
  public List<CommandEntry> commands() {
    return List.of(
        new CommandEntry("使用 「物品」", "使用物品（丹药、玉简、卷轴等）", "使用 天元丹"),
        new CommandEntry("使用 「物品」 「参数」", "使用物品并指定参数", "使用 天元丹 1"));
  }
}
