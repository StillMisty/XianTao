package top.stillmisty.xiantao.handle.listener;

import lombok.RequiredArgsConstructor;
import love.forte.simbot.component.onebot.v11.core.event.message.OneBotMessageEvent;
import love.forte.simbot.component.qguild.event.QGGroupAtMessageCreateEvent;
import love.forte.simbot.component.qguild.message.QGMarkdown;
import love.forte.simbot.quantcat.common.annotations.ContentTrim;
import love.forte.simbot.quantcat.common.annotations.Filter;
import love.forte.simbot.quantcat.common.annotations.FilterValue;
import love.forte.simbot.quantcat.common.annotations.Listener;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.handle.command.SkillCommandHandler;

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
    if (skill == null || skill.isEmpty()) {
      event.replyBlocking("用法：法决装载 [法决名称或编号]\n示例：法决装载 御剑术");
      return;
    }
    replyHelper.oneBot(event, "法决装载", skill, skillCommandHandler::handleEquipSkill);
  }

  @Listener
  @ContentTrim
  @Filter("法决卸下 {{skill}}")
  public void unequipSkill(OneBotMessageEvent event, @FilterValue("skill") String skill) {
    if (skill == null || skill.isEmpty()) {
      event.replyBlocking("用法：法决卸下 [法决名称或编号]\n示例：法决卸下 御剑术");
      return;
    }
    replyHelper.oneBot(event, "法决卸下", skill, skillCommandHandler::handleUnequipSkill);
  }

  @Listener
  @ContentTrim
  @Filter("法决")
  public void skills(OneBotMessageEvent event) {
    replyHelper.oneBot(event, "法决查询", skillCommandHandler::handleSkills);
  }

  // === QQ ===

  @Listener
  @ContentTrim
  @Filter("法决装载 {{skill}}")
  public void equipSkillQq(QGGroupAtMessageCreateEvent event, @FilterValue("skill") String skill) {
    if (skill == null || skill.isEmpty()) {
      event.replyBlocking(QGMarkdown.create("用法：法决装载 [法决名称或编号]\n示例：法决装载 御剑术"));
      return;
    }
    replyHelper.qq(event, "法决装载", skill, skillCommandHandler::handleEquipSkill);
  }

  @Listener
  @ContentTrim
  @Filter("法决卸下 {{skill}}")
  public void unequipSkillQq(
      QGGroupAtMessageCreateEvent event, @FilterValue("skill") String skill) {
    if (skill == null || skill.isEmpty()) {
      event.replyBlocking(QGMarkdown.create("用法：法决卸下 [法决名称或编号]\n示例：法决卸下 御剑术"));
      return;
    }
    replyHelper.qq(event, "法决卸下", skill, skillCommandHandler::handleUnequipSkill);
  }

  @Listener
  @ContentTrim
  @Filter("法决")
  public void skillsQq(QGGroupAtMessageCreateEvent event) {
    replyHelper.qq(event, "法决查询", skillCommandHandler::handleSkills);
  }
}
