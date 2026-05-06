package top.stillmisty.xiantao.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.user.entity.DaoProtection;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.domain.user.repository.DaoProtectionRepository;
import top.stillmisty.xiantao.domain.user.repository.UserRepository;
import top.stillmisty.xiantao.domain.user.vo.DaoProtectionQueryResult;
import top.stillmisty.xiantao.domain.user.vo.DaoProtectionResult;
import top.stillmisty.xiantao.domain.user.vo.ProtectionInfo;

@Slf4j
@Service
@RequiredArgsConstructor
public class DaoProtectionService {

  static final int MAX_PROTECTOR_COUNT = 3;
  private final UserRepository userRepository;
  private final UserStateService userStateService;
  private final MapService mapService;
  private final DaoProtectionRepository daoProtectionRepository;
  private final ProtectionHelper protectionHelper;

  @Transactional
  public DaoProtectionResult establishProtection(Long protectorId, String protegeNickname) {
    User protector = userStateService.loadUser(protectorId);

    Optional<User> protegeOpt = findUserByNickname(protegeNickname);
    if (protegeOpt.isEmpty()) {
      return new DaoProtectionResult(
          false,
          String.format("未找到道号为【%s】的修士", protegeNickname),
          null,
          null,
          null,
          null,
          null,
          null,
          null,
          null,
          null);
    }

    User protege = protegeOpt.get();

    if (protector.getLevel() < protege.getLevel()) {
      return new DaoProtectionResult(
          false,
          String.format(
              "你的境界（第%d层）低于%s（第%d层），无法为其护道",
              protector.getLevel(), protege.getNickname(), protege.getLevel()),
          null,
          null,
          null,
          null,
          null,
          null,
          null,
          null,
          null);
    }

    long currentProtectingCount = daoProtectionRepository.countByProtectorId(protectorId);
    if (currentProtectingCount >= MAX_PROTECTOR_COUNT) {
      return new DaoProtectionResult(
          false,
          String.format("你当前已在为%d位道友护道，分身乏术。请先使用「护道解除」解除部分关系", MAX_PROTECTOR_COUNT),
          null,
          null,
          null,
          null,
          null,
          null,
          null,
          null,
          null);
    }

    Optional<DaoProtection> existingRelation =
        daoProtectionRepository.findByProtectorAndProtege(protectorId, protege.getId());
    if (existingRelation.isPresent()) {
      return new DaoProtectionResult(
          false,
          String.format("你已在为%s护道", protege.getNickname()),
          null,
          null,
          null,
          null,
          null,
          null,
          null,
          null,
          null);
    }

    DaoProtection protection =
        DaoProtection.create().setProtectorId(protectorId).setProtegeId(protege.getId());
    daoProtectionRepository.save(protection);

    double singleBonus = protectionHelper.calculateSingleProtectorBonus(protector, protege);

    return new DaoProtectionResult(
        true,
        String.format(
            "已与%s建立护道契约！当其在同地点突破时，你将提供 %.1f%% 的成功率加成", protege.getNickname(), singleBonus),
        protector.getId(),
        protector.getNickname(),
        protector.getLevel(),
        protege.getId(),
        protege.getNickname(),
        protege.getLevel(),
        singleBonus,
        null,
        protectionHelper.isInSameLocation(protector, protege));
  }

  @Transactional
  public DaoProtectionResult removeProtection(Long protectorId, String protegeNickname) {
    Optional<User> protegeOpt = findUserByNickname(protegeNickname);
    if (protegeOpt.isEmpty()) {
      return new DaoProtectionResult(
          false,
          String.format("未找到道号为【%s】的修士", protegeNickname),
          null,
          null,
          null,
          null,
          null,
          null,
          null,
          null,
          null);
    }

    User protege = protegeOpt.get();

    Optional<DaoProtection> protectionOpt =
        daoProtectionRepository.findByProtectorAndProtege(protectorId, protege.getId());
    if (protectionOpt.isEmpty()) {
      return new DaoProtectionResult(
          false,
          String.format("你并未为%s护道", protege.getNickname()),
          null,
          null,
          null,
          null,
          null,
          null,
          null,
          null,
          null);
    }

    daoProtectionRepository.deleteById(protectionOpt.get().getId());

    return new DaoProtectionResult(
        true,
        String.format("已解除与%s的护道契约", protege.getNickname()),
        protectorId,
        null,
        null,
        protege.getId(),
        protege.getNickname(),
        null,
        null,
        null,
        null);
  }

