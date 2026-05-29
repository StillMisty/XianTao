package top.stillmisty.xiantao.service.inventory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.item.entity.Equipment;
import top.stillmisty.xiantao.domain.item.entity.ItemTemplate;
import top.stillmisty.xiantao.domain.item.entity.StackableItem;
import top.stillmisty.xiantao.domain.item.enums.ItemType;
import top.stillmisty.xiantao.domain.item.enums.MaterialAttribute;
import top.stillmisty.xiantao.domain.item.vo.ItemEntry;
import top.stillmisty.xiantao.domain.pill.enums.ElementType;
import top.stillmisty.xiantao.domain.pill.enums.PillQuality;
import top.stillmisty.xiantao.infrastructure.repository.EquipmentRepository;
import top.stillmisty.xiantao.infrastructure.repository.ItemTemplateRepository;
import top.stillmisty.xiantao.infrastructure.repository.StackableItemRepository;
import top.stillmisty.xiantao.service.fudi.FudiHelper;

@Component
@RequiredArgsConstructor
public class ItemResolver {

  private final StackableItemRepository stackableItemRepository;
  private final ItemTemplateRepository itemTemplateRepository;
  private final EquipmentRepository equipmentRepository;

  // ===================== Result Types =====================

  private int tier(ItemTemplate template) {
    return FudiHelper.getCropTier(template.getGrowTime() != null ? template.getGrowTime() : 0);
  }

  private String tierMetadata(ItemTemplate template) {
    return top.stillmisty.xiantao.domain.beast.entity.Beast.getTierName(tier(template));
  }

  public List<ItemEntry> listSeeds(Long userId) {
    return sortedSeedEntries().apply(userId).stream()
        .map(
            e ->
                new ItemEntry(
                    e.index,
                    e.stackable.getTemplateId(),
                    e.stackable.getName(),
                    e.stackable().getQuantity(),
                    tierMetadata(e.template)))
        .toList();
  }

  // ===================== Public API =====================

  public List<ItemEntry> listEggs(Long userId) {
    return sortedEggEntries().apply(userId).stream()
        .map(
            e ->
                new ItemEntry(
                    e.index,
                    e.stackable.getTemplateId(),
                    e.stackable.getName(),
                    e.stackable().getQuantity(),
                    tierMetadata(e.template)))
        .toList();
  }

  public List<ItemEntry> listEquipment(Long userId) {
    return sortedEquipmentEntries(userId).stream()
        .map(
            e ->
                new ItemEntry(
                    e.index, e.item.getId(), e.item.getName(), 1, e.item.getRarity().getName()))
        .toList();
  }

  public List<ItemEntry> listItems(Long userId, ItemType type) {
    var items = stackableItemRepository.findByUserIdAndType(userId, type);
    var result = new ArrayList<ItemEntry>();
    for (int i = 0; i < items.size(); i++) {
      var item = items.get(i);
      String metadata =
          switch (type) {
            case MATERIAL -> formatMaterialProperties(item);
            case HERB -> formatHerbProperties(item);
            default -> "";
          };
      result.add(new ItemEntry(i + 1, item.getId(), item.getName(), item.getQuantity(), metadata));
    }
    return result;
  }

  private String formatMaterialProperties(StackableItem item) {
    var sb = new StringBuilder();
    for (var attr : MaterialAttribute.values()) {
      int value = item.getMaterialValue(attr);
      if (value > 0) {
        if (!sb.isEmpty()) sb.append(" ");
        sb.append(attr.getName()).append(":").append(value);
      }
    }
    return sb.toString();
  }

  private String formatHerbProperties(StackableItem item) {
    var sb = new StringBuilder();
    for (var elem : ElementType.values()) {
      int value = item.getElementValue(elem);
      if (value > 0) {
        if (!sb.isEmpty()) sb.append(" ");
        sb.append(elem.getName()).append(":").append(value);
      }
    }
    return sb.toString();
  }

  public ResolveResult<ItemTemplate> resolveSeed(Long userId, String input) {
    return resolveStackable(sortedSeedEntries().apply(userId), input);
  }

  public ResolveResult<ItemTemplate> resolveEgg(Long userId, String input) {
    return resolveStackable(sortedEggEntries().apply(userId), input);
  }

  public ResolveResult<StackableItem> resolveStackableItem(Long userId, String input) {
    var exactMatches = stackableItemRepository.findByUserIdAndName(userId, input);
    if (exactMatches.size() == 1) return new Found<>(exactMatches.getFirst(), 0);
    if (!exactMatches.isEmpty()) {
      var candidates = exactMatches.stream().map(item -> buildAmbiguityEntry(item, 0)).toList();
      return new Ambiguous<>(input, candidates);
    }

    var fuzzyMatches = stackableItemRepository.findByUserIdAndNameContaining(userId, input);
    if (fuzzyMatches.isEmpty()) return new NotFound<>(input);
    if (fuzzyMatches.size() == 1) return new Found<>(fuzzyMatches.getFirst(), 0);

    var candidates = fuzzyMatches.stream().map(item -> buildAmbiguityEntry(item, 0)).toList();
    return new Ambiguous<>(input, candidates);
  }

