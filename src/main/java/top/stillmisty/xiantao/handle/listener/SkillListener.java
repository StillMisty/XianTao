package top.stillmisty.xiantao.handle.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import love.forte.simbot.component.onebot.v11.core.event.message.OneBotMessageEvent;
import love.forte.simbot.component.qguild.event.QGGroupAtMessageCreateEvent;
import love.forte.simbot.component.qguild.message.QGMarkdown;
import love.forte.simbot.quantcat.common.annotations.ContentTrim;
import love.forte.simbot.quantcat.common.annotations.Filter;
import love.forte.simbot.quantcat.common.annotations.FilterValue;
import love.forte.simbot.quantcat.common.annotations.Listener;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.handle.command.SkillCommandHandler;

@Slf4j
@Component
@RequiredArgsConstructor
public class SkillListener {
  private final SkillCommandHandler skillCommandHandler;
  private final ReplyHelper replyHelper;

  // === OneBotV11 ===

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
    replyHelper.replyOneBot(event, response);
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
    replyHelper.replyOneBot(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("法决")
  public void skills(OneBotMessageEvent event) {
    log.debug("收到法决查询请求 - AuthorId: {}", event.getAuthorId());
    String response =
        skillCommandHandler.handleSkills(PlatformType.ONE_BOT_V11, event.getAuthorId().toString());
    replyHelper.replyOneBot(event, response);
  }

  // === QQ ===

  @Listener
  @ContentTrim
  @Filter("法决装载 {{skill}}")
  public void equipSkillQq(QGGroupAtMessageCreateEvent event, @FilterValue("skill") String skill) {
    log.debug("收到法决装载请求 - AuthorId: {}, Skill: {}", event.getAuthorId(), skill);
    if (skill == null || skill.isEmpty()) {
      event.replyBlocking(QGMarkdown.create("用法：法决装载 [法决名称或编号]\n示例：法决装载 御剑术"));
      return;
    }
    String response =
        skillCommandHandler.handleEquipSkillMarkdown(
            PlatformType.QQ, event.getAuthorId().toString(), skill);
    replyHelper.replyQQ(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("法决卸下 {{skill}}")
  public void unequipSkillQq(
      QGGroupAtMessageCreateEvent event, @FilterValue("skill") String skill) {
    log.debug("收到法决卸下请求 - AuthorId: {}, Skill: {}", event.getAuthorId(), skill);
    if (skill == null || skill.isEmpty()) {
      event.replyBlocking(QGMarkdown.create("用法：法决卸下 [法决名称或编号]\n示例：法决卸下 御剑术"));
      return;
    }
    String response =
        skillCommandHandler.handleUnequipSkillMarkdown(
            PlatformType.QQ, event.getAuthorId().toString(), skill);
    replyHelper.replyQQ(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("法决")
  public void skillsQq(QGGroupAtMessageCreateEvent event) {
    log.debug("收到法决查询请求 - AuthorId: {}", event.getAuthorId());
    String response =
        skillCommandHandler.handleSkillsMarkdown(PlatformType.QQ, event.getAuthorId().toString());
    replyHelper.replyQQ(event, response);
  }
}
