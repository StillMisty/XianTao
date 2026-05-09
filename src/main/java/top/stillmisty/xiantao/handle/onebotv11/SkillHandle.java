package top.stillmisty.xiantao.handle.onebotv11;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import love.forte.simbot.component.onebot.v11.core.event.message.OneBotMessageEvent;
import love.forte.simbot.quantcat.common.annotations.ContentTrim;
import love.forte.simbot.quantcat.common.annotations.Filter;
import love.forte.simbot.quantcat.common.annotations.FilterValue;
import love.forte.simbot.quantcat.common.annotations.Listener;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.handle.command.SkillCommandHandler;
import top.stillmisty.xiantao.service.NotificationAppender;

@Slf4j
@Component
@RequiredArgsConstructor
public class SkillHandle {

  private final SkillCommandHandler skillCommandHandler;
  private final NotificationAppender notificationAppender;

  @Listener
  @ContentTrim
  @Filter("法决列表")
  public void learnedSkillsAll(OneBotMessageEvent event) {
    log.debug("收到法决列表请求 - AuthorId: {}", event.getAuthorId());
    String response =
        skillCommandHandler.handleLearnedSkills(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString());
    sendWithNotifications(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("法决装载 {{skill}}")
  public void equipSkill(OneBotMessageEvent event, @FilterValue("skill") String skill) {
    log.debug("收到法决装载请求 - AuthorId: {}, Skill: {}", event.getAuthorId(), skill);
    if (skill == null || skill.isEmpty()) {
      event.replyBlocking("用法：法决装载 [法决名称或编号]\n示例：法决装载 御剑术");
      return;
    }
    String response =
        skillCommandHandler.handleEquipSkill(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString(), skill);
    sendWithNotifications(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("法决卸下 {{skill}}")
  public void unequipSkill(OneBotMessageEvent event, @FilterValue("skill") String skill) {
    log.debug("收到法决卸下请求 - AuthorId: {}, Skill: {}", event.getAuthorId(), skill);
    if (skill == null || skill.isEmpty()) {
      event.replyBlocking("用法：法决卸下 [法决名称或编号]\n示例：法决卸下 御剑术");
      return;
    }
    String response =
        skillCommandHandler.handleUnequipSkill(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString(), skill);
    sendWithNotifications(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("法决")
  public void equippedSkills(OneBotMessageEvent event) {
    log.debug("收到法决查询请求 - AuthorId: {}", event.getAuthorId());
    String response =
        skillCommandHandler.handleEquippedSkills(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString());
    sendWithNotifications(event, response);
  }

  private void sendWithNotifications(OneBotMessageEvent event, String response) {
    var result =
        notificationAppender.prepareAppend(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString(), response);
    event.replyBlocking(result.text());
    notificationAppender.markDelivered(result.eventIds());
  }
}
