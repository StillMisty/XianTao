package top.stillmisty.xiantao.domain.sect.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import top.stillmisty.xiantao.domain.sect.enums.ChatRole;
import top.stillmisty.xiantao.domain.sect.enums.ChatType;
import top.stillmisty.xiantao.infrastructure.mybatis.handler.JsonbTypeHandler;

/** 统一对话历史实体（地灵/商铺/宗灵/旅行商人 共用） */
@EqualsAndHashCode
@Table("xt_chat_history")
@Accessors(chain = true)
@Data
@NoArgsConstructor
public class ChatHistory {

  public static ChatHistory create() {
    return new ChatHistory();
  }

  @Id(keyType = KeyType.Auto)
  private Long id;

  private ChatType chatType;

  private Long conversationId;

  private Long userId;

  private ChatRole role;

  private String content;

  @Column(typeHandler = JsonbTypeHandler.class)
  private Map<String, Object> extraData;

  @Column(onInsertValue = "now()")
  private LocalDateTime createTime;

  public boolean isFromUser() {
    return ChatRole.USER.equals(role);
  }

  public boolean isFromAssistant() {
    return ChatRole.ASSISTANT.equals(role);
  }
}
