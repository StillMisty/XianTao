package top.stillmisty.xiantao.domain.user.vo;

import java.util.List;

public record PlayerViewVO(
    String nickname,
    int level,
    int hpCurrent,
    int hpMax,
    int attack,
    int defense,
    int statStr,
    int statCon,
    int statAgi,
    int statWis,
    String locationName,
    String statusName,
    List<String> equippedItems) {}
