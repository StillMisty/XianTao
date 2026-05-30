package top.stillmisty.xiantao.domain.monster.vo;

import java.util.List;
import org.jspecify.annotations.Nullable;

public record MonsterDetailVO(
    long id,
    String name,
    @Nullable String description,
    String typeName,
    int baseLevel,
    int baseHp,
    int baseAttack,
    int baseDefense,
    int baseSpeed,
    int expReward,
    List<String> tags) {}
