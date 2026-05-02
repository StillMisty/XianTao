package top.stillmisty.xiantao.domain.item.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.item.entity.ItemTemplate;
import top.stillmisty.xiantao.domain.item.entity.StackableItem;
import top.stillmisty.xiantao.domain.item.enums.ItemType;
import top.stillmisty.xiantao.domain.land.vo.PenCellVO;
import top.stillmisty.xiantao.service.FudiService;

/**
 * 进化石使用处理器
 */
@Component
@RequiredArgsConstructor
public class EvolutionStoneUseHandler implements ItemUseHandler {

    private final FudiService fudiService;

    @Override
    public boolean supports(ItemType type, ItemTemplate template) {
        return type == ItemType.EVOLUTION_STONE;
    }

    @Override
    public String use(Long userId, StackableItem item, ItemTemplate template, String args) {
        if (args == null || args.isBlank()) {
            return "用法：使用 进化石 [灵兽位置]\n示例：使用 进化石 1";
        }

        // 解析位置参数
        String position = args.trim();

        try {
            PenCellVO vo = fudiService.evolveBeast(userId, position, "进化");
            return formatEvolveResult(vo);
        } catch (IllegalStateException e) {
            return e.getMessage();
        }
    }

    private String formatEvolveResult(PenCellVO vo) {
        var sb = new StringBuilder();
        sb.append("进化成功！\n");
        sb.append("灵兽：").append(vo.getBeastName()).append("\n");
        sb.append("等阶：T").append(vo.getTier());
        if (vo.getQuality() != null) {
            sb.append(" | 品质：").append(vo.getQuality());
        }
        return sb.toString();
    }

    @Override
    public boolean consumesInternally() {
        return true;
    }
}
