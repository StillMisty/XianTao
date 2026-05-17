package top.stillmisty.xiantao.domain.masterapprentice.vo;

/** 徒弟信息 VO */
public record ApprenticeInfoVO(
    Long userId, String nickname, Integer level, String realmDisplay, String status) {}