  private ItemEntry buildAmbiguityEntry(StackableItem item, int index) {
    var meta = new StringBuilder();
    Object gradeObj = item.getProperties() != null ? item.getProperties().get("grade") : null;
    if (gradeObj instanceof Number n) {
      meta.append(n.intValue()).append("级 ");
    }
    if (item.getQuality() != null && !item.getQuality().isEmpty()) {
      try {
        meta.append(PillQuality.fromCode(item.getQuality()).getChineseName());
      } catch (IllegalArgumentException e) {
        meta.append(item.getQuality());
      }
    }
    return new ItemEntry(index, item.getId(), item.getName(), item.getQuantity(), meta.toString());
  }

  public ResolveResult<Equipment> resolveEquipment(Long userId, String input) {
    var entries = sortedEquipmentEntries(userId);
    if (input.matches("\\d+")) {
      int idx = Integer.parseInt(input);
      for (var e : entries) {
        if (e.index == idx) return new Found<>(e.item, idx);
      }
    }
    var exact = entries.stream().filter(e -> e.item.getName().equals(input)).toList();
    if (exact.size() == 1) return new Found<>(exact.getFirst().item, exact.getFirst().index);

    var partial = entries.stream().filter(e -> e.item.getName().contains(input)).toList();
    if (partial.isEmpty()) return new NotFound<>(input);
    if (partial.size() == 1) return new Found<>(partial.getFirst().item, partial.getFirst().index);

    var candidates =
        partial.stream()
            .map(
                e ->
                    new ItemEntry(
                        e.index, e.item.getId(), e.item.getName(), 1, e.item.getRarity().getName()))
            .toList();
    return new Ambiguous<>(input, candidates);
  }

  private List<SeedEntry> sortedStackableEntries(Long userId, ItemType type) {
    var items = stackableItemRepository.findByUserIdAndType(userId, type);
    if (items.isEmpty()) return List.of();

    var templates =
        itemTemplateRepository.findByType(type).stream()
            .collect(Collectors.toMap(ItemTemplate::getId, t -> t));

    var sorted =
        items.stream()
            .map(
                si -> {
                  ItemTemplate tmpl = templates.get(si.getTemplateId());
                  if (tmpl == null) return null;
                  return Map.entry(si, tmpl);
                })
            .filter(Objects::nonNull)
            .sorted(
                (a, b) -> {
                  int cmp = Integer.compare(tier(b.getValue()), tier(a.getValue()));
                  return cmp != 0 ? cmp : a.getValue().getName().compareTo(b.getValue().getName());
                })
            .toList();

    var result = new ArrayList<SeedEntry>();
    for (int i = 0; i < sorted.size(); i++) {
      result.add(new SeedEntry(i + 1, sorted.get(i).getKey(), sorted.get(i).getValue()));
    }
    return result;
  }

  // ===================== Internal Structs =====================

  private ResolveResult<ItemTemplate> resolveStackable(List<SeedEntry> entries, String input) {
    if (input.matches("\\d+")) {
      int idx = Integer.parseInt(input);
      for (var e : entries) {
        if (e.index == idx) return new Found<>(e.template, idx);
      }
    }
    var exact = entries.stream().filter(e -> e.template.getName().equals(input)).toList();
    if (exact.size() == 1) return new Found<>(exact.getFirst().template, exact.getFirst().index);

    var partial = entries.stream().filter(e -> e.template.getName().contains(input)).toList();
    if (partial.isEmpty()) return new NotFound<>(input);
    if (partial.size() == 1)
      return new Found<>(partial.getFirst().template, partial.getFirst().index);

    var candidates =
        partial.stream()
            .map(
                e ->
                    new ItemEntry(
                        e.index,
                        e.template.getId(),
                        e.template.getName(),
                        e.stackable.getQuantity(),
                        tierMetadata(e.template)))
            .toList();
    return new Ambiguous<>(input, candidates);
  }

  private SortedEntries sortedSeedEntries() {
    return userId -> sortedStackableEntries(userId, ItemType.SEED);
  }

  private SortedEntries sortedEggEntries() {
    return userId -> sortedStackableEntries(userId, ItemType.BEAST_EGG);
  }

  private List<EquipmentEntry> sortedEquipmentEntries(Long userId) {
    var sorted =
        equipmentRepository.findUnequippedByUserId(userId).stream()
            .sorted(
                (a, b) -> {
                  int cmp = Integer.compare(b.getRarity().ordinal(), a.getRarity().ordinal());
                  if (cmp != 0) return cmp;
                  cmp = Integer.compare(b.getForgeLevel(), a.getForgeLevel());
                  if (cmp != 0) return cmp;
                  return a.getName().compareTo(b.getName());
                })
            .toList();

    var result = new ArrayList<EquipmentEntry>();
    for (int i = 0; i < sorted.size(); i++) {
      result.add(new EquipmentEntry(i + 1, sorted.get(i)));
    }
    return result;
  }

  // ===================== Seeds / Eggs =====================

  public sealed interface ResolveResult<T> {}

  private interface SortedEntries {
    List<SeedEntry> apply(Long userId);
  }

  public record Found<T>(T item, int index) implements ResolveResult<T> {}

  // ===================== Equipment =====================

  public record NotFound<T>(String input) implements ResolveResult<T> {}

  // ===================== Tier Helpers =====================

  public record Ambiguous<T>(String input, List<ItemEntry> candidates)
      implements ResolveResult<T> {}

  private record SeedEntry(int index, StackableItem stackable, ItemTemplate template) {}

  private record EquipmentEntry(int index, Equipment item) {}
}
