package top.stillmisty.xiantao.service.ai.sect;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/**
 * 任免成员职位操作结果。
 *
 * <p>仅宗主可操作。设置目标成员的宗门职位：ELDER（长老/执事）或 MEMBER（弟子）。 不可任免职位高于自己的成员。
 */
public record AppointMemberResponse(
    @JsonPropertyDescription("被任免的成员道号") String targetNickname,
    @JsonPropertyDescription("新职位代码：ELDER=长老/执事, MEMBER=弟子") String positionCode) {}
