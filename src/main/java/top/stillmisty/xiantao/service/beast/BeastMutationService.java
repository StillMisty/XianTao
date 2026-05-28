package top.stillmisty.xiantao.service.beast;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.stillmisty.xiantao.domain.beast.entity.Beast;
import top.stillmisty.xiantao.domain.beast.entity.MutationTraitConfig;
import top.stillmisty.xiantao.domain.beast.repository.MutationTraitConfigRepository;
import top.stillmisty.xiantao.domain.fudi.enums.BeastQuality;
import top.stillmisty.xiantao.domain.item.entity.ItemTemplate;
import top.stillmisty.xiantao.domain.item.repository.ItemTemplateRepository;

/** 灵兽变异 — 随机特性生成、变异判定、等阶槽位控制 */
@Service
@RequiredArgsConstructor
public class BeastMutationService {

  private final MutationTraitConfigRepository traitConfigRepository;
  private final ItemTemplateRepository itemTemplateRepository;

  /** 根据灵兽等阶返回最大特性槽数 */
  public static int getMaxSlots(int tier) {
    return switch (tier) {
      case 1, 2 -> 3;
      case 3, 4 -> 4;
      default -> 5;
    };
  }

  public boolean canAddTrait(Beast beast) {
    Set<Long> traits = beast.getMutationTraits();
    int current = traits != null ? traits.size() : 0;
    return current < getMaxSlots(beast.getTier() != null ? beast.getTier() : 1);
  }

  public Long rollRandomTrait(Beast beast) {
    List<String> beastTags = getBeastTags(beast.getTemplateId());
    BeastQuality quality = beast.getQuality() != null ? beast.getQuality() : BeastQuality.MORTAL;

    List<MutationTraitConfig> available =
        traitConfigRepository.findAvailableForBeast(beastTags, quality);

    if (available.isEmpty()) return null;
    return available.get(ThreadLocalRandom.current().nextInt(available.size())).getId();
  }

  public void attemptMutation(Beast beast, int chancePercent) {
    if (!canAddTrait(beast)) return;
    if (rollMutation(chancePercent)) {
      addMutationTrait(beast);
    }
  }

  private boolean rollMutation(int chancePercent) {
    return ThreadLocalRandom.current().nextInt(100) < chancePercent;
  }

  private void addMutationTrait(Beast beast) {
    Set<Long> currentTraits = beast.getMutationTraits();
    if (currentTraits == null) {
      currentTraits = new LinkedHashSet<>();
      beast.setMutationTraits(currentTraits);
    }

    int maxSlots = getMaxSlots(beast.getTier() != null ? beast.getTier() : 1);
    if (currentTraits.size() >= maxSlots) return;

    List<String> beastTags = getBeastTags(beast.getTemplateId());
    BeastQuality quality = beast.getQuality() != null ? beast.getQuality() : BeastQuality.MORTAL;

    List<MutationTraitConfig> available =
        traitConfigRepository.findAvailableForBeast(beastTags, quality);

    Set<Long> traits = currentTraits;
    List<Long> availableIds =
        available.stream()
            .map(MutationTraitConfig::getId)
            .filter(id -> !traits.contains(id))
            .toList();

    if (availableIds.isEmpty()) return;

    Long newTrait = availableIds.get(ThreadLocalRandom.current().nextInt(availableIds.size()));
    currentTraits.add(newTrait);
  }

  private List<String> getBeastTags(Long templateId) {
    return itemTemplateRepository
        .findById(templateId)
        .map(ItemTemplate::getTags)
        .map(tags -> List.copyOf(tags))
        .orElse(List.of());
  }
}
