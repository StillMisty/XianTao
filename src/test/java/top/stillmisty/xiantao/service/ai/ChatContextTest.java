package top.stillmisty.xiantao.service.ai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class ChatContextTest {

  private record TestContext(String value) {}

  @Test
  void requireReturnsBoundContext() {
    TestContext ctx = new TestContext("店铺");

    String result = ChatContext.with(ctx, () -> ChatContext.require(TestContext.class).value());

    assertEquals("店铺", result);
  }

  @Test
  void requireThrowsWhenMissingOrWrongType() {
    assertThrows(IllegalStateException.class, () -> ChatContext.require(TestContext.class));

    assertThrows(
        IllegalStateException.class,
        () -> ChatContext.with("not-a-context", () -> ChatContext.require(TestContext.class)));
  }

  @Test
  void currentReturnsNullOutsideConversation() {
    assertNull(ChatContext.current(TestContext.class));
  }
}
