package top.stillmisty.xiantao.domain.shop.vo;

public record PurchaseResult(String itemName, int quantity, long totalPrice, String error) {}
