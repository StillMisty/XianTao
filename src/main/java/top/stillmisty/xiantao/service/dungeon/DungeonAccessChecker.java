package top.stillmisty.xiantao.service.dungeon;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.dungeon.entity.DungeonTemplate;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.infrastructure.repository.DungeonProgressRepository;
import top.stillmisty.xiantao.infrastructure.repository.HiddenCompletionRepository;
import top.stillmisty.xiantao.infrastructure.repository.StackableItemRepository;
import top.stillmisty.xiantao.service.BusinessException;
import top.stillmisty.xiantao.service.ErrorCode;

@Component
@RequiredArgsConstructor
public class DungeonAccessChecker {

  private final StackableItemRepository stackableItemRepository;
  private final DungeonProgressRepository progressRepository;
  private final HiddenCompletionRepository hiddenCompletionRepository;

  public boolean canAccess(User user, DungeonTemplate dungeon) {
    try {
      checkAccess(user, dungeon);
      return true;
    } catch (BusinessException e) {
      return false;
    }
  }

  public void checkAccess(User user, DungeonTemplate dungeon) {
    List<DungeonTemplate.AccessCondition> conditions = dungeon.getAccessRules();
    if (conditions == null || conditions.isEmpty()) return;

    for (DungeonTemplate.AccessCondition condition : conditions) {
      switch (condition.type()) {
        case "MAP_NODE" -> {
          if (condition.nodeIds() != null && !condition.nodeIds().isEmpty()) {
            boolean atNode =
                user.getLocationId() != null && condition.nodeIds().contains(user.getLocationId());
            if (!atNode) {
              throw new BusinessException(ErrorCode.DUNGEON_NOT_AT_ENTRANCE, dungeon.getName());
            }
          }
        }
        case "LEVEL" -> {
          int level = user.getLevel() != null ? user.getLevel() : 0;
          if (condition.min() != null && level < condition.min()) {
            throw new BusinessException(
                ErrorCode.DUNGEON_LEVEL_INSUFFICIENT,
                dungeon.getName(),
                condition.min(),
                condition.max() != null ? condition.max() : Integer.MAX_VALUE);
          }
          if (condition.max() != null && level > condition.max()) {
            throw new BusinessException(
                ErrorCode.DUNGEON_LEVEL_INSUFFICIENT,
                dungeon.getName(),
                condition.min() != null ? condition.min() : 0,
                condition.max());
          }
        }
        case "SECT" -> {
          throw new BusinessException(ErrorCode.DUNGEON_STATUS_BLOCKED, "宗门限制暂未实现");
        }
        case "ITEM" -> {
          if (condition.templateId() != null) {
            boolean hasItem =
                stackableItemRepository
                    .findByUserIdAndTemplateId(user.getId(), condition.templateId())
                    .isPresent();
            if (!hasItem) {
              throw new BusinessException(ErrorCode.DUNGEON_STATUS_BLOCKED, "你没有进入秘境的钥匙");
            }
          }
        }
        case "DUNGEON_CLEARED" -> {
          if (condition.dungeonId() != null) {
            boolean cleared =
                progressRepository
                    .findByUserIdAndDungeonId(user.getId(), condition.dungeonId())
                    .map(p -> Boolean.TRUE.equals(p.getFirstClear()))
                    .orElse(false);
            if (!cleared) {
              throw new BusinessException(ErrorCode.DUNGEON_STATUS_BLOCKED, "你需要先通关指定秘境");
            }
          }
        }
        case "HIDDEN_COMPLETION" -> {
          if (condition.code() != null) {
            boolean completed =
                hiddenCompletionRepository.existsByCode(user.getId(), condition.code());
            if (!completed) {
              throw new BusinessException(ErrorCode.DUNGEON_STATUS_BLOCKED, "你需要先完成指定任务");
            }
          }
        }
        default -> {
          throw new BusinessException(ErrorCode.PARAM_INVALID, "未知秘境入口条件类型: " + condition.type());
        }
      }
    }
  }
}
