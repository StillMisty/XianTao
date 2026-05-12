package top.stillmisty.xiantao.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.domain.user.enums.UserStatus;

@DisplayName("TrainingService 测试")
@ExtendWith(MockitoExtension.class)
class TrainingServiceTest {

  @Mock private UserStateService userStateService;

  @InjectMocks private TrainingService trainingService;

  private final Long userId = 1L;

  private User createUser(UserStatus status, LocalDateTime activityStartTime) {
    return User.create()
        .setId(userId)
        .setStatus(status)
        .setLocationId(10L)
        .setActivityStartTime(activityStartTime)
        .setHpCurrent(100)
        .setLevel(1);
  }

  @Test
  @DisplayName("endTraining — activityStartTime 为空时恢复 IDLE")
  void endTraining_whenNoTrainingStartTime_shouldRestoreIdle() {
    User user = createUser(UserStatus.TRAINING, null);
    when(userStateService.loadUser(userId)).thenReturn(user);

    trainingService.endTraining(userId);

    assertEquals(UserStatus.IDLE, user.getStatus());
    verify(userStateService).save(user);
  }

  @Test
  @DisplayName("endTraining — 时间不足 5 分钟时恢复 IDLE")
  void endTraining_whenLessThan5Minutes_shouldRestoreIdle() {
    User user = createUser(UserStatus.TRAINING, LocalDateTime.now());
    when(userStateService.loadUser(userId)).thenReturn(user);

    trainingService.endTraining(userId);

    assertEquals(UserStatus.IDLE, user.getStatus());
    assertNull(user.getActivityStartTime());
    verify(userStateService).save(user);
  }

  @Test
  @DisplayName("endTraining — 非 TRAINING 状态抛异常")
  void endTraining_whenNotTraining_shouldThrow() {
    User user = createUser(UserStatus.IDLE, LocalDateTime.now().minusMinutes(10));
    when(userStateService.loadUser(userId)).thenReturn(user);

    assertThrows(BusinessException.class, () -> trainingService.endTraining(userId));
  }

  @Test
  @DisplayName("startTraining — 非 IDLE 状态抛异常")
  void startTraining_whenNotIdle_shouldThrow() {
    User user = createUser(UserStatus.TRAINING, null);
    when(userStateService.loadUser(userId)).thenReturn(user);

    assertThrows(BusinessException.class, () -> trainingService.startTraining(userId));
  }

  @Test
  @DisplayName("startTraining — 位置为空返回失败")
  void startTraining_whenNoLocation_shouldReturnFailure() {
    User user = createUser(UserStatus.IDLE, null).setLocationId(null);
    when(userStateService.loadUser(userId)).thenReturn(user);

    var result = trainingService.startTraining(userId);

    assertFalse(result.isSuccess());
    assertEquals("当前位置无效，无法开始历练", result.getMessage());
  }
}
