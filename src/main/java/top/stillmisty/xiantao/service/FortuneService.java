package top.stillmisty.xiantao.service;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import top.stillmisty.xiantao.domain.event.enums.FortuneLevel;
import top.stillmisty.xiantao.domain.event.vo.FortuneVO;
import top.stillmisty.xiantao.infrastructure.util.TimeUtil;

@Slf4j
@Service
public class FortuneService {

  private static final int BAR_COUNT = 10;

  public ServiceResult<FortuneVO> getFortune(Long userId) {
    return new ServiceResult.Success<>(calculate(userId));
  }

  @Cacheable(cacheNames = "fortunes", key = "#userId + '-' + T(java.time.LocalDate).now()")
  public FortuneVO calculate(Long userId) {
    long[] seeds = generateSeeds(userId, TimeUtil.today());
    int wealth = (int) (Math.abs(seeds[0]) % 101);
    int fate = (int) (Math.abs(seeds[1]) % 101);
    int luck = (int) (Math.abs(seeds[2]) % 101);

    FortuneLevel level = determineLevel(wealth, fate, luck);
    String comment = generateComment(wealth, fate, luck, level);

    return new FortuneVO(wealth, fate, luck, level, comment);
  }

  public String buildDisplay(Long userId) {
    return buildDisplay(calculate(userId));
  }

  public String buildDisplay(FortuneVO fortune) {
    StringBuilder sb = new StringBuilder();
    sb.append("\n════ 今日运势 ════\n");
    sb.append(formatBar("财运", fortune.wealth())).append("\n");
    sb.append(formatBar("机缘", fortune.fate())).append("\n");
    sb.append(formatBar("气运", fortune.luck())).append("\n");
    sb.append("点评：").append(fortune.level().getDisplay());
    if (!fortune.comment().isEmpty()) {
      sb.append(" · ").append(fortune.comment());
    }
    sb.append("\n══════════════════");
    return sb.toString();
  }

  public double getWealthMultiplier(int wealth) {
    return 0.85 + wealth / 333.0;
  }

  public double getFateMultiplier(int fate) {
    return 0.7 + fate / 166.0;
  }

  public double getLuckMultiplier(int luck) {
    return 0.8 + luck / 250.0;
  }

  public int getMonsterLevelOffset(int luck) {
    return Math.clamp((50 - luck) / 30, -3, 3);
  }

  private String formatBar(String label, int value) {
    int filled = (int) Math.round(value / 10.0);
    filled = Math.clamp(filled, 0, BAR_COUNT);
    String bar = "█".repeat(filled) + "░".repeat(BAR_COUNT - filled);
    return String.format("%s %s %2d  %s", label, bar, value, describeDimension(label, value));
  }

  private String describeDimension(String label, int value) {
    if (value >= 95) return label + "极旺";
    if (value >= 80) return label + "旺盛";
    if (value >= 60) return label + "渐旺";
    if (value >= 40) return "中规中矩";
    if (value >= 20) return label + "不济";
    return label + "衰微";
  }

  private FortuneLevel determineLevel(int wealth, int fate, int luck) {
    if (wealth >= 80 && fate >= 80 && luck >= 80) return FortuneLevel.GREAT_FORTUNE;
    if (wealth < 40 && fate < 40 && luck < 40) return FortuneLevel.GREAT_MISFORTUNE;

    int max = Math.max(wealth, Math.max(fate, luck));
    int min = Math.min(wealth, Math.min(fate, luck));

    if (max >= 80 && min >= 45) return FortuneLevel.GOOD_FORTUNE;
    if (min < 25) return FortuneLevel.BAD_FORTUNE;
    return FortuneLevel.NEUTRAL;
  }

  private String generateComment(int wealth, int fate, int luck, FortuneLevel level) {
    List<String> goodParts = new ArrayList<>();
    List<String> badParts = new ArrayList<>();

    if (wealth >= 80) goodParts.add("财源广进");
    else if (wealth < 30) badParts.add("财运不佳");

    if (fate >= 80) goodParts.add("机缘天降");
    else if (fate < 30) badParts.add("机缘未至");

    if (luck >= 80) goodParts.add("气运加身");
    else if (luck < 30) badParts.add("气运低迷");

    return switch (level) {
      case GREAT_FORTUNE -> "万事顺遂，宜大胆行事";
      case GOOD_FORTUNE -> goodParts.isEmpty() ? "运势尚可" : String.join("，", goodParts);
      case BAD_FORTUNE -> badParts.isEmpty() ? "诸事小心" : String.join("，", badParts) + "，谨言慎行";
      case GREAT_MISFORTUNE -> "诸事不宜，宜闭关静修";
      case NEUTRAL ->
          goodParts.isEmpty()
              ? "吉凶参半，随缘即可"
              : goodParts.size() >= 2 ? "彼消此长，贵在坚持" : String.join("，", goodParts) + "但亦有隐忧";
    };
  }

  private long[] generateSeeds(Long userId, LocalDate date) {
    String input = userId + "-" + date;
    try {
      MessageDigest md = MessageDigest.getInstance("MD5");
      byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
      return new long[] {
        ByteBuffer.wrap(digest, 0, 4).getInt(),
        ByteBuffer.wrap(digest, 4, 4).getInt(),
        ByteBuffer.wrap(digest, 8, 4).getInt(),
      };
    } catch (NoSuchAlgorithmException e) {
      int h = input.hashCode();
      return new long[] {h, h * 31L, h * 127L};
    }
  }
}
