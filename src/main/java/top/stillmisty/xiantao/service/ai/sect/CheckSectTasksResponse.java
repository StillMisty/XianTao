package top.stillmisty.xiantao.service.ai.sect;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.util.List;
import top.stillmisty.xiantao.domain.sect.vo.SectTaskVO;

/**
 * 宗门任务查询结果。
 *
 * <p>返回当前宗门事件对应的任务列表（宗门无事件时为空列表）。 每个任务含：任务名称、需求描述、奖励描述、当前进度。
 */
public record CheckSectTasksResponse(
    @JsonPropertyDescription("宗门任务列表，无事件时为空") List<SectTaskVO> tasks) {}
