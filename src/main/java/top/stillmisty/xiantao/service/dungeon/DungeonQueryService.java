package top.stillmisty.xiantao.service.dungeon;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.dungeon.entity.DungeonInstance;
import top.stillmisty.xiantao.domain.dungeon.entity.DungeonProgress;
import top.stillmisty.xiantao.domain.dungeon.entity.DungeonTemplate;
import top.stillmisty.xiantao.domain.dungeon.enums.DungeonStatus;
import top.stillmisty.xiantao.domain.dungeon.vo.DungeonListVO;
import top.stillmisty.xiantao.infrastructure.repository.DungeonInstanceRepository;
import top.stillmisty.xiantao.infrastructure.repository.DungeonProgressRepository;
import top.stillmisty.xiantao.infrastructure.repository.DungeonTemplateRepository;
import top.stillmisty.xiantao.service.ServiceResult;
import top.stillmisty.xiantao.service.player.UserStateService;

/**
 * 秘境查询服务
 *
 * <p>负责秘境列表查询等只读操作，从DungeonService中提取以降低耦合。
 */
@Service
@RequiredArgsConstructor
public class DungeonQueryService {

  private final DungeonTemplateRepository dungeonTemplateRepository;
  private final DungeonInstanceRepository instanceRepository;
  private final DungeonProgressRepository progressRepository;
  private final UserStateService userStateService;

  /**
   * 获取秘境列表（公开API）
   *
   * @param userId 用户ID
   * @return 秘境列表结果
   */
  @Transactional(readOnly = true)
  public ServiceResult<List<DungeonListVO>> listDungeons(Long userId) {
    return new ServiceResult.Success<>(listDungeonsInternal(userId));
  }

  /**
   * 获取秘境列表（内部API，供缓存调用）
   *
   * @param userId 用户ID
   * @return 秘境列表
   */
  @Cacheable(cacheNames = "dungeon_list", key = "#userId")
  public List<DungeonListVO> listDungeonsInternal(Long userId) {
    var user = userStateService.loadUserReadOnly(userId);
    List<DungeonTemplate> templates = dungeonTemplateRepository.findActive();

    List<Long> templateIds = templates.stream().map(DungeonTemplate::getId).toList();
    Map<Long, DungeonProgress> progressMap =
        progressRepository.findByUserIdAndDungeonIds(userId, templateIds).stream()
            .collect(Collectors.toMap(DungeonProgress::getDungeonId, p -> p, (a, b) -> a));
    Map<Long, DungeonInstance> activeInstances =
        instanceRepository
            .findByLeaderIdAndDungeonIdsAndStatus(userId, templateIds, DungeonStatus.ACTIVE)
            .stream()
            .collect(Collectors.toMap(DungeonInstance::getDungeonId, i -> i, (a, b) -> a));

    List<DungeonListVO> result = new ArrayList<>();
    for (DungeonTemplate tmpl : templates) {
      DungeonProgress progress = progressMap.get(tmpl.getId());
      DungeonInstance activeInstance = activeInstances.get(tmpl.getId());

      result.add(
          new DungeonListVO(
              tmpl.getId(),
              tmpl.getName(),
              tmpl.getMinLevel(),
              tmpl.getMaxLevel(),
              tmpl.getMaxTeamSize(),
              activeInstance != null,
              activeInstance != null ? activeInstance.getStatus() : null,
              activeInstance != null ? activeInstance.getCurrentArea() : null,
              progress != null ? progress.getRewardCount() : 0,
              progress != null
                  ? progress.getDailyLimit()
                  : DungeonProgress.calculateDailyLimit(user.getLevel()),
              progress != null && progress.getFirstClear()));
    }
    return result;
  }
}
