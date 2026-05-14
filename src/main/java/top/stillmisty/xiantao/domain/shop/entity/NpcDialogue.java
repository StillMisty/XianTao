package top.stillmisty.xiantao.domain.shop.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;
import top.stillmisty.xiantao.domain.shop.enums.NpcType;
import top.stillmisty.xiantao.infrastructure.mybatis.handler.JsonbTypeHandler;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table("xt_npc_dialogue")
public class NpcDialogue {

  @EqualsAndHashCode.Include
  @Id(keyType = KeyType.Auto)
  private Long id;

  private Long userId;

  private NpcType npcType;

  private Long npcId;

  private String role;

  private String content;

  @Column(typeHandler = JsonbTypeHandler.class)
  private Map<String, Object> extraData;

  @Column(onInsertValue = "now()")
  private LocalDateTime createTime;

  // ===================== 业务逻辑方法 =====================

  public boolean isFromUser() {
    return "user".equals(role);
  }

  public boolean isFromAssistant() {
    return "assistant".equals(role);
  }
}
