package top.stillmisty.xiantao.handle.listener;

import lombok.RequiredArgsConstructor;
import love.forte.simbot.event.MessageEvent;
import love.forte.simbot.quantcat.common.annotations.ContentTrim;
import love.forte.simbot.quantcat.common.annotations.Filter;
import love.forte.simbot.quantcat.common.annotations.FilterValue;
import love.forte.simbot.quantcat.common.annotations.Listener;
import love.forte.simbot.quantcat.common.filter.FilterMode;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.handle.command.SkillCommandHandler;
import top.stillmisty.xiantao.handle.interceptor.RequireAuth;

@Component
@RequiredArgsConstructor
public class SkillListener {
  private final SkillCommandHandler skillCommandHandler;
  private final ReplyHelper replyHelper;

  @Listener
  @ContentTrim
  @RequireAuth
  @Filter(mode = FilterMode.INTERCEPTOR, priority = 50, value = "法决装载\\s*{{skill}}")
  public void equipSkill(MessageEvent event, @FilterValue("skill") String skill) {
    replyHelper.dispatch(event, "法决装载", skill, skillCommandHandler::handleEquipSkill);
  }

  @Listener
  @ContentTrim
  @RequireAuth
  @Filter(mode = FilterMode.INTERCEPTOR, priority = 50, value = "法决卸下\\s*{{skill}}")
  public void unequipSkill(MessageEvent event, @FilterValue("skill") String skill) {
    replyHelper.dispatch(event, "法决卸下", skill, skillCommandHandler::handleUnequipSkill);
  }

  @Listener
  @ContentTrim
  @RequireAuth
  @Filter(mode = FilterMode.INTERCEPTOR, priority = 50, value = "法决")
  public void skills(MessageEvent event) {
    replyHelper.dispatch(event, "法决查询", skillCommandHandler::handleSkills);
  }
}
