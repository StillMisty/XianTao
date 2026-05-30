package top.stillmisty.xiantao.domain.dungeon.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import top.stillmisty.xiantao.infrastructure.mybatis.handler.StringListTypeHandler;

@SuppressWarnings("NullAway")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table("dungeon_spirit_state")
public class DungeonSpiritState {

  @EqualsAndHashCode.Include
  @Id(keyType = KeyType.Auto)
  private Long id;

  private Long instanceId;
  private Long dungeonId;
  private Long userId;
  private Integer favor;

  @Column(typeHandler = StringListTypeHandler.class)
  private List<String> favorLog;

  @Column(typeHandler = StringListTypeHandler.class)
  private List<String> hiddenFinds;

  @Column(typeHandler = StringListTypeHandler.class)
  private List<String> triggeredEvents;

  public void addFavor(int change, String reason) {
    if (favor == null) favor = 0;
    favor += change;
    if (favorLog == null) favorLog = new ArrayList<>();
    String entry = (change >= 0 ? "+" : "") + change + " " + reason;
    favorLog.add(entry);
  }

  public void addHiddenFind(String poiName) {
    if (hiddenFinds == null) hiddenFinds = new ArrayList<>();
    if (!hiddenFinds.contains(poiName)) {
      hiddenFinds.add(poiName);
    }
  }

  public void addTriggeredEvent(String event) {
    if (triggeredEvents == null) triggeredEvents = new ArrayList<>();
    if (!triggeredEvents.contains(event)) {
      triggeredEvents.add(event);
    }
  }

  public String favorAttitude() {
    if (favor == null) return "冷淡";
    if (favor <= -50) return "敌视";
    if (favor <= 0) return "冷淡";
    if (favor <= 50) return "平和";
    if (favor <= 100) return "欣赏";
    return "亲近";
  }
}
