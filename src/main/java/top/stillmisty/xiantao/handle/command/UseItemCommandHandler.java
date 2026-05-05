package top.stillmisty.xiantao.handle.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.service.ItemUseService;
import top.stillmisty.xiantao.service.ServiceResult;

/** 统一使用物品命令处理器 支持：丹药、法决玉简、丹方卷轴、进化石 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UseItemCommandHandler {

  private final ItemUseService itemUseService;

  /**
   * 处理使用物品命令
   *
   * @param itemName 物品名称或编号
   * @param args 额外参数（如进化石需要位置）
   */
  public String handleUseItem(PlatformType platform, String openId, String itemName, String args) {
    log.debug(
        "处理使用物品 - Platform: {}, OpenId: {}, ItemName: {}, Args: {}",
        platform,
        openId,
        itemName,
        args);
    return switch (itemUseService.useItem(platform, openId, itemName, args)) {
      case ServiceResult.Failure(var code, var msg) -> msg;
      case ServiceResult.Success(var result) -> result;
    };
  }
}
