package top.stillmisty.xiantao.service.beast;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.stereotype.Service;
import top.stillmisty.xiantao.domain.beast.entity.Beast;
import top.stillmisty.xiantao.domain.fudi.enums.MutationTrait;

/** 灵兽变异 — 随机特性生成、变异判定、等阶槽位控制 */
@Service
public class BeastMutationService {

  /** 根据灵兽等阶返回最大特性槽数 */
  public static int getMaxSlots(int tier) {
    return switch (tier) {
      case 1, 2 -> 1;
      case 3, 4 -> 2;
      default -> 3;
    };
  }

  public boolean canAddTrait(Beast beast) {
    List<String> traits = beast.getMutationTraits();
    int current = traits != null ? traits.size() : 0;
    return current < getMaxSlots(beast.getTier() != null ? beast.getTier() : 1);
  }

  public String rollRandomTrait() {
    return MutationTrait.values()[
        ThreadLocalRandom.current().nextInt(MutationTrait.values().length)]
        .getCode();
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
    List<String> traits = beast.getMutationTraits();
    if (traits == null) {
      traits = new ArrayList<>();
    }

    int maxSlots = getMaxSlots(beast.getTier() != null ? beast.getTier() : 1);
    if (traits.size() >= maxSlots) return;

    List<String> availableTraits = new ArrayList<>();
    for (MutationTrait trait : MutationTrait.values()) {
      if (!traits.contains(trait.getCode())) {
        availableTraits.add(trait.getCode());
      }
    }
    if (availableTraits.isEmpty()) return;

    String newTrait =
        availableTraits.get(ThreadLocalRandom.current().nextInt(availableTraits.size()));
    traits.add(newTrait);
    beast.setMutationTraits(traits);
  }
}
