package top.stillmisty.xiantao.service.ai.sect;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/**
 * 邀请成员加入宗门操作结果。
 *
 * <p>仅长老/执事可操作。目标必须是未加入任何宗门的散修。 邀请后目标自动成为宗门弟子（MEMBER）。
 */
public record InviteMemberResponse(@JsonPropertyDescription("被邀请加入的目标道号") String targetNickname) {}
