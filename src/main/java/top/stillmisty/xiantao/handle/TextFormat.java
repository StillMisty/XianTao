package top.stillmisty.xiantao.handle;

/** 文本格式抽象 纯文本和Markdown只需提供不同的格式化令牌即可统一处理 */
public sealed interface TextFormat {

  TextFormat PLAIN = new PlainFormat();
  TextFormat MARKDOWN = new MarkdownFormat();

  /** 三级标题 (### / 【】) */
  String heading(String text);

  /** 三级标题，markdown 额外显示 emoji */
  String heading(String text, String emoji);

  /** 二级标题 (## / 【】) */
  String subHeading(String text);

  /** 加粗 / 强调 */
  String bold(String text);

  /** 列表项 */
  String listItem(String text);

  /** 分隔符 */
  String separator();

  record PlainFormat() implements TextFormat {
    @Override
    public String heading(String text) {
      return "【" + text + "】\n";
    }

    @Override
    public String heading(String text, String emoji) {
      return heading(text);
    }

    @Override
    public String subHeading(String text) {
      return heading(text);
    }

    @Override
    public String bold(String text) {
      return "【" + text + "】";
    }

    @Override
    public String listItem(String text) {
      return "   " + text + "\n";
    }

    @Override
    public String separator() {
      return "\n";
    }
  }

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
    public String listItem(String text) {
      return "   - " + text + "\n";
    }

    @Override
    public String separator() {
      return "\n---\n";
    }
  }
}
