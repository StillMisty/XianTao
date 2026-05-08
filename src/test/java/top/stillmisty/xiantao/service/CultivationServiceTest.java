package top.stillmisty.xiantao.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import top.stillmisty.xiantao.domain.pill.enums.PlayerBuffType;
import top.stillmisty.xiantao.domain.pill.repository.PlayerBuffRepository;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.domain.user.enums.UserStatus;
import top.stillmisty.xiantao.domain.user.vo.BreakthroughResult;
import top.stillmisty.xiantao.domain.user.vo.DaoProtectionQueryResult;
import top.stillmisty.xiantao.domain.user.vo.DaoProtectionResult;

@DisplayName("CultivationService 测试")
@ExtendWith(MockitoExtension.class)
class CultivationServiceTest {

  @Mock private UserStateService userStateService;
  @Mock private PlayerBuffRepository playerBuffRepository;
  @Mock private ProtectionHelper protectionHelper;
  @Mock private DaoProtectionService daoProtectionService;

  @InjectMocks private CultivationService cultivationService;

  private final Long userId = 1L;

  private User createUser(int level, long exp, int breakthroughFailCount) {
    return User.create()
        .setId(userId)
        .setNickname("测试修士")
        .setLevel(level)
        .setExp(exp)
        .setSpiritStones(1000L)
        .setStatStr(10)
        .setStatCon(10)
        .setStatAgi(10)
        .setStatWis(10)
        .setHpCurrent(300)
        .setBreakthroughFailCount(breakthroughFailCount)
        .setStatus(UserStatus.IDLE)
        .setLocationId(1L);
  }

  // ===================== attemptBreakthrough =====================

  @Test
  @DisplayName("attemptBreakthrough — 经验不足时返回失败结果")
  void attemptBreakthrough_whenExpInsufficient_shouldReturnFailedResult() {
    User user = createUser(5, 0L, 0);
    when(userStateService.loadUser(userId)).thenReturn(user);

    BreakthroughResult result = cultivationService.attemptBreakthrough(userId);

    assertFalse(result.success());
    assertTrue(result.message().contains("修为不足"));
  }

  @Test
  @DisplayName("attemptBreakthrough — 突破后清除了护道关系和丹药加成")
  void attemptBreakthrough_shouldClearRelationsAndBuffs() {
    User user = createUser(2, 1000L, 0);
    when(userStateService.loadUser(userId)).thenReturn(user);
    when(playerBuffRepository.findActiveByUserIdAndType(userId, PlayerBuffType.BREAKTHROUGH))
        .thenReturn(List.of());

    BreakthroughResult result = cultivationService.attemptBreakthrough(userId);

    assertNotNull(result);
    verify(daoProtectionService).clearProtegeRelations(userId);
    verify(playerBuffRepository).deleteByUserIdAndType(userId, PlayerBuffType.BREAKTHROUGH);
  }

  // ===================== establishProtection (delegates to DaoProtectionService)
  // =====================

  @Test
  @DisplayName("establishProtection — 委托给 DaoProtectionService")
  void establishProtection_shouldDelegateToDaoProtectionService() {
    DaoProtectionResult mockResult =
        new DaoProtectionResult(
            false, "未找到道号为【不存在的道友】的修士", null, null, null, null, null, null, null, null, null);
    when(daoProtectionService.establishProtection(userId, "不存在的道友")).thenReturn(mockResult);

    DaoProtectionResult result = cultivationService.establishProtection(userId, "不存在的道友");

    assertFalse(result.success());
    assertTrue(result.message().contains("未找到"));
  }

  @Test
  @DisplayName("establishProtection — 境界不足委托给 DaoProtectionService")
  void establishProtection_whenProtectorLevelTooLow_shouldDelegate() {
    DaoProtectionResult mockResult =
        new DaoProtectionResult(
            false, "你的境界（第2层）低于高境界道友（第10层）", null, null, null, null, null, null, null, null, null);
    when(daoProtectionService.establishProtection(userId, "高境界道友")).thenReturn(mockResult);

    DaoProtectionResult result = cultivationService.establishProtection(userId, "高境界道友");

    assertFalse(result.success());
    assertTrue(result.message().contains("境界"));
  }

  @Test
  @DisplayName("establishProtection — 成功建立护道关系委托给 DaoProtectionService")
  void establishProtection_whenValid_shouldDelegate() {
    DaoProtectionResult mockResult =
        new DaoProtectionResult(true, "已与弟子建立护道契约！", 1L, "护道者", 10, 2L, "弟子", 5, 10.0, null, true);
    when(daoProtectionService.establishProtection(userId, "弟子")).thenReturn(mockResult);

    DaoProtectionResult result = cultivationService.establishProtection(userId, "弟子");

    assertTrue(result.success());
    assertTrue(result.message().contains("护道契约"));
  }

  // ===================== removeProtection (delegates to DaoProtectionService)
  // =====================

  @Test
  @DisplayName("removeProtection — 委托给 DaoProtectionService")
  void removeProtection_shouldDelegateToDaoProtectionService() {
    DaoProtectionResult mockResult =
        new DaoProtectionResult(
            false, "你并未为弟子护道", null, null, null, null, null, null, null, null, null);
    when(daoProtectionService.removeProtection(userId, "弟子")).thenReturn(mockResult);

    DaoProtectionResult result = cultivationService.removeProtection(userId, "弟子");

    assertFalse(result.success());
    assertTrue(result.message().contains("并未"));
  }

  // ===================== queryProtectionInfo (delegates to DaoProtectionService)
  // =====================

  @Test
  @DisplayName("queryProtectionInfo — 无人护道时委托给 DaoProtectionService 返回空结果")
  void queryProtectionInfo_whenNoProtections_shouldDelegate() {
    DaoProtectionQueryResult mockResult =
        DaoProtectionQueryResult.builder()
            .success(true)
            .message("天地孤寂，无道友相护。")
            .totalBonusPercentage(0.0)
            .build();
    when(daoProtectionService.queryProtectionInfo(userId)).thenReturn(mockResult);

    DaoProtectionQueryResult result = cultivationService.queryProtectionInfo(userId);

    assertTrue(result.isSuccess());
    assertTrue(result.getMessage().contains("无道友相护"));
    assertEquals(0.0, result.getTotalBonusPercentage());
  }

  @Test
  @DisplayName("queryProtectionInfo — 同地点护道者委托给 DaoProtectionService")
  void queryProtectionInfo_withSameLocationProtectors_shouldDelegate() {
    DaoProtectionQueryResult mockResult =
        DaoProtectionQueryResult.builder()
            .success(true)
            .message("共有 1 位道友为你护道")
            .totalBonusPercentage(10.0)
            .build();
    when(daoProtectionService.queryProtectionInfo(userId)).thenReturn(mockResult);

    DaoProtectionQueryResult result = cultivationService.queryProtectionInfo(userId);

    assertTrue(result.isSuccess());
  }

  @Test
  @DisplayName("queryProtectionInfo — 非同一地点委托给 DaoProtectionService")
  void queryProtectionInfo_whenDifferentLocation_shouldDelegate() {
    DaoProtectionQueryResult mockResult =
        DaoProtectionQueryResult.builder()
            .success(true)
            .message("虽有道友护道，但皆不在同地点")
            .totalBonusPercentage(0.0)
            .build();
    when(daoProtectionService.queryProtectionInfo(userId)).thenReturn(mockResult);

    DaoProtectionQueryResult result = cultivationService.queryProtectionInfo(userId);

    assertEquals(0.0, result.getTotalBonusPercentage());
    assertTrue(result.getMessage().contains("不在同地点"));
  }
}
