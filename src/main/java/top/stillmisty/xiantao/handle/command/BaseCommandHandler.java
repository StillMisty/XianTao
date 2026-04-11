package top.stillmisty.xiantao.handle.command;

import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.domain.user.enums.UserStatus;
import top.stillmisty.xiantao.service.UserAuthService;
import top.stillmisty.xiantao.service.UserService;

/**
 * 命令处理器基类
 * 提供通用的用户认证和状态检查逻辑
 */
@RequiredArgsConstructor
public abstract class BaseCommandHandler {

    protected final UserAuthService userAuthService;
    protected final UserService userService;

    /**
     * 认证结果封装
     */
    protected record AuthResult(boolean authenticated, Long userId, String errorMessage) {
        public static AuthResult success(Long userId) {
            return new AuthResult(true, userId, null);
        }

        public static AuthResult failure(String message) {
            return new AuthResult(false, null, message);
        }
    }

    /**
     * 验证用户身份并返回用户ID
     *
     * @param platform 平台类型
     * @param openId 平台用户ID
     * @return 认证结果
     */
    protected AuthResult authenticate(PlatformType platform, String openId) {
        var userAuth = userAuthService.findUserIdByOpenId(platform, openId);

        return userAuth.map(auth -> AuthResult.success(auth.getUserId())).orElseGet(() -> AuthResult.failure("输入「我要修仙 [道号]」进入仙途吧！"));

    }

    /**
     * 验证用户状态
     *
     * @param userId 用户ID
     * @param requiredStatus 期望的用户状态
     * @return 验证结果，如果成功返回null，失败返回错误消息
     */
    protected String validateUserStatus(Long userId, UserStatus requiredStatus) {
        // 获取用户状态
        var user = userService.findById(userId);
        if (user.isEmpty()) {
            return "用户不存在";
        }

        if (user.get().getStatus() != requiredStatus) {
            String currentStatusName = user.get().getStatus() != null ? user.get().getStatus().getName() : "未知";
            String requiredStatusName = requiredStatus.getName();
            return String.format("您当前处于 %s 状态，无法进行此操作（需要 %s 状态）", currentStatusName, requiredStatusName);
        }

        return null; // 验证通过
    }

    /**
     * 认证并验证用户状态为指定状态
     *
     * @param platform 平台类型
     * @param openId 平台用户ID
     * @param requiredStatus 期望的用户状态
     * @return 认证结果，包含用户ID或错误消息
     */
    protected AuthResult authenticateAndValidateStatus(PlatformType platform, String openId, UserStatus requiredStatus) {
        // 验证用户身份
        var authResult = authenticate(platform, openId);
        if (!authResult.authenticated()) {
            return authResult;
        }

        // 验证用户状态
        String validationError = validateUserStatus(authResult.userId(), requiredStatus);
        if (validationError != null) {
            return AuthResult.failure(validationError);
        }

        return authResult;
    }
}
