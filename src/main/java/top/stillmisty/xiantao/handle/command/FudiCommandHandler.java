package top.stillmisty.xiantao.handle.command;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.command.CommandEntry;
import top.stillmisty.xiantao.domain.command.CommandGroup;
import top.stillmisty.xiantao.domain.fudi.vo.FarmCellVO;
import top.stillmisty.xiantao.domain.fudi.vo.FudiStatusVO;
import top.stillmisty.xiantao.domain.fudi.vo.PenCellVO;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.handle.TextFormat;
import top.stillmisty.xiantao.service.BeastService;
import top.stillmisty.xiantao.service.FarmService;
import top.stillmisty.xiantao.service.FudiService;
import top.stillmisty.xiantao.service.ServiceResult;
import top.stillmisty.xiantao.service.ai.SpiritChatService;

/** 福地命令处理器（纯 View 层） */
@Component
@Slf4j
@RequiredArgsConstructor
public class FudiCommandHandler implements CommandGroup {

  private final FudiService fudiService;
  private final BeastService beastService;
  private final FarmService farmService;
  private final SpiritChatService spiritChatService;

  // ===================== 纯文本委托方法 =====================

  public String handleFudiStatus(PlatformType platform, String openId) {
    return handleFudiStatus(platform, openId, TextFormat.PLAIN);
  }

  public String handleFudiGrid(PlatformType platform, String openId) {
    return handleFudiGrid(platform, openId, TextFormat.PLAIN);
  }

  public String handleFudiSpirit(PlatformType platform, String openId) {
    return handleFudiSpirit(platform, openId, TextFormat.PLAIN);
  }

  public String handlePlant(
      PlatformType platform, String openId, String position, String cropName) {
    return handlePlant(platform, openId, position, cropName, TextFormat.PLAIN);
  }

  public String handleCollect(PlatformType platform, String openId, String position) {
    return handleCollect(platform, openId, position, TextFormat.PLAIN);
  }

  public String handleBuild(
      PlatformType platform, String openId, String position, String cellTypeName) {
    return handleBuild(platform, openId, position, cellTypeName, TextFormat.PLAIN);
  }

  public String handleRemove(PlatformType platform, String openId, String position) {
    return handleRemove(platform, openId, position, TextFormat.PLAIN);
  }

  public String handleUpgradeCell(PlatformType platform, String openId, String position) {
    return handleUpgradeCell(platform, openId, position, TextFormat.PLAIN);
  }

  public String handleHatch(PlatformType platform, String openId, String position, String eggName) {
    return handleHatch(platform, openId, position, eggName, TextFormat.PLAIN);
  }

  public String handleRelease(PlatformType platform, String openId, String position) {
    return handleRelease(platform, openId, position, TextFormat.PLAIN);
  }

  public String handleEvolve(PlatformType platform, String openId, String position, String mode) {
    return handleEvolve(platform, openId, position, mode, TextFormat.PLAIN);
  }

  public String handleSpiritChat(PlatformType platform, String openId, String userInput) {
    return handleSpiritChat(platform, openId, userInput, TextFormat.PLAIN);
  }

  public String handleGiveGift(PlatformType platform, String openId, String itemName) {
    return handleGiveGift(platform, openId, itemName, TextFormat.PLAIN);
  }

  // ===================== Markdown 委托方法 =====================

  public String handleFudiStatusMarkdown(PlatformType platform, String openId) {
    return handleFudiStatus(platform, openId, TextFormat.MARKDOWN);
  }

  public String handleFudiGridMarkdown(PlatformType platform, String openId) {
    return handleFudiGrid(platform, openId, TextFormat.MARKDOWN);
  }

  public String handleSpiritChatMarkdown(PlatformType platform, String openId, String content) {
    return handleSpiritChat(platform, openId, content, TextFormat.MARKDOWN);
  }

