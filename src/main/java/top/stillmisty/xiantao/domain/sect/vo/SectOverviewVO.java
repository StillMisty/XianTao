package top.stillmisty.xiantao.domain.sect.vo;

import java.util.List;
import org.jspecify.annotations.Nullable;

/** 宗门总览值对象 */
public record SectOverviewVO(
    String name,
    @Nullable String verse,
    int level,
    String leaderNickname,
    int memberCount,
    int maxMembers,
    long funds,
    int myContribution,
    String myPosition,
    @Nullable String description,
    @Nullable String notice,
    @Nullable String currentEvent,
    List<MemberEntry> members) {

  /** 成员条目 */
  public record MemberEntry(String positionName, String nickname, int level, boolean isMe) {}
}
