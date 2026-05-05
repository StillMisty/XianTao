package top.stillmisty.xiantao.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.item.entity.Equipment;
import top.stillmisty.xiantao.domain.item.entity.ItemTemplate;
import top.stillmisty.xiantao.domain.item.entity.StackableItem;
import top.stillmisty.xiantao.domain.item.enums.ItemType;
import top.stillmisty.xiantao.domain.item.repository.EquipmentRepository;
import top.stillmisty.xiantao.domain.item.repository.ItemTemplateRepository;
import top.stillmisty.xiantao.domain.item.repository.StackableItemRepository;
import top.stillmisty.xiantao.domain.item.vo.ItemEntry;

@Component
@RequiredArgsConstructor
public class ItemResolver {

  private final StackableItemRepository stackableItemRepository;
  private final ItemTemplateRepository itemTemplateRepository;
  private final EquipmentRepository equipmentRepository;

  // ===================== Result Types =====================

  private static int tier(ItemTemplate template) {
    return getCropTier(template.getGrowTime() != null ? template.getGrowTime() : 0);
  }

  private static int getCropTier(int growTime) {
    if (growTime <= 24) return 1;
    if (growTime <= 48) return 2;
    if (growTime <= 72) return 3;
    if (growTime <= 120) return 4;
    return 5;
  }

  private static String tierMetadata(ItemTemplate template) {
    return "T" + tier(template);
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

  public ResolveResult<ItemTemplate> resolveSeed(Long userId, String input) {
    return resolveStackable(sortedSeedEntries().apply(userId), input);
  }

  public ResolveResult<ItemTemplate> resolveEgg(Long userId, String input) {
    return resolveStackable(sortedEggEntries().apply(userId), input);
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
            .map(si -> Map.entry(si, templates.get(si.getTemplateId())))
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
