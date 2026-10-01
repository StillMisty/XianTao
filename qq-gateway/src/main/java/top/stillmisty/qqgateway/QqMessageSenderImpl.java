package top.stillmisty.qqgateway;

import java.net.URI;
import java.util.List;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * {@link QqMessageSender} 默认实现：Markdown 组装 + 可选按钮键盘 + msg_seq 分配 + 重试。
 *
 * <p>重试语义：Token 失效先刷新再试；限流按退避重试。被动回复的重试始终复用同一 {@code msg_seq}， 避免「请求已送达但响应丢失」导致的重复消息。
 *
 * <p>降级语义：携带按钮的请求若被平台以 4xx 拒绝，自动去掉按钮重发一次，保证消息本身可达。
 */
final class QqMessageSenderImpl implements QqMessageSender {

  private static final Logger log = LoggerFactory.getLogger(QqMessageSenderImpl.class);
  private static final int MAX_ATTEMPTS = 3;
  private static final long INITIAL_RETRY_DELAY_MILLIS = 300;
  private static final long MAX_RETRY_DELAY_MILLIS = 3_000;

  private final QqHttpApi api;
  private final AccessTokenManager tokens;
  private final MessageSeqAllocator seqs;
  private final ObjectMapper mapper;

  QqMessageSenderImpl(
      QqHttpApi api, AccessTokenManager tokens, MessageSeqAllocator seqs, ObjectMapper mapper) {
    this.api = api;
    this.tokens = tokens;
    this.seqs = seqs;
    this.mapper = mapper;
  }

  @Override
  public void replyMarkdown(
      QqIncomingMessage incoming, String markdown, @Nullable QqKeyboard keyboard) {
    int seq = seqs.next(incoming.messageId());
    sendWithRetry(
        api.messageUri(incoming.scene(), incoming.groupOpenId(), incoming.openId()),
        markdown,
        incoming.messageId(),
        seq,
        keyboard,
        "被动回复");
  }

  @Override
  public void sendGroupMarkdown(
      String groupOpenId, String markdown, @Nullable QqKeyboard keyboard) {
    sendWithRetry(
        api.messageUri(QqScene.GROUP_AT, groupOpenId, ""),
        markdown,
        null,
        null,
        keyboard,
        "群聊主动消息");
  }

  @Override
  public void sendC2CMarkdown(String openId, String markdown, @Nullable QqKeyboard keyboard) {
    sendWithRetry(
        api.messageUri(QqScene.C2C, null, openId), markdown, null, null, keyboard, "单聊主动消息");
  }

  private ObjectNode markdownBody(
      String markdown,
      @Nullable String messageId,
      @Nullable Integer seq,
      @Nullable QqKeyboard keyboard) {
    ObjectNode body = mapper.createObjectNode();
    body.put("msg_type", 2);
    ObjectNode markdownNode = mapper.createObjectNode();
    markdownNode.put("content", markdown);
    body.set("markdown", markdownNode);
    if (messageId != null) {
      body.put("msg_id", messageId);
    }
    if (seq != null) {
      body.put("msg_seq", seq);
    }
    if (keyboard != null) {
      body.set("keyboard", keyboardJson(keyboard));
    }
    return body;
  }

  private ObjectNode keyboardJson(QqKeyboard keyboard) {
    ObjectNode content = mapper.createObjectNode();
    ArrayNode rows = content.putArray("rows");
    for (QqKeyboard.QqButtonRow row : keyboard.rows()) {
      ObjectNode rowNode = rows.addObject();
      ArrayNode buttons = rowNode.putArray("buttons");
      for (QqButton button : row.buttons()) {
        buttons.add(buttonJson(button));
      }
    }
    ObjectNode keyboardNode = mapper.createObjectNode();
    keyboardNode.set("content", content);
    return keyboardNode;
  }

  private ObjectNode buttonJson(QqButton button) {
    ObjectNode node = mapper.createObjectNode();
    node.put("id", button.id());

    ObjectNode renderData = node.putObject("render_data");
    renderData.put("label", button.label());
    renderData.put("visited_label", button.visitedLabel());
    renderData.put("style", button.style());

    ObjectNode action = node.putObject("action");
    action.put("type", button.actionType());
    ObjectNode permission = action.putObject("permission");
    List<String> userIds = button.specifyUserIds();
    if (userIds == null) {
      permission.put("type", 2);
    } else {
      permission.put("type", 0);
      ArrayNode ids = permission.putArray("specify_user_ids");
      userIds.forEach(ids::add);
    }
    action.put("data", button.data());
    if (button.actionType() == QqButton.ACTION_COMMAND) {
      action.put("enter", button.enter());
    }
    action.put("unsupport_tips", button.unsupportTips());
    return node;
  }

  private void sendWithRetry(
      URI uri,
      String markdown,
      @Nullable String messageId,
      @Nullable Integer seq,
      @Nullable QqKeyboard keyboard,
      String action) {
    boolean withKeyboard = keyboard != null;
    long delay = INITIAL_RETRY_DELAY_MILLIS;
    for (int attempt = 1; ; attempt++) {
      ObjectNode body = markdownBody(markdown, messageId, seq, withKeyboard ? keyboard : null);
      try {
        api.postJson(uri, tokens.token(), body);
        return;
      } catch (QqApiException e) {
        boolean lastAttempt = attempt >= MAX_ATTEMPTS;
        if (e.isTokenInvalid() && !lastAttempt) {
          log.debug("QQ {} 遇到 Token 失效，刷新后重试", action);
          tokens.invalidate();
          continue;
        }
        if (e.isRateLimited() && !lastAttempt) {
          long wait = e.retryAfterMillis() > 0 ? e.retryAfterMillis() : delay;
          wait = Math.min(wait, MAX_RETRY_DELAY_MILLIS);
          log.debug("QQ {} 被限流，{}ms 后重试", action, wait);
          sleep(wait);
          delay = Math.min(delay * 2, MAX_RETRY_DELAY_MILLIS);
          continue;
        }
        if (e.isServerError() && !lastAttempt) {
          log.debug("QQ {} 遇到平台 5xx，{}ms 后重试", action, delay);
          sleep(delay);
          delay = Math.min(delay * 2, MAX_RETRY_DELAY_MILLIS);
          continue;
        }
        if (withKeyboard
            && !lastAttempt
            && e.httpStatus() >= 400
            && e.httpStatus() < 500
            && !e.isTokenInvalid()
            && !e.isRateLimited()) {
          log.warn("QQ {} 携带按钮被平台拒绝（{}），降级为无按钮重试", action, e.getMessage());
          withKeyboard = false;
          continue;
        }
        log.warn("QQ {} 失败: {}", action, e.getMessage());
        throw e;
      }
    }
  }

  private static void sleep(long millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new QqApiException(0, null, "重试等待被中断", e);
    }
  }
}
