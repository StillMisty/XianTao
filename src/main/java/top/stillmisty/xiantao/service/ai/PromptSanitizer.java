package top.stillmisty.xiantao.service.ai;

/** 玩家可控文本进入 LLM prompt 前的净化：剥离控制字符与换行（防伪指令注入），截断至安全长度。 */
public final class PromptSanitizer {

  private PromptSanitizer() {}

  public static String sanitize(String text, int maxLength) {
    String cleaned = text.replaceAll("[\\p{Cntrl}\\n\\r\\t]", " ").trim();
    return cleaned.length() > maxLength ? cleaned.substring(0, maxLength) : cleaned;
  }
}
