package top.stillmisty.xiantao.service.ai;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.service.UserContext;
import top.stillmisty.xiantao.service.sect.SectMemberService;
import top.stillmisty.xiantao.service.sect.SectSharedSkillService;

/** 宗门长老及以上可用的管理工具 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SectElderTools {

  private final SectMemberService sectMemberService;
  private final SectSharedSkillService sectSharedSkillService;

  @Tool(description = "邀请散修加入宗门，需对方未加入任何宗门")
  @Transactional
  public SectToolResponse inviteMember(@ToolParam(description = "目标道号") String targetNickname) {
    try {
      Long userId = UserContext.requireCurrentUserId();
      String result = sectMemberService.inviteMember(userId, targetNickname);
      return new SectToolResponse(true, result);
    } catch (Exception e) {
      log.error("邀请成员失败: targetNickname={}", targetNickname, e);
      return new SectToolResponse(false, e.getMessage());
    }
  }

  @Tool(description = "将下级成员踢出宗门，不可踢出同级或上级")
  @Transactional
  public SectToolResponse kickMember(@ToolParam(description = "成员道号") String targetNickname) {
    try {
      Long userId = UserContext.requireCurrentUserId();
      String result = sectMemberService.kickMember(userId, targetNickname);
      return new SectToolResponse(true, result);
    } catch (Exception e) {
      log.error("踢出成员失败: targetNickname={}", targetNickname, e);
      return new SectToolResponse(false, e.getMessage());
    }
  }

  @Tool(description = "发布宗门公告，所有成员可见，覆盖旧公告")
  @Transactional
  public SectToolResponse postNotice(@ToolParam(description = "公告内容") String content) {
    try {
      Long userId = UserContext.requireCurrentUserId();
      String result = sectMemberService.setNotice(userId, content);
      return new SectToolResponse(true, result);
    } catch (Exception e) {
      log.error("发布公告失败", e);
      return new SectToolResponse(false, e.getMessage());
    }
  }

  @Tool(description = "下架共享功法，使其不可被成员学习")
  @Transactional
  public SectToolResponse removeSharedSkill(@ToolParam(description = "共享功法编号") long sharedSkillId) {
    try {
      Long userId = UserContext.requireCurrentUserId();
      String result = sectSharedSkillService.removeSharedSkill(userId, sharedSkillId);
      return new SectToolResponse(true, result);
    } catch (Exception e) {
      log.error("下架共享功法失败: sharedSkillId={}", sharedSkillId, e);
      return new SectToolResponse(false, e.getMessage());
    }
  }

  @Tool(description = "上架共享功法到宗门功法库，供成员学习")
  @Transactional
  public SectToolResponse publishSharedSkill(@ToolParam(description = "功法编号") long sharedSkillId) {
    try {
      Long userId = UserContext.requireCurrentUserId();
      String result = sectSharedSkillService.listSharedSkill(userId, sharedSkillId);
      return new SectToolResponse(true, result);
    } catch (Exception e) {
      log.error("上架共享功法失败: sharedSkillId={}", sharedSkillId, e);
      return new SectToolResponse(false, e.getMessage());
    }
  }
}
