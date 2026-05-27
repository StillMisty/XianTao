package top.stillmisty.xiantao.service.ai.spirit;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.util.List;

public record BreedBeastsResponse(
    @JsonPropertyDescription("父方名称") String parent1Name,
    @JsonPropertyDescription("父方性别") String parent1Gender,
    @JsonPropertyDescription("母方名称") String parent2Name,
    @JsonPropertyDescription("母方性别") String parent2Gender,
    @JsonPropertyDescription("后代兽卵名称") String offspringEggName,
    @JsonPropertyDescription("后代品质") String offspringQuality,
    @JsonPropertyDescription("继承的变异词条") List<String> inheritedTraits,
    @JsonPropertyDescription("繁育冷却小时数") long cooldownHours) {}
