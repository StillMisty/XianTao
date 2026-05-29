package top.stillmisty.xiantao.service.ai;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.sect.vo.SkillOperationResultVO;
import top.stillmisty.xiantao.service.UserContext;
import top.stillmisty.xiantao.service.ai.sect.*;
import top.stillmisty.xiantao.service.sect.SectMemberService;
import top.stillmisty.xiantao.service.sect.SectSharedSkillService;

/**
 * 宗门长老/执事专属工具（权限高于普通弟子）。
 *
 * <p>仅有 ELDER 或 LEADER 职位时才会注册这些工具。 与 {@link SectMemberTools} 组合使用。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SectElderTools {

  private final ToolExecutor toolExecutor;
  private final SectMemberService sectMemberService;
  private final SectSharedSkillService sectSharedSkillService;

  /**
   * 邀请散修加入宗门。
   *
   * <p>仅长老/执事可操作。目标必须是未加入任何宗门的散修。 若目标已有所属宗门，操作会被拒绝。
   *
   * @param targetNickname 目标道号（玩家昵称）
   */
  @Tool(description = "邀请散修加入宗门。目标必须未加入任何宗门，否则操作被拒绝")
  @Transactional
  public InviteMemberResponse inviteMember(@ToolParam(description = "目标道号") String targetNickname) {
    return toolExecutor.execute(
        "inviteMember",
        () -> {
          Long userId = UserContext.requireCurrentUserId();
          sectMemberService.inviteMemberInternal(userId, targetNickname);
          return new InviteMemberResponse(targetNickname);
        });
  }

  /**
   * 将成员逐出宗门。
   *
   * <p>仅长老/执事可操作。不可逐出职位高于或等于自己的成员。 被逐出者将恢复散修状态。
   *
   * @param targetNickname 目标道号
   */
  @Tool(description = "逐出宗门成员。不可逐出职位高于或等于自己的成员")
  @Transactional
  public ExpelMemberResponse expelMember(
      @ToolParam(description = "要逐出的成员道号") String targetNickname) {
    return toolExecutor.execute(
        "expelMember",
        () -> {
          Long userId = UserContext.requireCurrentUserId();
          sectMemberService.kickMemberInternal(userId, targetNickname);
          return new ExpelMemberResponse(targetNickname);
        });
  }

  /**
   * 发布宗门公告，全员可见。
   *
   * <p>仅长老/执事可操作。新公告覆盖旧公告，建议简洁明了。
   *
   * @param content 公告正文
   */
  @Tool(description = "发布宗门公告，全员可见，覆盖旧公告")
  @Transactional
  public PostNoticeResponse postNotice(@ToolParam(description = "公告内容") String content) {
    return toolExecutor.execute(
        "postNotice",
        () -> {
          Long userId = UserContext.requireCurrentUserId();
          sectMemberService.setNoticeInternal(userId, content);
          return new PostNoticeResponse();
        });
  }

  /**
   * 从功法库下架指定共享功法。
   *
   * <p>仅长老/执事可操作。下架后弟子无法学习该功法。
   *
   * @param sharedSkillId 共享功法编号（从 checkSharedSkills 返回的 skills 列表中获取）
   */
  @Tool(description = "从功法库下架共享功法。下架后弟子无法学习此功法")
  @Transactional
  public RemoveSharedSkillResponse removeSharedSkill(
      @ToolParam(description = "共享功法编号") long sharedSkillId) {
    return toolExecutor.execute(
        "removeSharedSkill",
        () -> {
          Long userId = UserContext.requireCurrentUserId();
          SkillOperationResultVO r =
              sectSharedSkillService.removeSharedSkillInternal(userId, sharedSkillId);
          return new RemoveSharedSkillResponse(sharedSkillId, r.skillName());
        });
  }

  /**
   * 将待上架功法发布到功法库，供弟子学习。
   *
   * <p>仅长老/执事可操作。待上架的功法来自弟子献上的功法玉简。
   *
   * @param sharedSkillId 共享功法编号（来自 checkSharedSkills，status 为 PENDING 的功法）
   */
  @Tool(description = "将待上架功法发布到功法库，供弟子学习。编号来自 checkSharedSkills 中的待上架功法")
  @Transactional
  public PublishSharedSkillResponse publishSharedSkill(
      @ToolParam(description = "待上架的共享功法编号") long sharedSkillId) {
    return toolExecutor.execute(
        "publishSharedSkill",
        () -> {
          Long userId = UserContext.requireCurrentUserId();
          SkillOperationResultVO r =
              sectSharedSkillService.listSharedSkillInternal(userId, sharedSkillId);
          return new PublishSharedSkillResponse(sharedSkillId, r.skillName());
        });
  }
}
