package top.stillmisty.xiantao.handle.command;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.command.CommandEntry;
import top.stillmisty.xiantao.domain.command.CommandGroup;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.handle.TextFormat;
import top.stillmisty.xiantao.service.ServiceResult;
import top.stillmisty.xiantao.service.ShopService;
import top.stillmisty.xiantao.service.ai.ShopChatService;

@Component
@RequiredArgsConstructor
public class ShopCommandHandler implements CommandGroup {

  private final ShopChatService shopChatService;
  private final ShopService shopService;

  @Override
  public String groupName() {
    return "商铺";
  }

  @Override
  public String groupDescription() {
    return "与商铺掌柜交易（买/卖/闲聊）";
  }

  @Override
  public List<CommandEntry> commands() {
    return List.of(
        new CommandEntry("掌柜 {{内容}}", "与当前所在地图商铺掌柜自然语言交互", "掌柜 有什么好货？"),
        new CommandEntry("回收 {{物品名}}", "快速回收物品（直接估价出售）", "回收 玄铁剑"));
  }

  public String handleShopkeeper(PlatformType platform, String openId, String userInput) {
    return handleShopkeeper(platform, openId, userInput, TextFormat.PLAIN);
  }

  public String handleShopkeeperMarkdown(PlatformType platform, String openId, String userInput) {
    return handleShopkeeper(platform, openId, userInput, TextFormat.MARKDOWN);
  }

  private String handleShopkeeper(
      PlatformType platform, String openId, String userInput, TextFormat fmt) {
    return switch (shopChatService.chatWithShopkeeper(platform, openId, userInput)) {
      case ServiceResult.Failure(var code, var msg) -> fmt.bold("掌柜") + " " + msg;
      case ServiceResult.Success(var msg) -> msg;
    };
  }

  public String handleQuickSell(PlatformType platform, String openId, String itemName) {
    return handleQuickSell(platform, openId, itemName, TextFormat.PLAIN);
  }

  public String handleQuickSellMarkdown(PlatformType platform, String openId, String itemName) {
    return handleQuickSell(platform, openId, itemName, TextFormat.MARKDOWN);
  }

  private String handleQuickSell(
      PlatformType platform, String openId, String itemName, TextFormat fmt) {
    // Quick sell is delegated to the LLM chat with a sell command
    String sellPrompt = "我想卖掉这个：" + itemName + "，按估价直接成交";
    return handleShopkeeper(platform, openId, sellPrompt, fmt);
  }
}
