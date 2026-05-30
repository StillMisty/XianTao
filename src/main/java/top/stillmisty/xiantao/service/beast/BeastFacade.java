package top.stillmisty.xiantao.service.beast;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.beast.entity.Beast;
import top.stillmisty.xiantao.domain.beast.vo.*;
import top.stillmisty.xiantao.domain.fudi.vo.PenCellVO;
import top.stillmisty.xiantao.domain.item.entity.ItemTemplate;
import top.stillmisty.xiantao.infrastructure.repository.BeastRepository;

/** 灵兽外观 - 提供统一的灵兽系统接口 封装内部多个服务的交互，简化调用方的使用 */
@Component
@RequiredArgsConstructor
public class BeastFacade {

  private final BeastRepository beastRepository;
  private final BeastBreedingService breedingService;
  private final BeastCombatService combatService;
  private final BeastDisplayHelper displayHelper;

  // ===================== 查询接口 =====================

  /**
   * 获取用户的灵兽列表
   *
   * @param userId 用户ID
   * @return 灵兽状态VO列表
   */
  public List<BeastStatusVO> getBeastList(Long userId) {
    return combatService.getBeastListInternal(userId);
  }

  /**
   * 获取已部署的灵兽列表
   *
   * @param userId 用户ID
   * @return 已部署的灵兽状态VO列表
   */
  public List<BeastStatusVO> getDeployedBeasts(Long userId) {
    return combatService.getDeployedBeastsInternal(userId);
  }

  // ===================== 操作接口 =====================

  /**
   * 切换灵兽部署状态
   *
   * @param userId 用户ID
   * @param position 灵兽位置
   * @return 操作结果消息
   */
  public String toggleDeploy(Long userId, String position) {
    return combatService.toggleDeploy(userId, position);
  }

  /**
   * 孵化灵兽
   *
   * @param userId 用户ID
   * @param cellId 巢穴格子ID
   * @param eggTemplate 灵蛋模板
   * @return 孵化结果VO
   */
  public PenCellVO hatchBeast(Long userId, Integer cellId, ItemTemplate eggTemplate) {
    return breedingService.hatchBeastWithTemplate(userId, cellId, eggTemplate);
  }

  /**
   * 配种灵兽
   *
   * @param userId 用户ID
   * @param position1 第一个灵兽位置
   * @param position2 第二个灵兽位置
   * @return 配种结果
   */
  public BeastBreedingService.BreedResult breed(Long userId, String position1, String position2) {
    return breedingService.breed(userId, position1, position2);
  }

  // ===================== 内部接口 =====================

  /**
   * 根据福地ID获取所有灵兽
   *
   * @param fudiId 福地ID
   * @return 灵兽列表
   */
  public List<Beast> getBeastsByFudiId(Long fudiId) {
    return beastRepository.findByFudiId(fudiId);
  }

  /**
   * 根据用户ID获取所有灵兽
   *
   * @param userId 用户ID
   * @return 灵兽列表
   */
  public List<Beast> getBeastsByUserId(Long userId) {
    return beastRepository.findByUserId(userId);
  }
}
