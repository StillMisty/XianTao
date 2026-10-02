package top.stillmisty.xiantao.service.combat;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class TrainingRatesTest {

  @Test
  void efficiencyMultiplierIsCappedAtThree() {
    assertEquals(1.0, TrainingRates.efficiencyMultiplier(0), 1e-9);
    assertEquals(1.5, TrainingRates.efficiencyMultiplier(50), 1e-9);
    assertEquals(3.0, TrainingRates.efficiencyMultiplier(1000), 1e-9);
  }

  @Test
  void levelDecayOnlyAppliesBeyondFiveLevels() {
    assertEquals(1.0, TrainingRates.levelDecayMultiplier(20, 15), 1e-9);
    assertEquals(1.0, TrainingRates.levelDecayMultiplier(15, 15), 1e-9);
    assertEquals(0.96, TrainingRates.levelDecayMultiplier(21, 15), 1e-9);
    assertEquals(0.1, TrainingRates.levelDecayMultiplier(100, 1), 1e-9);
  }

  @Test
  void baseExpPerMinuteUsesTheHigherOfMapLevelAndWisdom() {
    // 地图等级 × 5 = 50 高于 √400 × 12 = 240 时取悟性
    assertEquals(240, TrainingRates.baseExpPerMinute(10, 400));
    // 地图等级 × 5 = 250 高于 √100 × 12 = 120 时取地图
    assertEquals(250, TrainingRates.baseExpPerMinute(50, 100));
  }

  @Test
  void settlementMinutesAreCappedAtTwelveHours() {
    assertEquals(60, TrainingRates.settlementMinutes(60));
    assertEquals(720, TrainingRates.settlementMinutes(720));
    assertEquals(720, TrainingRates.settlementMinutes(1000));
  }
}
