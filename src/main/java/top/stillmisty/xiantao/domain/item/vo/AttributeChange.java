package top.stillmisty.xiantao.domain.item.vo;

/** 属性变化 VO */
public record AttributeChange(
    int strChange,
    int conChange,
    int agiChange,
    int wisChange,
    int attackChange,
    int defenseChange,
    int maxHpChange) {}
