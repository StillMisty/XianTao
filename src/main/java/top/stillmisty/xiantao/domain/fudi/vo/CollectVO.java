package top.stillmisty.xiantao.domain.fudi.vo;

public record CollectVO(
    int cellId, String type, String cropName, String beastName, int yield, int totalItems) {

  public static CollectVO forFarm(int cellId, String cropName, int yield) {
    return new CollectVO(cellId, "FARM", cropName, null, yield, yield);
  }

  public static CollectVO forPen(int cellId, String beastName, int totalItems) {
    return new CollectVO(cellId, "PEN", null, beastName, totalItems, totalItems);
  }
}