  public String handlePlantMarkdown(
      PlatformType platform, String openId, String position, String cropName) {
    return handlePlant(platform, openId, position, cropName, TextFormat.MARKDOWN);
  }

  public String handleCollectMarkdown(PlatformType platform, String openId, String position) {
    return handleCollect(platform, openId, position, TextFormat.MARKDOWN);
  }

  public String handleBuildMarkdown(
      PlatformType platform, String openId, String position, String cellType) {
    return handleBuild(platform, openId, position, cellType, TextFormat.MARKDOWN);
  }

  public String handleRemoveMarkdown(PlatformType platform, String openId, String position) {
    return handleRemove(platform, openId, position, TextFormat.MARKDOWN);
  }

  public String handleUpgradeCellMarkdown(PlatformType platform, String openId, String position) {
    return handleUpgradeCell(platform, openId, position, TextFormat.MARKDOWN);
  }

  public String handleHatchMarkdown(
      PlatformType platform, String openId, String position, String eggName) {
    return handleHatch(platform, openId, position, eggName, TextFormat.MARKDOWN);
  }

  public String handleReleaseMarkdown(PlatformType platform, String openId, String position) {
    return handleRelease(platform, openId, position, TextFormat.MARKDOWN);
  }

  public String handleEvolveMarkdown(
      PlatformType platform, String openId, String position, String mode) {
    return handleEvolve(platform, openId, position, mode, TextFormat.MARKDOWN);
  }

  public String handleGiveGiftMarkdown(PlatformType platform, String openId, String itemName) {
    return handleGiveGift(platform, openId, itemName, TextFormat.MARKDOWN);
  }

  // ===================== 统一处理方法（含 TextFormat 参数） =====================

  public String handleFudiStatus(PlatformType platform, String openId, TextFormat fmt) {
    return switch (fudiService.getFudiStatus(platform, openId)) {
      case ServiceResult.Failure(var code, var msg) -> "❌ " + msg;
      case ServiceResult.Success(var vo) -> formatFudiStatus(vo, fmt);
    };
  }

  public String handleFudiGrid(PlatformType platform, String openId, TextFormat fmt) {
    return switch (fudiService.getFudiStatus(platform, openId)) {
      case ServiceResult.Failure(var code, var msg) -> "❌ " + msg;
      case ServiceResult.Success(var vo) -> formatCellLayout(vo, fmt);
    };
  }

  public String handleFudiSpirit(PlatformType platform, String openId, TextFormat fmt) {
    return switch (fudiService.getFudiStatus(platform, openId)) {
      case ServiceResult.Failure(var code, var msg) -> "❌ " + msg;
      case ServiceResult.Success(var vo) -> formatSpiritInfo(vo, fmt);
    };
  }

  public String handlePlant(
      PlatformType platform, String openId, String position, String cropName, TextFormat fmt) {
    return switch (farmService.plantCropByInput(platform, openId, position, cropName)) {
      case ServiceResult.Failure(var code, var msg) -> "❌ " + msg;
      case ServiceResult.Success(var vo) -> formatPlantResult(vo, fmt);
    };
  }

  public String handleCollect(
      PlatformType platform, String openId, String position, TextFormat fmt) {
    if ("all".equalsIgnoreCase(position)) {
      return switch (fudiService.collectAll(platform, openId)) {
        case ServiceResult.Failure(var code, var msg) -> "❌ " + msg;
        case ServiceResult.Success(var result) -> {
          if (result.totalItems() == 0) {
            yield "没有可收取的内容。";
          }
          var parts = new java.util.ArrayList<String>();
          if (result.harvested() > 0) parts.add("收获 %d 块灵田".formatted(result.harvested()));
          if (result.collected() > 0) parts.add("收取 %d 个兽栏".formatted(result.collected()));
          yield "✅ 已" + String.join("、", parts) + "，共获得 " + result.totalItems() + " 份物资。";
        }
      };
    }
    return switch (fudiService.collect(platform, openId, position)) {
      case ServiceResult.Failure(var code, var msg) -> "❌ " + msg;
      case ServiceResult.Success(var result) -> {
        if ("FARM".equals(result.type())) {
          yield "✅ 已收获地块 %s 的%s，获得 %d 份。".formatted(position, result.cropName(), result.yield());
        } else {
          yield "✅ 从地块 %s 收取了 %d 件%s产出。"
              .formatted(position, result.totalItems(), result.beastName());
        }
      }
    };
  }

