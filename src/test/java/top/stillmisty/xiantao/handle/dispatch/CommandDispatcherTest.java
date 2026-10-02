package top.stillmisty.xiantao.handle.dispatch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.GenericApplicationContext;
import top.stillmisty.qqgateway.QqIncomingMessage;
import top.stillmisty.qqgateway.QqScene;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.handle.interceptor.RequireAuth;
import top.stillmisty.xiantao.handle.interceptor.RequireGm;
import top.stillmisty.xiantao.handle.listener.ReplyHelper;
import top.stillmisty.xiantao.handle.platform.PlatformHandler;
import top.stillmisty.xiantao.handle.platform.PlatformRegistry;
import top.stillmisty.xiantao.service.AuthenticationService;
import top.stillmisty.xiantao.service.ServiceResult;
import top.stillmisty.xiantao.service.UserContext;
import top.stillmisty.xiantao.service.player.UserStateService;

class CommandDispatcherTest {

  @CommandGroup
  static class Commands {
    final List<String> calls = new CopyOnWriteArrayList<>();
    final AtomicReference<Long> scopedUser = new AtomicReference<>();

    @Command("状态")
    @RequireAuth
    public void status(QqIncomingMessage message) {
      calls.add("status");
      scopedUser.set(UserContext.getCurrentUserId());
    }

    @Command("GM重置")
    @RequireAuth
    @RequireGm
    public void gmReset(QqIncomingMessage message) {
      calls.add("gm");
    }
  }

  @CommandGroup
  static class GateCommands {

    final List<String> log = new CopyOnWriteArrayList<>();
    final CountDownLatch firstStarted = new CountDownLatch(1);
    final CountDownLatch release = new CountDownLatch(1);

    @Command("闸门")
    public void gate(QqIncomingMessage message) {
      String who = message.openId();
      log.add("start-" + who);
      if (who.equals("USER-A") && firstStarted.getCount() > 0) {
        firstStarted.countDown();
        try {
          release.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        }
      }
      log.add("end-" + who);
    }
  }

  @Test
  void sameUserCommandsAreSerialized() throws Exception {
    GenericApplicationContext context = contextOf(GateCommands.class);
    try {
      GateCommands commands = context.getBean(GateCommands.class);
      CommandDispatcher dispatcher =
          dispatcher(context, new StubHandler(), mock(AuthenticationService.class));

      dispatcher.onMessage(message("闸门", "USER-A"));
      assertTrue(commands.firstStarted.await(3, TimeUnit.SECONDS));

      dispatcher.onMessage(message("闸门", "USER-A"));
      Thread.sleep(200);
      assertEquals(List.of("start-USER-A"), commands.log, "同一玩家的第二条命令不应并发开始");

      commands.release.countDown();
      awaitTrue(() -> commands.log.size() == 4);
      assertEquals(
          List.of("start-USER-A", "end-USER-A", "start-USER-A", "end-USER-A"), commands.log);
    } finally {
      context.close();
    }
  }

  @Test
  void differentUsersAreNotBlocked() throws Exception {
    GenericApplicationContext context = contextOf(GateCommands.class);
    try {
      GateCommands commands = context.getBean(GateCommands.class);
      CommandDispatcher dispatcher =
          dispatcher(context, new StubHandler(), mock(AuthenticationService.class));

      dispatcher.onMessage(message("闸门", "USER-A"));
      assertTrue(commands.firstStarted.await(3, TimeUnit.SECONDS));

      dispatcher.onMessage(message("闸门", "USER-B"));
      awaitTrue(() -> commands.log.contains("end-USER-B"));
      assertFalse(commands.log.contains("end-USER-A"), "USER-A 仍应被阻塞");

      commands.release.countDown();
      awaitTrue(() -> commands.log.contains("end-USER-A"));
    } finally {
      context.close();
    }
  }

  static final class StubHandler implements PlatformHandler {

    final List<String> replies = new CopyOnWriteArrayList<>();

    @Override
    public PlatformType getPlatformType() {
      return PlatformType.QQ;
    }

    @Override
    public boolean supports(QqIncomingMessage message) {
      return true;
    }

    @Override
    public String extractOpenId(QqIncomingMessage message) {
      return message.openId();
    }

    @Override
    public void replyText(QqIncomingMessage message, String text) {
      replies.add(text);
    }
  }

  @Test
  void invokesMatchedCommandWithBoundUser() throws Exception {
    GenericApplicationContext context = contextOf(Commands.class);
    try {
      Commands commands = context.getBean(Commands.class);
      StubHandler handler = new StubHandler();
      AuthenticationService auth = mock(AuthenticationService.class);
      when(auth.authenticate(PlatformType.QQ, "OPEN-1"))
          .thenReturn(new ServiceResult.Success<>(42L));
      CommandDispatcher dispatcher = dispatcher(context, handler, auth);

      dispatcher.onMessage(message("状态"));

      awaitTrue(() -> commands.calls.size() == 1);
      assertEquals(42L, commands.scopedUser.get());
      assertTrue(handler.replies.isEmpty());
    } finally {
      context.close();
    }
  }

