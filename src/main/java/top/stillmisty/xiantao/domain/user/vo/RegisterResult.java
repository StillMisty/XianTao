package top.stillmisty.xiantao.domain.user.vo;

import lombok.Builder;
import lombok.Data;

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
