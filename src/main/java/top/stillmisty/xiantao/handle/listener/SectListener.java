package top.stillmisty.xiantao.handle.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import love.forte.simbot.component.onebot.v11.core.event.message.OneBotMessageEvent;
import love.forte.simbot.component.qguild.event.QGGroupAtMessageCreateEvent;
import love.forte.simbot.quantcat.common.annotations.ContentTrim;
import love.forte.simbot.quantcat.common.annotations.Filter;
import love.forte.simbot.quantcat.common.annotations.FilterValue;
import love.forte.simbot.quantcat.common.annotations.Listener;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.handle.TextFormat;
import top.stillmisty.xiantao.handle.command.SectCommandHandler;

@Slf4j
@Component
@RequiredArgsConstructor
public class SectListener {

  private final SectCommandHandler sectCommandHandler;
  private final ReplyHelper replyHelper;

  // === OneBotV11 ===

  @Listener
  @ContentTrim
  @Filter("宗门")
  public void overview(OneBotMessageEvent event) {
    String response =
        sectCommandHandler.handleOverview(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString(), TextFormat.PLAIN);
    replyHelper.replyOneBot(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("宗门创建 {{name,[\\S]+}}")
  public void create(OneBotMessageEvent event, @FilterValue("name") String name) {
    String response =
        sectCommandHandler.handleCreate(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString(), name, TextFormat.PLAIN);
    replyHelper.replyOneBot(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("宗门邀请 {{targetNickname,[\\S]+}}")
  public void invite(
      OneBotMessageEvent event, @FilterValue("targetNickname") String targetNickname) {
    String response =
        sectCommandHandler.handleInvite(
            PlatformType.ONE_BOT_V11,
            event.getAuthorId().toString(),
            targetNickname,
            TextFormat.PLAIN);
    replyHelper.replyOneBot(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("宗门申请 {{sectName,.+}}")
  public void apply(OneBotMessageEvent event, @FilterValue("sectName") String sectName) {
    String response =
        sectCommandHandler.handleApply(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString(), sectName, TextFormat.PLAIN);
    replyHelper.replyOneBot(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("宗门踢出 {{targetNickname,[\\S]+}}")
  public void kick(OneBotMessageEvent event, @FilterValue("targetNickname") String targetNickname) {
    String response =
        sectCommandHandler.handleKick(
            PlatformType.ONE_BOT_V11,
            event.getAuthorId().toString(),
            targetNickname,
            TextFormat.PLAIN);
    replyHelper.replyOneBot(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("宗门退出")
  public void leave(OneBotMessageEvent event) {
    String response =
        sectCommandHandler.handleLeave(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString(), TextFormat.PLAIN);
    replyHelper.replyOneBot(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("宗门任命 {{targetNickname,[\\S]+}} {{position,[\\S]+}}")
  public void appoint(
      OneBotMessageEvent event,
      @FilterValue("targetNickname") String targetNickname,
      @FilterValue("position") String position) {
    String response =
        sectCommandHandler.handleAppoint(
            PlatformType.ONE_BOT_V11,
            event.getAuthorId().toString(),
            targetNickname,
            position,
            TextFormat.PLAIN);
    replyHelper.replyOneBot(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("宗门解散")
  public void dismiss(OneBotMessageEvent event) {
    String response =
        sectCommandHandler.handleDismiss(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString(), TextFormat.PLAIN);
    replyHelper.replyOneBot(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("宗门公告 {{content,.+}}")
  public void notice(OneBotMessageEvent event, @FilterValue("content") String content) {
    String response =
        sectCommandHandler.handleNotice(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString(), content, TextFormat.PLAIN);
    replyHelper.replyOneBot(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("宗门捐献 {{amount,[\\d]+}}")
  public void donate(OneBotMessageEvent event, @FilterValue("amount") String amountStr) {
    long amount = Long.parseLong(amountStr);
    String response =
        sectCommandHandler.handleDonate(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString(), amount, TextFormat.PLAIN);
    replyHelper.replyOneBot(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("宗门商店")
  public void shop(OneBotMessageEvent event) {
    String response =
        sectCommandHandler.handleShop(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString(), TextFormat.PLAIN);
    replyHelper.replyOneBot(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("宗门兑换 {{shopItemId,[\\d]+}}")
  public void exchange(OneBotMessageEvent event, @FilterValue("shopItemId") String shopItemId) {
    long id = Long.parseLong(shopItemId);
    String response =
        sectCommandHandler.handleExchange(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString(), id, TextFormat.PLAIN);
    replyHelper.replyOneBot(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("宗门功法")
  public void skills(OneBotMessageEvent event) {
    String response =
        sectCommandHandler.handleSkills(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString(), TextFormat.PLAIN);
    replyHelper.replyOneBot(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("宗门升级")
  public void upgrade(OneBotMessageEvent event) {
    String response =
        sectCommandHandler.handleUpgrade(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString(), TextFormat.PLAIN);
    replyHelper.replyOneBot(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("宗门扩充")
  public void expand(OneBotMessageEvent event) {
    String response =
        sectCommandHandler.handleExpand(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString(), TextFormat.PLAIN);
    replyHelper.replyOneBot(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("宗门刷新商店")
  public void refreshShop(OneBotMessageEvent event) {
    String response =
        sectCommandHandler.handleRefreshShop(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString(), TextFormat.PLAIN);
    replyHelper.replyOneBot(event, response);
  }

  // === QQ ===

  @Listener
  @ContentTrim
  @Filter("宗门")
  public void overviewQq(QGGroupAtMessageCreateEvent event) {
    String response =
        sectCommandHandler.handleOverview(
            PlatformType.QQ, event.getAuthorId().toString(), TextFormat.MARKDOWN);
    replyHelper.replyQQ(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("宗门创建 {{name,[\\S]+}}")
  public void createQq(QGGroupAtMessageCreateEvent event, @FilterValue("name") String name) {
    String response =
        sectCommandHandler.handleCreate(
            PlatformType.QQ, event.getAuthorId().toString(), name, TextFormat.MARKDOWN);
    replyHelper.replyQQ(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("宗门邀请 {{targetNickname,[\\S]+}}")
  public void inviteQq(
      QGGroupAtMessageCreateEvent event, @FilterValue("targetNickname") String targetNickname) {
    String response =
        sectCommandHandler.handleInvite(
            PlatformType.QQ, event.getAuthorId().toString(), targetNickname, TextFormat.MARKDOWN);
    replyHelper.replyQQ(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("宗门申请 {{sectName,.+}}")
  public void applyQq(QGGroupAtMessageCreateEvent event, @FilterValue("sectName") String sectName) {
    String response =
        sectCommandHandler.handleApply(
            PlatformType.QQ, event.getAuthorId().toString(), sectName, TextFormat.MARKDOWN);
    replyHelper.replyQQ(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("宗门踢出 {{targetNickname,[\\S]+}}")
  public void kickQq(
      QGGroupAtMessageCreateEvent event, @FilterValue("targetNickname") String targetNickname) {
    String response =
        sectCommandHandler.handleKick(
            PlatformType.QQ, event.getAuthorId().toString(), targetNickname, TextFormat.MARKDOWN);
    replyHelper.replyQQ(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("宗门退出")
  public void leaveQq(QGGroupAtMessageCreateEvent event) {
    String response =
        sectCommandHandler.handleLeave(
            PlatformType.QQ, event.getAuthorId().toString(), TextFormat.MARKDOWN);
    replyHelper.replyQQ(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("宗门任命 {{targetNickname,[\\S]+}} {{position,[\\S]+}}")
  public void appointQq(
      QGGroupAtMessageCreateEvent event,
      @FilterValue("targetNickname") String targetNickname,
      @FilterValue("position") String position) {
    String response =
        sectCommandHandler.handleAppoint(
            PlatformType.QQ,
            event.getAuthorId().toString(),
            targetNickname,
            position,
            TextFormat.MARKDOWN);
    replyHelper.replyQQ(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("宗门解散")
  public void dismissQq(QGGroupAtMessageCreateEvent event) {
    String response =
        sectCommandHandler.handleDismiss(
            PlatformType.QQ, event.getAuthorId().toString(), TextFormat.MARKDOWN);
    replyHelper.replyQQ(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("宗门公告 {{content,.+}}")
  public void noticeQq(QGGroupAtMessageCreateEvent event, @FilterValue("content") String content) {
    String response =
        sectCommandHandler.handleNotice(
            PlatformType.QQ, event.getAuthorId().toString(), content, TextFormat.MARKDOWN);
    replyHelper.replyQQ(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("宗门捐献 {{amount,[\\d]+}}")
  public void donateQq(QGGroupAtMessageCreateEvent event, @FilterValue("amount") String amountStr) {
    long amount = Long.parseLong(amountStr);
    String response =
        sectCommandHandler.handleDonate(
            PlatformType.QQ, event.getAuthorId().toString(), amount, TextFormat.MARKDOWN);
    replyHelper.replyQQ(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("宗门商店")
  public void shopQq(QGGroupAtMessageCreateEvent event) {
    String response =
        sectCommandHandler.handleShop(
            PlatformType.QQ, event.getAuthorId().toString(), TextFormat.MARKDOWN);
    replyHelper.replyQQ(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("宗门兑换 {{shopItemId,[\\d]+}}")
  public void exchangeQq(
      QGGroupAtMessageCreateEvent event, @FilterValue("shopItemId") String shopItemId) {
    long id = Long.parseLong(shopItemId);
    String response =
        sectCommandHandler.handleExchange(
            PlatformType.QQ, event.getAuthorId().toString(), id, TextFormat.MARKDOWN);
    replyHelper.replyQQ(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("宗门功法")
  public void skillsQq(QGGroupAtMessageCreateEvent event) {
    String response =
        sectCommandHandler.handleSkills(
            PlatformType.QQ, event.getAuthorId().toString(), TextFormat.MARKDOWN);
    replyHelper.replyQQ(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("宗门升级")
  public void upgradeQq(QGGroupAtMessageCreateEvent event) {
    String response =
        sectCommandHandler.handleUpgrade(
            PlatformType.QQ, event.getAuthorId().toString(), TextFormat.MARKDOWN);
    replyHelper.replyQQ(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("宗门扩充")
  public void expandQq(QGGroupAtMessageCreateEvent event) {
    String response =
        sectCommandHandler.handleExpand(
            PlatformType.QQ, event.getAuthorId().toString(), TextFormat.MARKDOWN);
    replyHelper.replyQQ(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("宗门刷新商店")
  public void refreshShopQq(QGGroupAtMessageCreateEvent event) {
    String response =
        sectCommandHandler.handleRefreshShop(
            PlatformType.QQ, event.getAuthorId().toString(), TextFormat.MARKDOWN);
    replyHelper.replyQQ(event, response);
  }
}
