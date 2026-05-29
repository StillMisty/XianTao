package top.stillmisty.xiantao.service.player;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.stillmisty.xiantao.domain.item.entity.Equipment;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.domain.user.enums.CultivationRealm;
import top.stillmisty.xiantao.domain.user.vo.PlayerViewVO;
import top.stillmisty.xiantao.infrastructure.repository.EquipmentRepository;
import top.stillmisty.xiantao.infrastructure.repository.MapNodeRepository;
import top.stillmisty.xiantao.infrastructure.repository.UserRepository;
import top.stillmisty.xiantao.service.BusinessException;
import top.stillmisty.xiantao.service.ErrorCode;
import top.stillmisty.xiantao.service.ServiceResult;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlayerViewService {

  private final UserRepository userRepository;
  private final EquipmentRepository equipmentRepository;
  private final MapNodeRepository mapNodeRepository;

  public ServiceResult<PlayerViewVO> viewPlayer(Long userId, String targetNickname) {
    return new ServiceResult.Success<>(viewPlayerInternal(userId, targetNickname));
  }

  public PlayerViewVO viewPlayerInternal(Long userId, String targetNickname) {
    User target =
        userRepository
            .findByNickname(targetNickname)
            .orElseThrow(() -> new BusinessException(ErrorCode.PLAYER_NOT_FOUND, targetNickname));

    String locationName = null;
    var mapNode = mapNodeRepository.findById(target.getLocationId());
    if (mapNode.isPresent()) {
      locationName = mapNode.get().getName();
    }

    List<Equipment> equipped = equipmentRepository.findEquippedByUserId(target.getId());
    List<String> equippedNames =
        equipped.stream().map(e -> e.getSlot().getName() + "：" + e.getName()).toList();

    String statusName = target.getStatus() != null ? target.getStatus().getName() : "未知";

    int equipAttack = 0, equipDefense = 0;
    for (Equipment e : equipped) {
      equipAttack += e.getFinalAttack();
      equipDefense += e.getFinalDefense();
    }
    int attack = target.getEffectiveStatStr() * 2 + equipAttack;
    int defense = target.getEffectiveStatCon() + equipDefense;

    return new PlayerViewVO(
        target.getNickname(),
        target.getLevel(),
        CultivationRealm.realmDisplay(target.getLevel()),
        target.getHpCurrent(),
        target.calculateMaxHp(),
        attack,
        defense,
        target.getEffectiveStatStr(),
        target.getEffectiveStatCon(),
        target.getEffectiveStatAgi(),
        target.getEffectiveStatWis(),
        locationName,
        statusName,
        equippedNames);
  }
}
