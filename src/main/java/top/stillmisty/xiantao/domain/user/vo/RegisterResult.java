package top.stillmisty.xiantao.domain.user.vo;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

/**
 * 注册结果 VO
 */
@Data
@Builder
public class RegisterResult {
    
    private boolean success;
    private String message;
    private UUID userId;
    private String nickname;
}
