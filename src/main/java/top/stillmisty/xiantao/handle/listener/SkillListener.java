package top.stillmisty.xiantao.handle.listener;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.stillmisty.qqgateway.QqIncomingMessage;
import top.stillmisty.xiantao.handle.command.SkillCommandHandler;
import top.stillmisty.xiantao.handle.dispatch.Arg;
import top.stillmisty.xiantao.handle.dispatch.Command;
import top.stillmisty.xiantao.handle.dispatch.CommandGroup;
import top.stillmisty.xiantao.handle.interceptor.RequireAuth;

@Component
@RequiredArgsConstructor
@CommandGroup
public class SkillListener {
  private final SkillCommandHandler skillCommandHandler;
  private final ReplyHelper replyHelper;

  @RequireAuth
  @Command("法决装载\\s*{{skill}}")
  public void equipSkill(QqIncomingMessage event, @Arg("skill") String skill) {
    replyHelper.dispatch(event, "法决装载", skill, skillCommandHandler::handleEquipSkill);
  }

  @RequireAuth
  @Command("法决卸下\\s*{{skill}}")
  public void unequipSkill(QqIncomingMessage event, @Arg("skill") String skill) {
    replyHelper.dispatch(event, "法决卸下", skill, skillCommandHandler::handleUnequipSkill);
  }

  @RequireAuth
  @Command("法决")
  public void skills(QqIncomingMessage event) {
    replyHelper.dispatch(event, "法决查询", skillCommandHandler::handleSkills);
  }
}
