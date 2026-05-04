package top.stillmisty.xiantao.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.domain.user.enums.UserStatus;
import top.stillmisty.xiantao.domain.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("TrainingService 测试")
@ExtendWith(MockitoExtension.class)
class TrainingServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TrainingService trainingService;

    private final Long userId = 1L;

    private User createUser(UserStatus status, LocalDateTime trainingStartTime) {
        return User.create()
            .setId(userId)
            .setStatus(status)
            .setLocationId(10L)
            .setTrainingStartTime(trainingStartTime)
            .setHpCurrent(100)
            .setLevel(1);
    }

    // ===================== endTraining 状态恢复 =====================

    @Test
    @DisplayName("endTraining — trainingStartTime 为空时恢复 IDLE")
    void endTraining_whenNoTrainingStartTime_shouldRestoreIdle() {
        User user = createUser(UserStatus.EXERCISING, null);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        trainingService.endTraining(userId);

        assertEquals(UserStatus.IDLE, user.getStatus());
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("endTraining — 时间不足 5 分钟时恢复 IDLE")
    void endTraining_whenLessThan5Minutes_shouldRestoreIdle() {
        User user = createUser(UserStatus.EXERCISING, LocalDateTime.now());
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        trainingService.endTraining(userId);

        assertEquals(UserStatus.IDLE, user.getStatus());
        assertNull(user.getTrainingStartTime());
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("endTraining — 非 EXERCISING 状态抛异常")
    void endTraining_whenNotExercising_shouldThrow() {
        User user = createUser(UserStatus.IDLE, LocalDateTime.now().minusMinutes(10));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThrows(IllegalStateException.class, () -> trainingService.endTraining(userId));
    }

    // ===================== startTraining 状态检查 =====================

    @Test
    @DisplayName("startTraining — 非 IDLE 状态抛异常")
    void startTraining_whenNotIdle_shouldThrow() {
        User user = createUser(UserStatus.EXERCISING, null);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThrows(IllegalStateException.class, () -> trainingService.startTraining(userId));
    }

    @Test
    @DisplayName("startTraining — 位置为空返回失败")
    void startTraining_whenNoLocation_shouldReturnFailure() {
        User user = createUser(UserStatus.IDLE, null).setLocationId(null);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        var result = trainingService.startTraining(userId);

        assertFalse(result.isSuccess());
        assertEquals("当前位置无效，无法开始历练", result.getMessage());
    }
}
