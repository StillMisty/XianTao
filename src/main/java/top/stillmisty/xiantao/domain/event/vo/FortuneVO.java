package top.stillmisty.xiantao.domain.event.vo;

import top.stillmisty.xiantao.domain.event.enums.FortuneLevel;

public record FortuneVO(int wealth, int fate, int luck, FortuneLevel level, String comment) {}
