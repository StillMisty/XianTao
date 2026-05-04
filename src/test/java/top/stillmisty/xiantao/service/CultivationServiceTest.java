package top.stillmisty.xiantao.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import top.stillmisty.xiantao.domain.pill.entity.PlayerBuff;
import top.stillmisty.xiantao.domain.pill.repository.PlayerBuffRepository;
import top.stillmisty.xiantao.domain.user.entity.DaoProtection;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.domain.user.enums.UserStatus;
import top.stillmisty.xiantao.domain.user.repository.DaoProtectionRepository;
import top.stillmisty.xiantao.domain.user.repository.UserRepository;
import top.stillmisty.xiantao.domain.user.vo.BreakthroughResult;
import top.stillmisty.xiantao.domain.user.vo.DaoProtectionQueryResult;
import top.stillmisty.xiantao.domain.user.vo.DaoProtectionResult;
import top.stillmisty.xiantao.service.UserStateService;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("CultivationService 测试")
@ExtendWith(MockitoExtension.class)
class CultivationServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserStateService userStateService;
    @Mock
    private MapService mapService;
    @Mock
    private DaoProtectionRepository daoProtectionRepository;
    @Mock
    private PlayerBuffRepository playerBuffRepository;

    @InjectMocks
    private CultivationService cultivationService;

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
        when(userStateService.getUser(userId)).thenReturn(user);

        BreakthroughResult result = cultivationService.attemptBreakthrough(userId);

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("修为不足"));
    }

    @Test
    @DisplayName("attemptBreakthrough — 突破后清除了护道关系和丹药加成")
    void attemptBreakthrough_shouldClearRelationsAndBuffs() {
        User user = createUser(2, 1000L, 0);
        when(userStateService.getUser(userId)).thenReturn(user);
        when(daoProtectionRepository.findByProtegeId(userId)).thenReturn(List.of());
        when(playerBuffRepository.findActiveByUserIdAndType(userId, "breakthrough")).thenReturn(List.of());

        BreakthroughResult result = cultivationService.attemptBreakthrough(userId);

        assertNotNull(result);
        verify(daoProtectionRepository).deleteByProtegeId(userId);
        verify(playerBuffRepository).deleteByUserIdAndType(userId, "breakthrough");
    }

    // ===================== establishProtection =====================

    @Test
    @DisplayName("establishProtection — 被护道者不存在返回失败")
    void establishProtection_whenProtegeNotFound_shouldReturnFailure() {
        User protector = createUser(5, 0L, 0);
        when(userStateService.getUser(userId)).thenReturn(protector);
        when(userRepository.findByNickname("不存在的道友")).thenReturn(Optional.empty());

        DaoProtectionResult result = cultivationService.establishProtection(userId, "不存在的道友");

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("未找到"));
    }

    @Test
    @DisplayName("establishProtection — 护道者境界低于被护道者返回失败")
    void establishProtection_whenProtectorLevelTooLow_shouldReturnFailure() {
        User protector = createUser(2, 0L, 0);
        User protege = User.create().setId(2L).setNickname("高境界道友").setLevel(10).setLocationId(1L);

        when(userStateService.getUser(userId)).thenReturn(protector);
        when(userRepository.findByNickname("高境界道友")).thenReturn(Optional.of(protege));

        DaoProtectionResult result = cultivationService.establishProtection(userId, "高境界道友");

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("境界"));
    }

    @Test
    @DisplayName("establishProtection — 已达护道上限返回失败")
    void establishProtection_whenAtLimit_shouldReturnFailure() {
        User protector = createUser(10, 0L, 0);
        User protege = User.create().setId(2L).setNickname("弟子").setLevel(5).setLocationId(1L);

        when(userStateService.getUser(userId)).thenReturn(protector);
        when(userRepository.findByNickname("弟子")).thenReturn(Optional.of(protege));
        when(daoProtectionRepository.countByProtectorId(userId)).thenReturn(3L);

        DaoProtectionResult result = cultivationService.establishProtection(userId, "弟子");

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("分身乏术"));
    }

    @Test
    @DisplayName("establishProtection — 已存在护道关系返回失败")
    void establishProtection_whenAlreadyProtecting_shouldReturnFailure() {
        User protector = createUser(10, 0L, 0);
        User protege = User.create().setId(2L).setNickname("弟子").setLevel(5).setLocationId(1L);

        when(userStateService.getUser(userId)).thenReturn(protector);
        when(userRepository.findByNickname("弟子")).thenReturn(Optional.of(protege));
        when(daoProtectionRepository.countByProtectorId(userId)).thenReturn(0L);
        when(daoProtectionRepository.findByProtectorAndProtege(userId, 2L))
                .thenReturn(Optional.of(DaoProtection.create()
                        .setProtectorId(userId)
                        .setProtegeId(2L)));

        DaoProtectionResult result = cultivationService.establishProtection(userId, "弟子");

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("已在"));
    }

    @Test
    @DisplayName("establishProtection — 成功建立护道关系")
    void establishProtection_whenValid_shouldCreateProtection() {
        User protector = User.create().setId(userId).setNickname("护道者").setLevel(10).setLocationId(1L);
        User protege = User.create().setId(2L).setNickname("弟子").setLevel(5).setLocationId(1L);

        when(userStateService.getUser(userId)).thenReturn(protector);
        when(userRepository.findByNickname("弟子")).thenReturn(Optional.of(protege));
        when(daoProtectionRepository.countByProtectorId(userId)).thenReturn(0L);
        when(daoProtectionRepository.findByProtectorAndProtege(userId, 2L)).thenReturn(Optional.empty());

        DaoProtectionResult result = cultivationService.establishProtection(userId, "弟子");

        assertTrue(result.isSuccess());
        assertTrue(result.getMessage().contains("护道契约"));
        verify(daoProtectionRepository).save(any(DaoProtection.class));
    }

    // ===================== removeProtection =====================

    @Test
    @DisplayName("removeProtection — 护道关系不存在返回失败")
    void removeProtection_whenNotProtecting_shouldReturnFailure() {
        when(userRepository.findByNickname("弟子")).thenReturn(Optional.of(
                User.create().setId(2L).setNickname("弟子")));
        when(daoProtectionRepository.findByProtectorAndProtege(userId, 2L)).thenReturn(Optional.empty());

        DaoProtectionResult result = cultivationService.removeProtection(userId, "弟子");

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("并未"));
    }

    // ===================== queryProtectionInfo =====================

    @Test
    @DisplayName("queryProtectionInfo — 无人护道时返回空结果")
    void queryProtectionInfo_whenNoProtections_shouldReturnEmptyInfo() {
        User user = createUser(5, 0L, 0);
        when(userStateService.getUser(userId)).thenReturn(user);
        when(daoProtectionRepository.findByProtectorId(userId)).thenReturn(List.of());
        when(daoProtectionRepository.findByProtegeId(userId)).thenReturn(List.of());

        DaoProtectionQueryResult result = cultivationService.queryProtectionInfo(userId);

        assertTrue(result.isSuccess());
        assertTrue(result.getMessage().contains("无道友相护"));
        assertEquals(0.0, result.getTotalBonusPercentage());
    }

    @Test
    @DisplayName("queryProtectionInfo — 同地点护道者提供加成")
    void queryProtectionInfo_withSameLocationProtectors_shouldCalculateBonus() {
        User user = createUser(5, 0L, 0);
        User protector = User.create().setId(10L).setNickname("大佬").setLevel(10).setLocationId(1L);

        DaoProtection protection = DaoProtection.create()
                .setProtectorId(10L)
                .setProtegeId(userId);

        when(userStateService.getUser(userId)).thenReturn(user);
        when(daoProtectionRepository.findByProtectorId(userId)).thenReturn(List.of());
        when(daoProtectionRepository.findByProtegeId(userId)).thenReturn(List.of(protection));
        when(userRepository.findByIds(List.of(10L))).thenReturn(List.of(protector));
        when(mapService.getMapName(1L)).thenReturn("测试地图");

        DaoProtectionQueryResult result = cultivationService.queryProtectionInfo(userId);

        assertTrue(result.isSuccess());
        assertEquals(1, result.getProtectedByList().size());
    }

    @Test
    @DisplayName("queryProtectionInfo — 非同一地点护道者不提供加成")
    void queryProtectionInfo_whenDifferentLocation_shouldNotAddBonus() {
        User user = createUser(5, 0L, 0);
        User protector = User.create().setId(10L).setNickname("大佬").setLevel(10).setLocationId(99L);

        DaoProtection protection = DaoProtection.create()
                .setProtectorId(10L)
                .setProtegeId(userId);

        when(userStateService.getUser(userId)).thenReturn(user);
        when(daoProtectionRepository.findByProtectorId(userId)).thenReturn(List.of());
        when(daoProtectionRepository.findByProtegeId(userId)).thenReturn(List.of(protection));
        when(userRepository.findByIds(List.of(10L))).thenReturn(List.of(protector));
        when(mapService.getMapName(99L)).thenReturn("远方");

        DaoProtectionQueryResult result = cultivationService.queryProtectionInfo(userId);

        assertEquals(0.0, result.getTotalBonusPercentage());
        assertTrue(result.getMessage().contains("不在同地点"));
    }
}
