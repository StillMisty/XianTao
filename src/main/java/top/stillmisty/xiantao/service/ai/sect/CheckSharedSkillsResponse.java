package top.stillmisty.xiantao.service.ai.sect;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.util.List;
import top.stillmisty.xiantao.domain.sect.vo.SectSharedSkillVO;

/**
 * 宗门共享功法库查询结果。
 *
 * <p>返回当前弟子可学习的共享功法列表。{@code usedSlots}/{@code maxSlots} 表示已用/最大功法槽位。 若槽位已满，需先遗忘旧功法才能学习新的。{@code
 * pendingCount} 表示已提交但未上架的功法数。
 */
public record CheckSharedSkillsResponse(
    @JsonPropertyDescription("当前贡献值") int myContribution,
    @JsonPropertyDescription("已使用的功法槽位") int usedSlots,
    @JsonPropertyDescription("最大可用功法槽位") int maxSlots,
    @JsonPropertyDescription("可学习的共享功法列表，每项含编号(sharedSkillId)、名称、消耗贡献值、功法效果描述")
        List<SectSharedSkillVO> skills,
    @JsonPropertyDescription("已提交玉简但待长老上架的功法数量") int pendingCount) {}
