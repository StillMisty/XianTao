package top.stillmisty.xiantao.domain.monster.vo;

import java.util.List;

public record MonsterDetailVO(
    long id,
    String name,
    String description,
    String typeName,
    int baseLevel,
    int baseHp,
    int baseAttack,
    int baseDefense,
    int baseSpeed,
    int expReward,
    List<String> tags) {}