  public String handleBuild(
      PlatformType platform, String openId, String position, String cellTypeName, TextFormat fmt) {
    return switch (fudiService.buildCell(platform, openId, position, cellTypeName)) {
      case ServiceResult.Failure(var code, var msg) -> "❌ " + msg;
      case ServiceResult.Success(_) -> "✅ 已在地块 %s 建造%s。".formatted(position, cellTypeName);
    };
  }

  public String handleRemove(
      PlatformType platform, String openId, String position, TextFormat fmt) {
    return switch (fudiService.removeCell(platform, openId, position)) {
      case ServiceResult.Failure(var code, var msg) -> "❌ " + msg;
      case ServiceResult.Success(var result) ->
          "✅ 已拆除地块 %s 的%s。".formatted(position, result.type());
    };
  }

  public String handleUpgradeCell(
      PlatformType platform, String openId, String position, TextFormat fmt) {
    return switch (fudiService.upgradeCell(platform, openId, position)) {
      case ServiceResult.Failure(var code, var msg) -> "❌ " + msg;
      case ServiceResult.Success(var result) ->
          "✅ 已将地块 %s 的%s从 Lv%d 升级至 Lv%d"
              .formatted(position, result.type(), result.oldLevel(), result.newLevel());
    };
  }

  public String handleHatch(
      PlatformType platform, String openId, String position, String eggName, TextFormat fmt) {
    return switch (beastService.hatchBeastByInput(platform, openId, position, eggName)) {
      case ServiceResult.Failure(var code, var msg) -> "❌ " + msg;
      case ServiceResult.Success(var vo) -> formatHatchResult(vo, fmt);
    };
  }

  public String handleRelease(
      PlatformType platform, String openId, String position, TextFormat fmt) {
    return switch (beastService.releaseBeast(platform, openId, position)) {
      case ServiceResult.Failure(var code, var msg) -> "❌ " + msg;
      case ServiceResult.Success(var result) -> "✅ 已放生%s。".formatted(result.beastName());
    };
  }

  public String handleEvolve(
      PlatformType platform, String openId, String position, String mode, TextFormat fmt) {
    return switch (beastService.evolveBeast(platform, openId, position, mode)) {
      case ServiceResult.Failure(var code, var msg) -> "❌ " + msg;
      case ServiceResult.Success(var vo) -> formatEvolveResult(vo, mode, fmt);
    };
  }

  public String handleSpiritChat(
      PlatformType platform, String openId, String userInput, TextFormat fmt) {
    log.info("处理地灵自然语言交互 - platform: {}, input: {}", platform, userInput);
    return switch (spiritChatService.chatWithSpirit(platform, openId, userInput)) {
      case ServiceResult.Failure(var code, var msg) -> "❌ " + msg;
      case ServiceResult.Success(var response) -> response;
    };
  }

  public String handleGiveGift(
      PlatformType platform, String openId, String itemName, TextFormat fmt) {
    return switch (fudiService.giveGift(platform, openId, itemName)) {
      case ServiceResult.Failure(var code, var msg) -> "❌ " + msg;
      case ServiceResult.Success(var result) -> {
        String prefix = result.change() > 0 ? "✅" : "😅";
        yield "%s 地灵收到了%s（%s），好感度 %+d"
            .formatted(prefix, result.itemName(), result.reaction(), result.change());
      }
    };
  }

  // ===================== 格式化方法 =====================