  public DaoProtectionQueryResult queryProtectionInfo(Long userId) {
    User user = userStateService.loadUser(userId);

    List<ProtectionInfo> protectingInfoList = buildProtectingList(userId, user);
    ProtectionByData protectedByData = buildProtectedByList(userId, user);

    String message = buildProtectionMessage(protectedByData);

    return DaoProtectionQueryResult.builder()
        .success(true)
        .message(message)
        .protectingList(protectingInfoList)
        .protectingCount(protectingInfoList.size())
        .maxProtectingCount(MAX_PROTECTOR_COUNT)
        .protectedByList(protectedByData.infoList)
        .totalBonusPercentage(protectedByData.totalBonus)
        .allInSameLocation(protectedByData.allInSameLocation)
        .build();
  }

  private List<ProtectionInfo> buildProtectingList(Long userId, User user) {
    List<DaoProtection> protectingList = daoProtectionRepository.findByProtectorId(userId);
    List<ProtectionInfo> protectingInfoList = new ArrayList<>();

    if (protectingList.isEmpty()) {
      return protectingInfoList;
    }

    var protegeIds = protectingList.stream().map(DaoProtection::getProtegeId).distinct().toList();
    var protegeMap =
        userRepository.findByIds(protegeIds).stream()
            .collect(
                java.util.stream.Collectors.toMap(
                    top.stillmisty.xiantao.domain.user.entity.User::getId, u -> u));

    for (DaoProtection protection : protectingList) {
      User protege = protegeMap.get(protection.getProtegeId());
      if (protege != null) {
        boolean inSameLocation = protectionHelper.isInSameLocation(user, protege);
        double bonus = protectionHelper.calculateSingleProtectorBonus(user, protege);
        protectingInfoList.add(
            ProtectionInfo.builder()
                .userId(protege.getId())
                .userName(protege.getNickname())
                .userLevel(protege.getLevel())
                .locationId(protege.getLocationId())
                .locationName(mapService.getMapName(protege.getLocationId()))
                .isInSameLocation(inSameLocation)
                .bonusPercentage(bonus)
                .build());
      }
    }
    return protectingInfoList;
  }

  private ProtectionByData buildProtectedByList(Long userId, User user) {
    List<DaoProtection> protectedByList = daoProtectionRepository.findByProtegeId(userId);
    List<ProtectionInfo> protectedByInfoList = new ArrayList<>();
    double totalBonus = 0.0;
    int sameLocationCount = 0;

    if (protectedByList.isEmpty()) {
      return new ProtectionByData(protectedByInfoList, totalBonus, false);
    }

    var protectorIds =
        protectedByList.stream().map(DaoProtection::getProtectorId).distinct().toList();
    var protectorMap =
        userRepository.findByIds(protectorIds).stream()
            .collect(
                java.util.stream.Collectors.toMap(
                    top.stillmisty.xiantao.domain.user.entity.User::getId, u -> u));

    for (DaoProtection protection : protectedByList) {
      User protector = protectorMap.get(protection.getProtectorId());
      if (protector != null) {
        boolean inSameLocation = protectionHelper.isInSameLocation(user, protector);
        double bonus = 0.0;
        if (inSameLocation) {
          bonus = protectionHelper.calculateSingleProtectorBonus(protector, user);
          totalBonus += bonus;
          sameLocationCount++;
        }
        protectedByInfoList.add(
            ProtectionInfo.builder()
                .userId(protector.getId())
                .userName(protector.getNickname())
                .userLevel(protector.getLevel())
                .locationId(protector.getLocationId())
                .locationName(mapService.getMapName(protector.getLocationId()))
                .isInSameLocation(inSameLocation)
                .bonusPercentage(bonus)
                .build());
      }
    }

    totalBonus = Math.min(ProtectionHelper.MAX_TOTAL_BONUS_PERCENTAGE, totalBonus);
    boolean allInSameLocation =
        sameLocationCount == protectedByInfoList.size() && !protectedByInfoList.isEmpty();
    return new ProtectionByData(protectedByInfoList, totalBonus, allInSameLocation);
  }

  private String buildProtectionMessage(ProtectionByData data) {
    if (data.infoList.isEmpty()) {
      return "天地孤寂，无道友相护。";
    }
    int sameLocationCount =
        (int)
            data.infoList.stream()
                .filter(pi -> Boolean.TRUE.equals(pi.getIsInSameLocation()))
                .count();
    if (sameLocationCount == 0) {
      return "虽有道友护道，但皆不在同地点，无法提供加成。";
    }
    return String.format(
        "共有 %d 位道友为你护道，其中 %d 位在同地点，总加成 %.1f%%",
        data.infoList.size(), sameLocationCount, data.totalBonus);
  }

  private record ProtectionByData(
      List<ProtectionInfo> infoList, double totalBonus, boolean allInSameLocation) {}

  private Optional<User> findUserByNickname(String nickname) {
    return userRepository.findByNickname(nickname);
  }

  public void clearProtegeRelations(Long protegeId) {
    daoProtectionRepository.deleteByProtegeId(protegeId);
  }
}
