package top.stillmisty.xiantao.handle.platform;

/** 平台回复能力 — 分段与按钮限制，由各平台适配器声明。 */
public record ReplyLimits(
    int maxSegmentBytes,
    int maxSegments,
    int maxButtonLabelChars,
    boolean choiceButtons,
    boolean suggestionButtons) {}
