package top.stillmisty.xiantao.domain.item.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

/** 丹方产物，从 Scroll 提取以降低嵌套深度 */
public record RecipeProduct(@JsonProperty("item_id") long itemId, int quantity) {}
