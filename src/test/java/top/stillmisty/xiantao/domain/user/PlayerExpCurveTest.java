package top.stillmisty.xiantao.domain.user;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import top.stillmisty.xiantao.domain.user.entity.Player;

/** 修为需求曲线标定回归（长线挂机构型，目标见 tools/balance/README.md）。 */
class PlayerExpCurveTest {

  private static Player playerAtLevel(int level) {
    return Player.create().setLevel(level);
  }

  @Test
  void expRequirementFollowsCalibratedCurve() {
    // 圈定曲线锚点：240 × 等级^2.2，四舍五入
    assertEquals(240, playerAtLevel(1).calculateExpToNextLevel());
    assertEquals(1_103, playerAtLevel(2).calculateExpToNextLevel());
    assertEquals(38_037, playerAtLevel(10).calculateExpToNextLevel());
    assertEquals(458_360, playerAtLevel(31).calculateExpToNextLevel());
    assertEquals(2_837_786, playerAtLevel(71).calculateExpToNextLevel());
    assertEquals(6_028_527, playerAtLevel(100).calculateExpToNextLevel());
    assertEquals(7_584_410, playerAtLevel(111).calculateExpToNextLevel());
  }

  @Test
  void storageIsFiveTimesRequirement() {
    assertEquals(38_037L * 5, playerAtLevel(10).calculateMaxExpStorage());
  }

  @Test
  void expInCurrentLevelIsTotalMinusPreviousRequirement() {
    Player player = Player.create().setLevel(10).setExp(50_000L);

    // 50,000 − 30,168（到达 10 级所需修为，即 9 级需求） = 19,832
    assertEquals(19_832, player.getExpInCurrentLevel());
  }

  @Test
  void addExpRespectsStorageCapWithNewCurve() {
    Player player = Player.create().setLevel(2).setExp(1_103L);
    player.addExp(1_000_000);

    // 级内上限 = 5 × 1,103 = 5,515，当前级内已有 1,103 − 240 = 863
    // 可再存 4,652 → 总修为 1,103 + 4,652 = 5,755
    assertEquals(5_755, player.getExp());
  }
}
