package top.stillmisty.xiantao.service.aspect;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.domain.user.enums.UserStatus;
import top.stillmisty.xiantao.service.AuthenticationService;
import top.stillmisty.xiantao.service.ServiceResult;
import top.stillmisty.xiantao.service.UserContext;

/**
 * 统一认证切面 拦截 Service 中所有返回 ServiceResult 且首两个参数为 (PlatformType, String openId) 的 public 方法，
 * 自动完成身份认证、状态校验，并将 userId 注入到 UserContext。
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuthenticatedAspect {

  private final AuthenticationService authService;

  /**
   * 匹配 Service 包下所有返回 ServiceResult、前两个参数为 (PlatformType, String, [可选 UserStatus], ...) 的方法 已移除
   * authService.authenticateAndValidateUser 和 authenticateAndValidateStatus 现在
   * authenticate(platform, openId, requiredStatus) 支持 null requiredStatus 表示不校验状态
   */
  @Around("@annotation(top.stillmisty.xiantao.service.annotation.Authenticated)")
  public Object authenticate(ProceedingJoinPoint pjp) throws Throwable {
    Object[] args = pjp.getArgs();
    if (args.length < 2
        || !(args[0] instanceof PlatformType platform)
        || !(args[1] instanceof String openId)) {
      log.warn(
          "AuthenticatedAspect: first two args are not (PlatformType, String), skipping auth for {}",
          pjp.getSignature());
      return pjp.proceed();
    }

    // 提取可选的 UserStatus（第3个参数）
    UserStatus requiredStatus = null;
    if (args.length >= 3 && args[2] instanceof UserStatus) {
      requiredStatus = (UserStatus) args[2];
    }

    ServiceResult<Long> auth = authService.authenticate(platform, openId, requiredStatus);
    if (auth instanceof ServiceResult.Failure<Long> f) {
      return new ServiceResult.Failure<>(f.errorCode(), f.errorMessage());
    }

    Long userId = ((ServiceResult.Success<Long>) auth).data();

    try {
      return ScopedValue.where(UserContext.CURRENT_USER, userId)
          .call(
              () -> {
                try {
                  return pjp.proceed();
                } catch (RuntimeException e) {
                  throw e;
                } catch (Exception e) {
                  throw new RuntimeException(e);
                }
              });
    } catch (RuntimeException e) {
      log.error("Service exception for userId={}: {}", userId, e.getMessage(), e);
      String message =
          e instanceof IllegalStateException || e instanceof IllegalArgumentException
              ? e.getMessage()
              : "系统繁忙，请稍后再试";
      return ServiceResult.businessFailure(message);
    }
  }
}
