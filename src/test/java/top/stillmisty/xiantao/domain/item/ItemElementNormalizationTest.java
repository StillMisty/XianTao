package top.stillmisty.xiantao.domain.item;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import top.stillmisty.xiantao.domain.item.entity.ElementRange;
import top.stillmisty.xiantao.domain.item.entity.ItemProperties;
import top.stillmisty.xiantao.domain.item.entity.StackableItem;
import top.stillmisty.xiantao.domain.pill.enums.ElementType;
import top.stillmisty.xiantao.service.CombinationStrategy;

class ItemElementNormalizationTest {

  @Test
  void herbElementLookupIsCaseInsensitive() {
    StackableItem herb = new StackableItem();
    herb.setProperties(Map.of("elements", Map.of("wood", 1, "water", 1)));

    assertEquals(1, herb.getElementValue(ElementType.WOOD));
    assertEquals(1, herb.getElementValue("WOOD"));
    assertEquals(1, herb.getElementValue("wood"));
    assertEquals(0, herb.getElementValue(ElementType.FIRE));
  }

  @Test
  void recipeRequirementKeysAreNormalizedToUppercase() {
    ItemProperties.Scroll scroll =
        new ItemProperties.Scroll(
            new ItemProperties.Scroll.Recipe(1, 1L, 2, Map.of("metal", new ElementRange(1, 2))));

    assertTrue(scroll.requirements().containsKey("METAL"), scroll.requirements().toString());
  }

  @Test
  void forgingRequirementKeysStayUppercase() {
    ItemProperties.ForgingBlueprint blueprint =
        new ItemProperties.ForgingBlueprint(1L, 1, Map.of("RIGIDITY", new ElementRange(1, 5)));

    assertTrue(blueprint.requirements().containsKey("RIGIDITY"));
  }

  @Test
  void normalizedKeysMatchTotalsBuiltFromEnumCodes() {
    CombinationStrategy strategy =
        new CombinationStrategy(
            List.of("METAL", "WOOD", "WATER", "FIRE", "EARTH"), 5, (item, attribute) -> 0);
    Map<String, ElementRange> requirements = Map.of("METAL", new ElementRange(1, 2));
    Map<String, Integer> totals = Map.of("METAL", 2);

    assertTrue(strategy.matchesRequirements(totals, requirements));
    assertEquals(0, strategy.collectMissingAttributes(requirements, totals).size());
  }
}
