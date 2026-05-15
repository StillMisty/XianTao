package top.stillmisty.xiantao.service.enhance;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.forge.vo.EnhanceResultVO;
import top.stillmisty.xiantao.domain.item.entity.Equipment;
import top.stillmisty.xiantao.service.fudi.FudiHelper;

/** 强化安全期 +0→+3：100% 成功，仅消耗灵石 */
@Component
@RequiredArgsConstructor
public class SafeEnhanceRegime {

  private final EnhancementCore core;
  private final FudiHelper fudiHelper;

  public boolean canHandle(int targetLevel) {
    return targetLevel <= 3;
  }

  public EnhanceResultVO executeAuto(
      Long userId, Equipment equipment, int currentLevel, int targetLevel, int stoneCost) {
    fudiHelper.deductSpiritStones(userId, stoneCost);
    return core.applyEnhanceSuccess(equipment, targetLevel, stoneCost, null, userId);
  }
}