  private String formatFudiStatus(FudiStatusVO status, TextFormat fmt) {
    StringBuilder sb = new StringBuilder();

    if (status.getTribulationResult() != null) {
      sb.append(status.getTribulationResult()).append("\n\n");
    }

    sb.append(fmt.heading("福地状态", "🏔️"));
    sb.append(fmt.separator());
    sb.append("⛈️ 劫数：")
        .append(status.getTribulationStage())
        .append("  连胜×")
        .append(status.getTribulationWinStreak())
        .append("\n");
    sb.append("🧚 地灵形态：")
        .append(status.getSpiritFormName() != null ? status.getSpiritFormName() : "未知形态")
        .append("\n");
    if (status.getMbtiType() != null) {
      sb.append("🎭 地灵人格：").append(status.getMbtiType().getCode()).append("\n");
    }
    if (status.getEmotionState() != null) {
      sb.append("😊 地灵情绪：")
          .append(status.getEmotionState().getEmoji())
          .append(" ")
          .append(status.getEmotionState().getDescription())
          .append("\n");
    }
    sb.append("🏗️ 已占地块：")
        .append(status.getOccupiedCells())
        .append("/")
        .append(status.getTotalCells())
        .append("\n");
    sb.append(fmt.separator());
    sb.append("💡 输入「福地地块」查看详细布局");
    return sb.toString();
  }

  private String formatCellLayout(FudiStatusVO status, TextFormat fmt) {
    StringBuilder sb = new StringBuilder();
    sb.append(fmt.heading("福地地块布局", "🗺️"));
    sb.append(fmt.separator());
    if (status.getCellDetails() == null || status.getCellDetails().isEmpty()) {
      sb.append("（空地，尚未建造任何地块）\n");
    } else {
      for (var cell : status.getCellDetails()) {
        sb.append("📍 #").append(cell.getCellId()).append(" ");
        sb.append(cell.getType().getChineseName());
        if (cell.getCellLevel() != null && cell.getCellLevel() > 1) {
          sb.append(" Lv").append(cell.getCellLevel());
        }
        if (cell.getName() != null) sb.append(" - ").append(cell.getName());
        if (cell.getGrowthProgress() != null) {
          int percent = (int) (cell.getGrowthProgress() * 100);
          sb.append(" [").append(percent).append("%]");
          if (Boolean.TRUE.equals(cell.getIsMature())) sb.append(" ✅ 可收取");
        }
        if (cell.getQuality() != null) sb.append(" ").append(cell.getQuality());
        if (cell.getProductionStored() != null && cell.getProductionStored() > 0)
          sb.append(" 📦x").append(cell.getProductionStored());
        if (Boolean.TRUE.equals(cell.getIsIncubating())) sb.append(" 🥚孵化中");
        sb.append("\n");
      }
    }
    sb.append(fmt.separator());
    sb.append("💡 输入「福地种植」「福地建造」「福地收取」等指令进行操作");
    return sb.toString();
  }

  private String formatSpiritInfo(FudiStatusVO status, TextFormat fmt) {
    StringBuilder sb = new StringBuilder();
    sb.append(fmt.heading("地灵信息", "🧚"));
    sb.append(fmt.separator());
    sb.append(
        fmt.listItem(
            "形态：" + (status.getSpiritFormName() != null ? status.getSpiritFormName() : "未知形态")));
    sb.append(
        fmt.listItem(
            "MBTI人格：" + (status.getMbtiType() != null ? status.getMbtiType().getCode() : "未知")));
    sb.append(
        fmt.listItem(
            "语气风格：" + (status.getMbtiType() != null ? status.getMbtiType().getToneStyle() : "")));
    sb.append(fmt.listItem("劫数：" + status.getTribulationStage()));
    sb.append(
        fmt.listItem("好感度：" + status.getSpiritAffection() + "/" + status.getAffectionMax() + " 点"));
    if (status.getEmotionState() != null) {
      sb.append(
          fmt.listItem(
              "当前情绪："
                  + status.getEmotionState().getEmoji()
                  + " "
                  + status.getEmotionState().getDescription()));
    }
    if (status.getLikedTags() != null && !status.getLikedTags().isEmpty()) {
      sb.append(fmt.listItem("喜爱：" + String.join("、", status.getLikedTags())));
    }
    if (status.getDislikedTags() != null && !status.getDislikedTags().isEmpty()) {
      sb.append(fmt.listItem("厌恶：" + String.join("、", status.getDislikedTags())));
    }
    sb.append(fmt.separator());
    sb.append("💡 与地灵互动可提升好感度 ｜ 输入「地灵送礼 [物品名]」赠送礼物");
    return sb.toString();
  }

