package top.stillmisty.xiantao.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.domain.user.enums.UserStatus;
import top.stillmisty.xiantao.domain.user.repository.UserRepository;

/**
 * 统一身份认证服务
 * 负责平台身份凭证解析、用户存在性验证、状态校验
 * 统一返回 ServiceResult&lt;Long&gt; 替代原来的 AuthResult
 */
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserAuthService userAuthService;
    private final UserRepository userRepository;

    /**
     * 仅验证平台绑定（不检查 User 实体是否存在）
     *
     * @return 成功时 ServiceResult.Success 携带 userId
     */
    public ServiceResult<Long> authenticate(PlatformType platform, String openId) {
        var userAuth = userAuthService.findUserIdByOpenId(platform, openId);
        return userAuth.<ServiceResult<Long>>map(auth -> new ServiceResult.Success<>(auth.getUserId()))
                .orElseGet(() -> ServiceResult.authFailure("输入「我要修仙 [道号]」进入仙途吧！"));
    }

    /**
     * 验证平台绑定 + User 实体存在 + 状态校验
     *
     * @param requiredStatus 要求的状态，null 表示不校验状态
     * @return 成功时 ServiceResult.Success 携带 userId
     */
    public ServiceResult<Long> authenticate(PlatformType platform, String openId, UserStatus requiredStatus) {
        ServiceResult<Long> authResult = authenticate(platform, openId);
        if (authResult instanceof ServiceResult.Failure) {
            return authResult;
        }
        Long userId = ((ServiceResult.Success<Long>) authResult).data();

        var user = userRepository.findById(userId);
        if (user.isEmpty()) {
            return ServiceResult.authFailure("用户不存在");
        }
        if (requiredStatus != null && user.get().getStatus() != requiredStatus) {
            String current = user.get().getStatus().getName();
            return ServiceResult.authFailure(
                    String.format("您当前处于 %s 状态，无法进行此操作（需要 %s 状态）", current, requiredStatus.getName()));
        }
        return authResult;
    }
}
