package top.stillmisty.xiantao.service.inventory.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.item.entity.ItemTemplate;
import top.stillmisty.xiantao.domain.item.entity.StackableItem;
import top.stillmisty.xiantao.domain.item.enums.ItemType;
import top.stillmisty.xiantao.domain.skill.vo.SkillSlotResult;
import top.stillmisty.xiantao.service.skill.SkillService;

@Component
@RequiredArgsConstructor
public class SkillJadeUseHandler implements ItemUseHandler {

  private final SkillService skillService;

  @Override
  public ItemType getItemType() {
    return ItemType.SKILL_JADE;
  }

  @Override
  public boolean consumesInternally() {
    return true;
  }

  @Override
  public String use(Long userId, StackableItem item, ItemTemplate template, String args) {
    SkillSlotResult result = skillService.learnFromJade(userId, item.getName());
    StringBuilder sb = new StringBuilder();
    sb.append(result.getMessage());
    if (result.isSuccess() && result.getSkill() != null) {
      sb.append("\n习得法决：").append(result.getSkill().name());
    }
    return sb.toString();
  }
}