  private String formatPlantResult(FarmCellVO result, TextFormat fmt) {
    return "✅ 已在地块 %s 种植%s，预计 %.1f 小时后成熟。"
        .formatted(result.getCellId(), result.getCropName(), result.getActualGrowthHours());
  }

  private String formatHatchResult(PenCellVO result, TextFormat fmt) {
    StringBuilder sb = new StringBuilder();
    sb.append("✅ 已开始在地块 %s 孵化%s！\n".formatted(result.getCellId(), result.getBeastName()));
    sb.append(fmt.listItem("品质：" + result.getQuality() + " | 等阶：T" + result.getTier()));
    if (result.isMutant()) {
      sb.append("✨ 变异灵兽！特性：").append(String.join(",", result.getMutationTraits())).append("\n");
    }
    long hours =
        java.time.Duration.between(java.time.LocalDateTime.now(), result.getMatureTime()).toHours();
    sb.append(fmt.listItem("预计 " + hours + " 小时后孵化完成。"));
    return sb.toString();
  }

  private String formatEvolveResult(PenCellVO result, String mode, TextFormat fmt) {
    StringBuilder sb = new StringBuilder();
    sb.append("✅ %s成功！\n".formatted(mode));
    sb.append(
        fmt.listItem(
            "灵兽："
                + result.getBeastName()
                + " | 等阶：T"
                + result.getTier()
                + " | 品质："
                + result.getQuality()));
    if (result.isMutant()) {
      sb.append(fmt.listItem("能力：" + String.join(",", result.getMutationTraits())));
    }
    return sb.toString();
  }

  @Override
  public String groupName() {
    return "福地";
  }

  @Override
  public String groupDescription() {
    return "福地经营、地灵互动、灵兽培育";
  }

  @Override
  public List<CommandEntry> commands() {
    return List.of(
        new CommandEntry("福地", "查看福地概况", "福地"),
        new CommandEntry("福地地块", "查看福地详细布局", "福地地块"),
        new CommandEntry("福地种植 {{位置}} {{作物}}", "种植作物", "福地种植 1 灵芝"),
        new CommandEntry("福地收取 {{位置/all}}", "收获作物或收取产物", "福地收取 1"),
        new CommandEntry("福地建造 {{位置}} {{类型}}", "建造新地块（灵田/兽栏）", "福地建造 2 灵田"),
        new CommandEntry("福地拆除 {{位置}}", "拆除地块", "福地拆除 1"),
        new CommandEntry("福地升级 {{位置}}", "升级地块等级", "福地升级 1"),
        new CommandEntry("福地孵化 {{位置}} {{兽卵}}", "孵化灵兽蛋", "福地孵化 1 火凤卵"),
        new CommandEntry("福地放生 {{位置}}", "放生灵兽", "福地放生 1"),
        new CommandEntry("福地进化 {{位置}} {{升阶/升品}}", "进化灵兽", "福地进化 1 升阶"),
        new CommandEntry("地灵 {{内容}}", "与地灵自然语言聊天", "地灵 你好呀"),
        new CommandEntry("地灵送礼 {{物品}}", "赠送礼物给地灵", "地灵送礼 灵果"));
  }
}
