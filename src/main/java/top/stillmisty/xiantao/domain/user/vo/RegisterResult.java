package top.stillmisty.xiantao.domain.user.vo;

/**
 * 注册结果 VO
 */

public record RegisterResult(
        boolean success,
        String message,
        Long userId,
        String nickname
) {
}
