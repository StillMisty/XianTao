package top.stillmisty.xiantao.domain.user.vo;

import lombok.Builder;
import lombok.Data;

/**
 * 打坐开始结果 VO
 */
@Data
@Builder
public class MeditationStartResult {
    
    private boolean success;
    private String message;
    private UserStatusVO userStatus;
}
