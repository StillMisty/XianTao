package top.stillmisty.xiantao.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.stereotype.Service;
import top.stillmisty.xiantao.domain.beast.entity.Beast;
import top.stillmisty.xiantao.domain.fudi.enums.MutationTrait;

/** 灵兽变异 — 随机特性生成、变异判定 */
@Service
public class BeastMutationService {

  public String rollRandomTrait() {
    return MutationTrait.values()[
        ThreadLocalRandom.current().nextInt(MutationTrait.values().length)]
        .getCode();
  }

  public void attemptMutation(Beast beast, int chancePercent) {
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
    if (traits.size() >= 2) return;
    String newTrait = rollRandomTrait();
    if (!traits.contains(newTrait)) {
      traits.add(newTrait);
      beast.setMutationTraits(traits);
      beast.setIsMutant(true);
    }
  }
}
