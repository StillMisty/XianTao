package top.stillmisty.xiantao.domain.sect.vo;

import java.util.List;

/** 宗门总览值对象 */
public record SectOverviewVO(
    String name,
    String verse,
    int level,
    String leaderNickname,
    int memberCount,
    int maxMembers,
    long funds,
    int myContribution,
    String myPosition,
    String description,
    String notice,
    String currentEvent,
    List<MemberEntry> members) {

  /** 成员条目 */
  public record MemberEntry(String positionName, String nickname, int level, boolean isMe) {}
}
