package top.stillmisty.xiantao.domain.land.vo;

import lombok.Builder;
import lombok.Data;

/**
 * 灵兽产出物值对象
 */
@Data
@Builder
public class BeastProduceVO {
    private String position;
    private String beastName;
    private int totalProduced;
    private int itemTemplateId;
    private String itemName;
}
