package top.stillmisty.xiantao.service.ai;

import top.stillmisty.xiantao.domain.sect.enums.ChatType;
import top.stillmisty.xiantao.service.BusinessException;
import top.stillmisty.xiantao.service.ErrorCode;

public record ConversationId(ChatType chatType, Long userId, Long entityId) {

  public String value() {
    return chatType.getCode() + ":" + userId + ":" + entityId;
  }

  public static ConversationId from(String conversationId) {
    String[] parts = conversationId.split(":", 3);
    if (parts.length < 3) {
      throw new BusinessException(
          ErrorCode.PARAM_INVALID,
          "conversationId格式应为 chatType:userId:entityId，实际: " + conversationId);
    }
    ChatType chatType = ChatType.fromCode(parts[0]);
    Long userId = Long.parseLong(parts[1]);
    Long entityId = Long.parseLong(parts[2]);
    return new ConversationId(chatType, userId, entityId);
  }
}
