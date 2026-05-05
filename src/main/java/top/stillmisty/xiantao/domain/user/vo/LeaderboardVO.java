package top.stillmisty.xiantao.domain.user.vo;

import java.util.List;

public record LeaderboardVO(String title, List<LeaderboardEntryVO> entries, boolean showLevel) {}
