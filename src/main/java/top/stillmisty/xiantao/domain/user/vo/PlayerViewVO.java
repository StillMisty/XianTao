package top.stillmisty.xiantao.domain.user.vo;

import java.util.List;
import org.jspecify.annotations.Nullable;

public record PlayerViewVO(
    String nickname,
    int level,
    String realmDisplay,
    int hpCurrent,
    int hpMax,
    int attack,
    int defense,
    int statStr,
    int statCon,
    int statAgi,
    int statWis,
    @Nullable String locationName,
    String statusName,
    List<String> equippedItems) {}
