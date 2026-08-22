package top.stillmisty.xiantao.domain.user.vo;

import java.util.List;

/** 大境界雷劫预报 当修为足以尝试跨大境界/渡劫期突破时，随角色状态展示的情报面板。 仅展示候选天劫与可用削助手段，不透露具体概率数值——天数难测。 */
public record TribulationForecast(
    String targetRealmName,
    List<String> tribulationNames,
    double pillBonusPercent,
    double protectionBonusPercent,
    double resistPercent,
    int failCount) {}
