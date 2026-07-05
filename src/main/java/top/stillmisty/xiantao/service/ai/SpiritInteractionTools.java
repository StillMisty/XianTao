package top.stillmisty.xiantao.service.ai;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.fudi.vo.GiveGiftVO;
import top.stillmisty.xiantao.domain.fudi.vo.TriggerTribulationVO;
import top.stillmisty.xiantao.service.UserContext;
import top.stillmisty.xiantao.service.fudi.FudiService;

/** 地灵互动工具 — 好感度、送礼、天劫 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SpiritInteractionTools {

  private final ToolExecutor toolExecutor;
  private final FudiService fudiService;

  @Tool(description = "表达对主人言行的不满，降低好感度。severity 1-5。仅当主人确有冒犯言行时调用")
  @Transactional
  public FeelOffendedResponse feelOffended(
      @ToolParam(description = "冒犯原因简述") String reason,
      @ToolParam(description = "冒犯程度 1-5") int severity) {
    return toolExecutor.execute(
        "feelOffended",
        () -> {
          Long userId = UserContext.requireCurrentUserId();
          int clamped = Math.clamp(severity, 1, 5);
          fudiService.adjustSpiritAffection(userId, -clamped);
          return new FeelOffendedResponse(reason, clamped);
        });
  }

  @Tool(description = "接受主人赠送的礼物。itemName 是背包中的物品名称。好感度可能上升也可能下降")
  @Transactional
  public AcceptGiftResponse acceptGift(@ToolParam(description = "赠送的物品名称") String itemName) {
    return toolExecutor.execute(
        "acceptGift",
        () -> {
          Long userId = UserContext.requireCurrentUserId();
          GiveGiftVO r = fudiService.giveGiftInternal(userId, itemName);
          return new AcceptGiftResponse(r.itemName(), r.change(), r.reaction());
        });
  }

  @Tool(description = "为主人触发天劫渡劫考验。仅在主人明确要求渡劫、突破或迎接天劫时调用")
  @Transactional
  public TriggerTribulationVO triggerTribulation() {
    return toolExecutor.execute(
        "triggerTribulation",
        () -> {
          Long userId = UserContext.requireCurrentUserId();
          return fudiService.triggerTribulationInternal(userId);
        });
  }

  // ===== Response records =====

  public record AcceptGiftResponse(
      @JsonPropertyDescription("收到的礼物物品名称") String itemName,
      @JsonPropertyDescription("好感度变化值：正数表示上升，负数表示下降") int affectionChange,
      @JsonPropertyDescription("地灵收到礼物后的反应描述，可直接告诉主人") String reaction) {}

  public record FeelOffendedResponse(
      @JsonPropertyDescription("触发冒犯的具体原因描述") String reason,
      @JsonPropertyDescription("冒犯程度（1-5）") int severity) {}
}
