package top.stillmisty.xiantao.domain.shop.vo;

import java.util.List;

public record ProductListVO(String shopName, List<ProductEntry> products) {

  public record ProductEntry(
      long id, String type, String name, long price, int stock, String extra) {}
}