  @Test
  void ignoresUnmatchedMessage() throws Exception {
    GenericApplicationContext context = contextOf(Commands.class);
    try {
      Commands commands = context.getBean(Commands.class);
      StubHandler handler = new StubHandler();
      CommandDispatcher dispatcher =
          dispatcher(context, handler, mock(AuthenticationService.class));

      dispatcher.onMessage(message("随便说说"));

      Thread.sleep(150);
      assertTrue(commands.calls.isEmpty());
      assertTrue(handler.replies.isEmpty());
    } finally {
      context.close();
    }
  }

  @Test
  void repliesWhenAuthenticationFails() throws Exception {
    GenericApplicationContext context = contextOf(Commands.class);
    try {
      Commands commands = context.getBean(Commands.class);
      StubHandler handler = new StubHandler();
      AuthenticationService auth = mock(AuthenticationService.class);
      when(auth.authenticate(PlatformType.QQ, "OPEN-1"))
          .thenReturn(ServiceResult.authFailure("输入「我要修仙 [道号]」进入仙途吧！"));
      CommandDispatcher dispatcher = dispatcher(context, handler, auth);

      dispatcher.onMessage(message("状态"));

      awaitTrue(() -> !handler.replies.isEmpty());
      assertTrue(handler.replies.getFirst().contains("我要修仙"));
      assertTrue(commands.calls.isEmpty());
    } finally {
      context.close();
    }
  }

  @Test
  void deniesGmCommandsWithoutPermission() throws Exception {
    GenericApplicationContext context = contextOf(Commands.class);
    try {
      Commands commands = context.getBean(Commands.class);
      StubHandler handler = new StubHandler();
      AuthenticationService auth = mock(AuthenticationService.class);
      when(auth.authenticate(PlatformType.QQ, "OPEN-1"))
          .thenReturn(new ServiceResult.Success<>(42L));
      when(auth.isGm(42L)).thenReturn(false);
      CommandDispatcher dispatcher = dispatcher(context, handler, auth);

      dispatcher.onMessage(message("GM重置"));

      awaitTrue(() -> !handler.replies.isEmpty());
      assertTrue(handler.replies.getFirst().contains("你不是 GM"));
      assertTrue(commands.calls.isEmpty());
    } finally {
      context.close();
    }
  }

  @Test
  void repliesWhenAuthLookupThrows() throws Exception {
    GenericApplicationContext context = contextOf(Commands.class);
    try {
      Commands commands = context.getBean(Commands.class);
      StubHandler handler = new StubHandler();
      AuthenticationService auth = mock(AuthenticationService.class);
      when(auth.authenticate(PlatformType.QQ, "OPEN-1"))
          .thenThrow(new IllegalStateException("db down"));
      CommandDispatcher dispatcher = dispatcher(context, handler, auth);

      dispatcher.onMessage(message("状态"));

      awaitTrue(() -> !handler.replies.isEmpty());
      assertTrue(handler.replies.getFirst().contains("系统繁忙"));
      assertTrue(commands.calls.isEmpty());
    } finally {
      context.close();
    }
  }

  private static GenericApplicationContext contextOf(Class<?> type) {
    GenericApplicationContext context = new GenericApplicationContext();
    register(context, type);
    context.refresh();
    return context;
  }

  @SuppressWarnings("unchecked")
  private static void register(GenericApplicationContext context, Class<?> type) {
    context.registerBean((Class<Object>) type);
  }

  private static CommandDispatcher dispatcher(
      GenericApplicationContext context, PlatformHandler handler, AuthenticationService auth) {
    CommandRegistry registry = new CommandRegistry(context);
    PlatformRegistry platformRegistry = new PlatformRegistry(List.of(handler));
    ReplyHelper replyHelper = new ReplyHelper(platformRegistry);
    UserStateService userStateService = mock(UserStateService.class);
    return new CommandDispatcher(registry, auth, platformRegistry, replyHelper, userStateService);
  }

  private static QqIncomingMessage message(String content) {
    return message(content, "OPEN-1");
  }

  private static QqIncomingMessage message(String content, String openId) {
    return new QqIncomingMessage(
        "EVENT-1", "MSG-1", QqScene.GROUP_AT, openId, "GROUP-1", content, null, Instant.EPOCH);
  }

  private static void awaitTrue(BooleanSupplier condition) throws InterruptedException {
    long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
    while (!condition.getAsBoolean()) {
      if (System.nanoTime() > deadline) {
        fail("等待条件超时");
      }
      Thread.sleep(20);
    }
  }
}
