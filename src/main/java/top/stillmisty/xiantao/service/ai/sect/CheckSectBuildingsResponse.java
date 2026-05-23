package top.stillmisty.xiantao.service.ai.sect;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.util.List;
import top.stillmisty.xiantao.domain.sect.vo.BuildingsQueryVO.BuildingEntry;

/**
 * 宗门建筑查询结果。
 *
 * <p>返回宗门所有建筑的当前状态。{@code built} 是已建成的建筑及其等级， {@code buildable} 是可建造但未建立的建筑模板（含建造成本）。
 */
public record CheckSectBuildingsResponse(
    @JsonPropertyDescription("已建成建筑列表，每项含建筑代码、名称、等级、效果描述") List<BuildingEntry> built,
    @JsonPropertyDescription("可建造建筑列表，每项含建筑代码、名称、建造成本（灵石）、效果描述") List<BuildingEntry> buildable) {}
