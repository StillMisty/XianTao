package top.stillmisty.xiantao.domain.fudi.vo;

public record GiveGiftVO(String itemName, int oldAffection, int newAffection, int change,
                          String reaction, boolean isLiked, boolean isDisliked) {
}
