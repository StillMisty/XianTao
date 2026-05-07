package top.stillmisty.xiantao.domain.item.entity;

/** 丹方元素需求，从 Scroll 提取以降低嵌套深度 */
public record ElementRequirement(String element, int min, int max) {}
