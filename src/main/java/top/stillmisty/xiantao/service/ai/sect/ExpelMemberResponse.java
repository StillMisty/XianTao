package top.stillmisty.xiantao.service.ai.sect;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/**
 * 逐出成员操作结果。
 *
 * <p>仅长老/执事可操作。不可逐出同级或职位高于自己的成员。 被逐出者将恢复散修状态。
 */
public record ExpelMemberResponse(@JsonPropertyDescription("被逐出的成员道号") String targetNickname) {}
