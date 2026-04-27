package top.stillmisty.xiantao.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.domain.user.enums.UserStatus;
import top.stillmisty.xiantao.domain.user.repository.UserRepository;

/**
 * 统一身份认证服务
 * 负责平台身份凭证解析、用户存在性验证、状态校验
 */
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserAuthService userAuthService;
    private final UserRepository userRepository;

    /**
     * 仅验证平台绑定（不检查 User 实体是否存在）
     */
    public AuthResult authenticate(PlatformType platform, String openId) {
        var userAuth = userAuthService.findUserIdByOpenId(platform, openId);
        return userAuth.map(auth -> AuthResult.success(auth.getUserId()))
                .orElseGet(() -> AuthResult.failure("输入「我要修仙 [道号]」进入仙途吧！"));
    }

    /**
     * 验证平台绑定 + User 实体存在
     */
    public AuthResult authenticateAndValidateUser(PlatformType platform, String openId) {
        var authResult = authenticate(platform, openId);
        if (!authResult.authenticated()) {
            return authResult;
        }
        var user = userRepository.findById(authResult.userId());
        if (user.isEmpty()) {
            return AuthResult.failure("用户不存在");
        }
        return authResult;
    }

    /**
     * 验证平台绑定 + User 状态
     */
    public AuthResult authenticateAndValidateStatus(PlatformType platform, String openId, UserStatus requiredStatus) {
        var authResult = authenticate(platform, openId);
        if (!authResult.authenticated()) {
            return authResult;
        }
        var user = userRepository.findById(authResult.userId());
        if (user.isEmpty()) {
            return AuthResult.failure("用户不存在");
        }
        if (user.get().getStatus() != requiredStatus) {
            String current = user.get().getStatus() != null ? user.get().getStatus().getName() : "未知";
            return AuthResult.failure(
                    String.format("您当前处于 %s 状态，无法进行此操作（需要 %s 状态）", current, requiredStatus.getName()));
        }
        return authResult;
    }

    /**
     * 认证结果
     */
    public record AuthResult(boolean authenticated, Long userId, String errorMessage) {
        public static AuthResult success(Long userId) {
            return new AuthResult(true, userId, null);
        }

        public static AuthResult failure(String message) {
            return new AuthResult(false, null, message);
        }
    }
}
