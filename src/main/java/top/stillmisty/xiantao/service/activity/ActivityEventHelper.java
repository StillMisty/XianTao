package top.stillmisty.xiantao.service.activity;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.event.entity.ActivityEvent;
import top.stillmisty.xiantao.domain.event.repository.EventTypeRepository;
import top.stillmisty.xiantao.domain.event.repository.HiddenCompletionRepository;

/** 活动事件辅助 — checkPrerequisite / resolveNarrativeKey，供 Training/Bounty/Travel Completer 共用 */
@Component
@RequiredArgsConstructor
public class ActivityEventHelper {

  private final HiddenCompletionRepository hiddenCompletionRepository;
  private final EventTypeRepository eventTypeRepository;

  /** 检查前置事件是否已完成 */
  public boolean checkPrerequisite(Long userId, ActivityEvent event) {
    String prerequisiteCode = event.getPrerequisiteCode();
    if (prerequisiteCode == null) return true;
    return hiddenCompletionRepository.existsByCode(userId, prerequisiteCode);
  }

  /** 将 event code 解析为叙事描述文本 */
  public String resolveNarrativeKey(String code) {
    return eventTypeRepository.findByCode(code).map(e -> e.getDescription()).orElse(code);
  }
}
