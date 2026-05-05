package top.stillmisty.xiantao.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import top.stillmisty.xiantao.domain.fudi.enums.MBTIPersonality;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.domain.user.entity.UserAuth;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.domain.user.repository.UserAuthRepository;
import top.stillmisty.xiantao.domain.user.repository.UserRepository;
import top.stillmisty.xiantao.domain.user.vo.RegisterResult;

@DisplayName("UserService 测试")
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @Mock private UserRepository userRepository;
  @Mock private UserAuthRepository userAuthRepository;
  @Mock private FudiService fudiService;

  @InjectMocks private UserService userService;

  private final PlatformType platform = PlatformType.QQ;
  private final String openId = "qq-user-123";
  private final String nickname = "清虚道人";

  // ===================== createUser =====================

  @Test
  @DisplayName("createUser — 已注册用户返回失败")
  void createUser_whenAlreadyRegistered_shouldReturnFailure() {
    UserAuth existingAuth = new UserAuth();
    when(userAuthRepository.findByPlatformAndOpenId(platform, openId))
        .thenReturn(Optional.of(existingAuth));

    RegisterResult result = userService.createUser(platform, openId, nickname);

    assertFalse(result.success());
    assertTrue(result.message().contains("已在仙路"));
  }

  @Test
  @DisplayName("createUser — 道号已被占用返回失败")
  void createUser_whenNicknameTaken_shouldReturnFailure() {
    when(userAuthRepository.findByPlatformAndOpenId(platform, openId)).thenReturn(Optional.empty());
    when(userRepository.existsByNickname(nickname)).thenReturn(true);

    RegisterResult result = userService.createUser(platform, openId, nickname);

    assertFalse(result.success());
    assertTrue(result.message().contains("已被他人使用"));
  }

  @Test
  @DisplayName("createUser — 成功注册返回用户信息")
  void createUser_shouldCreateUserAndFudi() {
    when(userAuthRepository.findByPlatformAndOpenId(platform, openId)).thenReturn(Optional.empty());
    when(userRepository.existsByNickname(nickname)).thenReturn(false);

    User savedUser = User.create().setId(1L).setNickname(nickname);
    when(userRepository.save(any(User.class))).thenReturn(savedUser);

    when(userAuthRepository.save(any(UserAuth.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    RegisterResult result = userService.createUser(platform, openId, nickname);

    assertTrue(result.success());
    assertEquals(1L, result.userId());
    assertEquals(nickname, result.nickname());

    verify(userRepository).save(any(User.class));
    verify(userAuthRepository).save(any(UserAuth.class));
    verify(fudiService).createFudi(eq(1L), any(MBTIPersonality.class));
  }

  @Test
  @DisplayName("createUser — 注册自动创建授权关系")
  void createUser_shouldCreateUserAuth() {
    when(userAuthRepository.findByPlatformAndOpenId(platform, openId)).thenReturn(Optional.empty());
    when(userRepository.existsByNickname(nickname)).thenReturn(false);

    User savedUser = User.create().setId(1L).setNickname(nickname);
    when(userRepository.save(any(User.class))).thenReturn(savedUser);
    when(userAuthRepository.save(any(UserAuth.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    userService.createUser(platform, openId, nickname);

    verify(userAuthRepository)
        .save(
            argThat(
                auth ->
                    auth.getPlatform() == platform
                        && openId.equals(auth.getPlatformOpenId())
                        && auth.getUserId().equals(1L)));
  }
}
