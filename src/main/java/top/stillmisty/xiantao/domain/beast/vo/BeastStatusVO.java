package top.stillmisty.xiantao.domain.beast.vo;

import java.util.List;
import lombok.Builder;
import org.jspecify.annotations.Nullable;

@Builder
public record BeastStatusVO(
    Long id,
    @Nullable String beastName,
    String quality,
    String gender,
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
    boolean needsRecovery,
    boolean breedCooldown,
    int pennedCellId) {}
