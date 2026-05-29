package top.stillmisty.xiantao.handle.listener;

import lombok.RequiredArgsConstructor;
import love.forte.simbot.component.onebot.v11.core.event.message.OneBotMessageEvent;
import love.forte.simbot.component.qguild.event.QGGroupAtMessageCreateEvent;
import love.forte.simbot.quantcat.common.annotations.ContentTrim;
import love.forte.simbot.quantcat.common.annotations.Filter;
import love.forte.simbot.quantcat.common.annotations.FilterValue;
import love.forte.simbot.quantcat.common.annotations.Listener;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.handle.command.SkillCommandHandler;
import top.stillmisty.xiantao.handle.interceptor.RequireAuth;

@Component
@RequiredArgsConstructor
public class SkillListener {
  private final SkillCommandHandler skillCommandHandler;
  private final ReplyHelper replyHelper;

  // === OneBotV11 ===

  @Listener
  @ContentTrim
  @RequireAuth
  @Filter("法决装载\\s*{{skill}}")
  public void equipSkill(OneBotMessageEvent event, @FilterValue("skill") String skill) {
    replyHelper.oneBot(event, "法决装载", skill, skillCommandHandler::handleEquipSkill);
  }

  @Listener
  @ContentTrim
  @RequireAuth
  @Filter("法决卸下\\s*{{skill}}")
  public void unequipSkill(OneBotMessageEvent event, @FilterValue("skill") String skill) {
    replyHelper.oneBot(event, "法决卸下", skill, skillCommandHandler::handleUnequipSkill);
  }

  @Listener
  @ContentTrim
  @RequireAuth
  @Filter("法决")
  public void skills(OneBotMessageEvent event) {
    replyHelper.oneBot(event, "法决查询", skillCommandHandler::handleSkills);
  }

  // === QQ ===

  @Listener
  @ContentTrim
  @RequireAuth
  @Filter("法决装载\\s*{{skill}}")
  public void equipSkillQq(QGGroupAtMessageCreateEvent event, @FilterValue("skill") String skill) {
    replyHelper.qq(event, "法决装载", skill, skillCommandHandler::handleEquipSkill);
  }

  @Listener
  @ContentTrim
  @RequireAuth
  @Filter("法决卸下\\s*{{skill}}")
  public void unequipSkillQq(
      QGGroupAtMessageCreateEvent event, @FilterValue("skill") String skill) {
    replyHelper.qq(event, "法决卸下", skill, skillCommandHandler::handleUnequipSkill);
  }

  @Listener
  @ContentTrim
  @RequireAuth
  @Filter("法决")
  public void skillsQq(QGGroupAtMessageCreateEvent event) {
    replyHelper.qq(event, "法决查询", skillCommandHandler::handleSkills);
  }
}
