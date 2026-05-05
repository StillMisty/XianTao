package top.stillmisty.xiantao.domain.fudi.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import java.time.LocalDateTime;
import lombok.Data;
import top.stillmisty.xiantao.domain.fudi.enums.EmotionState;

/** 地灵对话历史实体 */
@Data
@Table("xt_spirit_history")
public class SpiritHistory {

  @Id(keyType = KeyType.Auto)
  private Long id;

  /** 关联福地ID */
  private Long fudiId;

  /** 角色：user/assistant/system */
  private String role;

  /** 对话内容 */
  private String content;

  /** 情绪状态 */
  private EmotionState emotionState;

  /** 创建时间 */
  @Column(onInsertValue = "now()")
  private LocalDateTime createTime;
}
