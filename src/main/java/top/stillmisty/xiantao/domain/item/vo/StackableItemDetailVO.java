package top.stillmisty.xiantao.domain.item.vo;

import java.util.Map;
import java.util.Set;

public record StackableItemDetailVO(
    long itemId,
    long templateId,
    String name,
    String typeName,
    int quantity,
    String quality,
    String description,
    Map<String, Object> properties,
    Set<String> tags) {}
