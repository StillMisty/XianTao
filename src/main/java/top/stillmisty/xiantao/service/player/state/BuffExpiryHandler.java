package top.stillmisty.xiantao.service.player.state;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.notification.entity.GameEvent;
import top.stillmisty.xiantao.domain.notification.enums.GameEventCategory;
import top.stillmisty.xiantao.domain.pill.entity.PlayerBuff;
import top.stillmisty.xiantao.domain.user.entity.Player;
import top.stillmisty.xiantao.infrastructure.repository.PlayerBuffRepository;
import top.stillmisty.xiantao.service.GameEventService;

/** 过期 Buff 结算 — 删除过期行并产出 {@code BUFF_EXPIRED} 到期提醒（无框线纯文本）。 */
@Component
@RequiredArgsConstructor
@Order(4)
class BuffExpiryHandler implements StateHandler {

  private final PlayerBuffRepository playerBuffRepository;
  private final GameEventService gameEventService;

  @Override
  public boolean tryResolve(Player user) {
    List<PlayerBuff> expired = playerBuffRepository.findExpiredByUserId(user.getId());
    if (expired.isEmpty()) return false;

    playerBuffRepository.deleteExpiredByUserId(user.getId());

    // 同一批到期合并为一条提醒，避免多条 buff 同时到期刷屏
    String buffNames =
        expired.stream()
            .map(buff -> "「" + buff.getBuffType().getDisplayName() + "」")
            .distinct()
            .collect(Collectors.joining("、"));
    gameEventService.save(
        GameEvent.create(user.getId(), GameEventCategory.BUFF_EXPIRED)
            .withNarrative("{{buffNames}}的增益效果已消散。", Map.of("buffNames", buffNames)));
    return false;
  }
}
