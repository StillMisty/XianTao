package top.stillmisty.xiantao.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.domain.user.entity.UserAuth;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.domain.user.enums.UserStatus;
import top.stillmisty.xiantao.domain.user.repository.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@DisplayName("AuthenticationService 测试")
@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserAuthService userAuthService;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthenticationService authenticationService;

    private final PlatformType platform = PlatformType.QQ;
    private final String openId = "test-open-id";
    private final Long userId = 1L;

    @BeforeEach
    void setUp() {
        // default stubs can be overridden per test
    }

    // ===================== 单参数 authenticate =====================

    @Test
    @DisplayName("authenticate(platform, openId) — 用户存在返回 Success")
    void authenticate_withoutStatus_whenUserExists_shouldReturnSuccess() {
        UserAuth userAuth = new UserAuth();
        userAuth.setUserId(userId);
        when(userAuthService.findUserIdByOpenId(platform, openId)).thenReturn(Optional.of(userAuth));

        ServiceResult<Long> result = authenticationService.authenticate(platform, openId);

        assertInstanceOf(ServiceResult.Success.class, result);
        assertEquals(userId, ((ServiceResult.Success<Long>) result).data());
    }

    @Test
    @DisplayName("authenticate(platform, openId) — 用户不存在返回 AUTH_FAILED")
    void authenticate_withoutStatus_whenUserNotExists_shouldReturnAuthFailure() {
        when(userAuthService.findUserIdByOpenId(platform, openId)).thenReturn(Optional.empty());

        ServiceResult<Long> result = authenticationService.authenticate(platform, openId);

        assertInstanceOf(ServiceResult.Failure.class, result);
        assertEquals("AUTH_FAILED", ((ServiceResult.Failure<?>) result).errorCode());
        assertTrue(((ServiceResult.Failure<?>) result).errorMessage().contains("修仙"));
    }

    // ===================== 三参数 authenticate =====================

    @Test
    @DisplayName("authenticate + 状态校验 — 用户存在且状态匹配返回 Success")
    void authenticate_withStatus_whenUserAndStatusMatch_shouldReturnSuccess() {
        UserAuth userAuth = new UserAuth();
        userAuth.setUserId(userId);
        when(userAuthService.findUserIdByOpenId(platform, openId)).thenReturn(Optional.of(userAuth));

        User user = User.create().setId(userId).setStatus(UserStatus.IDLE);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        ServiceResult<Long> result = authenticationService.authenticate(platform, openId, UserStatus.IDLE);

        assertInstanceOf(ServiceResult.Success.class, result);
    }

    @Test
    @DisplayName("authenticate + 状态校验 — User 实体不存在返回 AUTH_FAILED")
    void authenticate_withStatus_whenUserEntityNotFound_shouldReturnAuthFailure() {
        UserAuth userAuth = new UserAuth();
        userAuth.setUserId(userId);
        when(userAuthService.findUserIdByOpenId(platform, openId)).thenReturn(Optional.of(userAuth));
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        ServiceResult<Long> result = authenticationService.authenticate(platform, openId, UserStatus.IDLE);

        assertInstanceOf(ServiceResult.Failure.class, result);
        assertEquals("AUTH_FAILED", ((ServiceResult.Failure<?>) result).errorCode());
    }

    @Test
    @DisplayName("authenticate + 状态校验 — 状态不匹配返回 AUTH_FAILED")
    void authenticate_withStatus_whenStatusMismatch_shouldReturnAuthFailure() {
        UserAuth userAuth = new UserAuth();
        userAuth.setUserId(userId);
        when(userAuthService.findUserIdByOpenId(platform, openId)).thenReturn(Optional.of(userAuth));

        User user = User.create().setId(userId).setStatus(UserStatus.EXERCISING);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        ServiceResult<Long> result = authenticationService.authenticate(platform, openId, UserStatus.IDLE);

        assertInstanceOf(ServiceResult.Failure.class, result);
        assertEquals("AUTH_FAILED", ((ServiceResult.Failure<?>) result).errorCode());
        assertTrue(((ServiceResult.Failure<?>) result).errorMessage().contains("状态"));
    }

    @Test
    @DisplayName("authenticate + 状态校验 — requiredStatus=null 不校验状态")
    void authenticate_withNullStatus_shouldSkipStatusCheck() {
        UserAuth userAuth = new UserAuth();
        userAuth.setUserId(userId);
        when(userAuthService.findUserIdByOpenId(platform, openId)).thenReturn(Optional.of(userAuth));

        User user = User.create().setId(userId).setStatus(UserStatus.DYING);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        ServiceResult<Long> result = authenticationService.authenticate(platform, openId, null);

        assertInstanceOf(ServiceResult.Success.class, result);
    }

    @Test
    @DisplayName("authenticate — 平台认证失败时，三参数版本不查 User")
    void authenticate_withStatus_whenAuthFails_shouldNotQueryUser() {
        when(userAuthService.findUserIdByOpenId(platform, openId)).thenReturn(Optional.empty());

        ServiceResult<Long> result = authenticationService.authenticate(platform, openId, UserStatus.IDLE);

        assertInstanceOf(ServiceResult.Failure.class, result);
        assertEquals("AUTH_FAILED", ((ServiceResult.Failure<?>) result).errorCode());
    }
}
