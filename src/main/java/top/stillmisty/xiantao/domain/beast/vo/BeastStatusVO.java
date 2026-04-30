package top.stillmisty.xiantao.domain.beast.vo;

import lombok.Builder;

import java.util.List;

@Builder
public record BeastStatusVO(
        Long id,
        String beastName,
        String quality,
        boolean isMutant,
        List<String> mutationTraits,
        int tier,
        int level,
        int exp,
        int attack,
        int defense,
        int maxHp,
        int hpCurrent,
        List<Long> skills,
        boolean isDeployed,
        boolean inRecovery,
        String recoveryEndTime,
        double lifespanDays,
        int pennedCellId
) {}
