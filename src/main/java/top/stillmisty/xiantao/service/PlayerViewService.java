package top.stillmisty.xiantao.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.stillmisty.xiantao.domain.item.entity.Equipment;
import top.stillmisty.xiantao.domain.item.repository.EquipmentRepository;
import top.stillmisty.xiantao.domain.map.repository.MapNodeRepository;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.domain.user.repository.UserRepository;
import top.stillmisty.xiantao.domain.user.vo.PlayerViewVO;
import top.stillmisty.xiantao.service.annotation.Authenticated;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlayerViewService {

  private final UserRepository userRepository;
  private final EquipmentRepository equipmentRepository;
  private final MapNodeRepository mapNodeRepository;

  @Authenticated
  public ServiceResult<PlayerViewVO> viewPlayer(
      PlatformType platform, String openId, String targetNickname) {
    try {
      Long userId = UserContext.getCurrentUserId();
      return new ServiceResult.Success<>(viewPlayer(userId, targetNickname));
    } catch (IllegalStateException e) {
      return ServiceResult.businessFailure(e.getMessage());
    }
  }

  public PlayerViewVO viewPlayer(Long userId, String targetNickname) {
    User target =
        userRepository
            .findByNickname(targetNickname)
            .orElseThrow(() -> new IllegalStateException("未找到玩家【" + targetNickname + "】"));

    String locationName = null;
    var mapNode = mapNodeRepository.findById(target.getLocationId());
    if (mapNode.isPresent()) {
      locationName = mapNode.get().getName();
    }

    List<Equipment> equipped = equipmentRepository.findEquippedByUserId(target.getId());
    List<String> equippedNames =
        equipped.stream().map(e -> e.getSlot().getName() + "：" + e.getName()).toList();

    String statusName = target.getStatus() != null ? target.getStatus().getName() : "未知";

    return new PlayerViewVO(
        target.getNickname(),
        target.getLevel(),
        target.getHpCurrent(),
        target.calculateMaxHp(),
        0,
        0,
        target.getEffectiveStatStr(),
        target.getEffectiveStatCon(),
        target.getEffectiveStatAgi(),
        target.getEffectiveStatWis(),
        locationName,
        statusName,
        equippedNames);
  }
}
