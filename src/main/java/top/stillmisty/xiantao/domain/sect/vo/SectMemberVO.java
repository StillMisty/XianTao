package top.stillmisty.xiantao.domain.sect.vo;

import top.stillmisty.xiantao.domain.sect.enums.SectPosition;

public record SectMemberVO(
    Long userId,
    String nickname,
    Integer level,
    String realmDisplay,
    SectPosition position,
    Integer contribution) {}
