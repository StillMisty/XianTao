package top.stillmisty.xiantao.service.shop;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/** 调货时长价格档位回归：2 / 4 / 6 / 8 / 10 / 12 小时 */
class SpecialOrderServiceTest {

  @Test
  void sourcingHoursFollowPriceTiers() {
    assertEquals(2, SpecialOrderService.sourcingHoursFor(1));
    assertEquals(2, SpecialOrderService.sourcingHoursFor(99));
    assertEquals(4, SpecialOrderService.sourcingHoursFor(100));
    assertEquals(4, SpecialOrderService.sourcingHoursFor(499));
    assertEquals(6, SpecialOrderService.sourcingHoursFor(500));
    assertEquals(6, SpecialOrderService.sourcingHoursFor(1_999));
    assertEquals(8, SpecialOrderService.sourcingHoursFor(2_000));
    assertEquals(8, SpecialOrderService.sourcingHoursFor(9_999));
    assertEquals(10, SpecialOrderService.sourcingHoursFor(10_000));
    assertEquals(10, SpecialOrderService.sourcingHoursFor(49_999));
    assertEquals(12, SpecialOrderService.sourcingHoursFor(50_000));
    assertEquals(12, SpecialOrderService.sourcingHoursFor(999_999));
  }
}
