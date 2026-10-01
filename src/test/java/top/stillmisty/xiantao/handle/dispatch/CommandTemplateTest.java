package top.stillmisty.xiantao.handle.dispatch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.junit.jupiter.api.Test;

class CommandTemplateTest {

  @Test
  void defaultPlaceholderIsGreedyDotPlus() {
    CommandTemplate template = CommandTemplate.compile("炼制\\s*{{itemName}}");
    assertTrue(template.matches("炼制 破旧木剑"));
    assertTrue(template.matches("炼制 破旧木剑 x"));
    assertEquals(Map.of("itemName", "破旧木剑 x"), template.extract("炼制 破旧木剑 x"));
    assertEquals(java.util.List.of("itemName"), template.argNames());
  }

  @Test
  void customRegexIsUsed() {
    CommandTemplate template =
        CommandTemplate.compile(
            "GM给物品\\s*{{nickname,\\S+}}\\s+{{itemName,\\S+}}\\s+{{quantity,\\d+}}");
    assertTrue(template.matches("GM给物品 张三 灵石 100"));
    Map<String, String> args = template.extract("GM给物品 张三 灵石 100");
    assertEquals("张三", args.get("nickname"));
    assertEquals("灵石", args.get("itemName"));
    assertEquals("100", args.get("quantity"));
    assertFalse(template.matches("GM给物品 张三 灵石"));
    assertFalse(template.matches("GM给物品 张三 灵石 abc"));
  }

  @Test
  void matchingIsFullMatch() {
    CommandTemplate template = CommandTemplate.compile("状态");
    assertTrue(template.matches("状态"));
    assertFalse(template.matches("状态 查询"));
    assertFalse(template.matches("查看状态"));
  }

  @Test
  void literalTemplateQuotesRegexMetacharacters() {
    CommandTemplate template = CommandTemplate.compile("使用 (测试)");
    assertTrue(template.matches("使用 (测试)"));
    assertFalse(template.matches("使用 测试"));
  }

  @Test
  void negativeLookaheadBehavesLikeSimbot() {
    CommandTemplate template = CommandTemplate.compile("炼(?!方)\\s*{{herbInput,.+}}");
    assertTrue(template.matches("炼制 三叶草"));
    assertTrue(template.matches("炼 三叶草"));
    assertFalse(template.matches("炼方 三叶草"));
  }

  @Test
  void nestedBracesInCustomRegexAreSupported() {
    CommandTemplate template = CommandTemplate.compile("{{code,\\d{2,4}}}");
    assertTrue(template.matches("123"));
    assertTrue(template.matches("1234"));
    assertFalse(template.matches("1"));
    assertEquals("123", template.extract("123").get("code"));
  }

  @Test
  void rejectsInvalidTemplates() {
    assertThrows(IllegalArgumentException.class, () -> CommandTemplate.compile("  "));
    assertThrows(IllegalArgumentException.class, () -> CommandTemplate.compile("炼制 {{itemName"));
    assertThrows(IllegalArgumentException.class, () -> CommandTemplate.compile("{{1bad}}"));
    assertThrows(IllegalArgumentException.class, () -> CommandTemplate.compile("{{a}}{{a}}"));
    assertThrows(IllegalArgumentException.class, () -> CommandTemplate.compile("{{name,}}"));
  }
}
