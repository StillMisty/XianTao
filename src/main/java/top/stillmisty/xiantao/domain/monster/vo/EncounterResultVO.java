package top.stillmisty.xiantao.domain.monster.vo;

import java.util.List;
import java.util.Map;
import lombok.Builder;

@Builder
public record EncounterResultVO(
    boolean encountered,
    String monsterName,
    int monsterCount,
    int monsterLevel,
    List<Map<String, Object>> encounterDetails) {}
