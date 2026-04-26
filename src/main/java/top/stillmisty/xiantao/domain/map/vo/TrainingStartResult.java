package top.stillmisty.xiantao.domain.map.vo;

import lombok.Builder;
import lombok.Data;

/**
 * 历练开始结果 VO
 */
@Data
@Builder
public class TrainingStartResult {
    private boolean success;
    private String message;
    private String mapName;
}
