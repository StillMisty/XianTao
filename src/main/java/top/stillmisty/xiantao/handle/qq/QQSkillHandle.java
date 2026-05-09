package top.stillmisty.xiantao.handle.qq;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import love.forte.simbot.component.qguild.event.QGGroupAtMessageCreateEvent;
import love.forte.simbot.component.qguild.message.QGMarkdown;
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
public class QQSkillHandle {

  private final SkillCommandHandler skillCommandHandler;
  private final NotificationAppender notificationAppender;

  @Listener
  @ContentTrim
  @Filter("法决列表")
  public void learnedSkillsAll(QGGroupAtMessageCreateEvent event) {
    log.debug("收到法决列表请求 - AuthorId: {}", event.getAuthorId());
    String response =
        skillCommandHandler.handleLearnedSkillsMarkdown(
            PlatformType.QQ, event.getAuthorId().toString());
    sendWithNotifications(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("法决装载 {{skill}}")
  public void equipSkill(QGGroupAtMessageCreateEvent event, @FilterValue("skill") String skill) {
    log.debug("收到法决装载请求 - AuthorId: {}, Skill: {}", event.getAuthorId(), skill);
    if (skill == null || skill.isEmpty()) {
      event.replyBlocking(QGMarkdown.create("用法：法决装载 [法决名称或编号]\n示例：法决装载 御剑术"));
      return;
    }
    String response =
        skillCommandHandler.handleEquipSkillMarkdown(
            PlatformType.QQ, event.getAuthorId().toString(), skill);
    sendWithNotifications(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("法决卸下 {{skill}}")
  public void unequipSkill(QGGroupAtMessageCreateEvent event, @FilterValue("skill") String skill) {
    log.debug("收到法决卸下请求 - AuthorId: {}, Skill: {}", event.getAuthorId(), skill);
    if (skill == null || skill.isEmpty()) {
      event.replyBlocking(QGMarkdown.create("用法：法决卸下 [法决名称或编号]\n示例：法决卸下 御剑术"));
      return;
    }
    String response =
        skillCommandHandler.handleUnequipSkillMarkdown(
            PlatformType.QQ, event.getAuthorId().toString(), skill);
    sendWithNotifications(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("法决")
  public void equippedSkills(QGGroupAtMessageCreateEvent event) {
    log.debug("收到法决查询请求 - AuthorId: {}", event.getAuthorId());
    String response =
        skillCommandHandler.handleEquippedSkillsMarkdown(
            PlatformType.QQ, event.getAuthorId().toString());
    sendWithNotifications(event, response);
  }

  private void sendWithNotifications(QGGroupAtMessageCreateEvent event, String response) {
    var result =
        notificationAppender.prepareAppend(
            PlatformType.QQ, event.getAuthorId().toString(), response);
    event.replyBlocking(QGMarkdown.create(result.text()));
    notificationAppender.markDelivered(result.eventIds());
  }
}
