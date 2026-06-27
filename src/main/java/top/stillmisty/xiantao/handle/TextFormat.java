package top.stillmisty.xiantao.handle;

/** 文本格式抽象 */
public interface TextFormat {

  static TextFormat get() {
    return MarkdownHolder.INSTANCE;
  }

  class MarkdownHolder {
    static final TextFormat INSTANCE = new MarkdownFormat();
  }

  /** 三级标题 */
  String heading(String text);

  /** 三级标题，额外显示 emoji */
  String heading(String text, String emoji);

  /** 二级标题 */
  String subHeading(String text);

  /** 加粗 */
  String bold(String text);

  /** 斜体 */
  String italic(String text);

  /** 删除线 */
  String strikethrough(String text);

  /** 列表项 */
  String listItem(String text);

  /** 分隔符 */
  String separator();

  /** 子列表项 */
  String subListItem(String text);

  /** 表情符号 */
  String emoji(String code, String fallbackText);

  /** 提示文本 */
  String tip(String text);

  /** 位置状态文本 */
  default String locationStatus(Boolean isInSameLocation) {
    boolean same = Boolean.TRUE.equals(isInSameLocation);
    return same ? emoji("📍", "") + "同地点" : emoji("📌", "") + "异地";
  }

  /** 错误前缀 */
  String error(String text);

  record MarkdownFormat() implements TextFormat {
    @Override
    public String heading(String text) {
      return "### " + text + "\n";
    }

    @Override
    public String heading(String text, String emoji) {
      return "### " + emoji + " " + text + "\n";
    }

    @Override
    public String subHeading(String text) {
      return "## " + text + "\n";
    }

    @Override
    public String bold(String text) {
      return "**" + text + "**";
    }

    @Override
    public String italic(String text) {
      return "*" + text + "*";
    }

    @Override
    public String strikethrough(String text) {
      return "~~" + text + "~~";
    }

    @Override
    public String listItem(String text) {
      return "   - " + text + "\n";
    }

    @Override
    public String separator() {
      return "\n---\n\n";
    }

    @Override
    public String subListItem(String text) {
      return "  - " + text + "\n";
    }

    @Override
    public String emoji(String code, String fallbackText) {
      return code;
    }

    @Override
    public String tip(String text) {
      return "> 💡 " + text + "\n";
    }

    @Override
    public String error(String text) {
      return "❌ " + text;
    }
  }
}
