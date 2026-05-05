package top.stillmisty.xiantao.domain.monster;

import java.util.*;
import lombok.extern.slf4j.Slf4j;
import top.stillmisty.xiantao.domain.monster.enums.BuffType;

/** Buff管理器 管理战斗单位身上的Buff/Debuff */
@Slf4j
public class BuffManager {

  private final Map<Long, List<Buff>> buffsMap = new HashMap<>();

  /** 为战斗单位添加Buff */
  public void addBuff(Long combatantId, Buff buff) {
    List<Buff> buffs = buffsMap.computeIfAbsent(combatantId, k -> new ArrayList<>());

    // 检查是否可以叠加
    Optional<Buff> existingBuff =
        buffs.stream()
            .filter(b -> b.getType() == buff.getType() && b.getSource().equals(buff.getSource()))
            .findFirst();

    if (existingBuff.isPresent()) {
      Buff existing = existingBuff.get();
      if (existing.tryStack()) {
        existing.refreshDuration(buff.getRemainingTurns());
        log.debug("Buff叠加: {} x{}", buff.getType().getName(), existing.getStackCount());
      } else {
        existing.refreshDuration(buff.getRemainingTurns());
        log.debug("Buff刷新: {}", buff.getType().getName());
      }
    } else {
      buffs.add(buff);
      log.debug("添加Buff: {} (持续{}回合)", buff.getType().getName(), buff.getRemainingTurns());
    }
  }

  /** 移除过期的Buff */
  public void removeExpiredBuffs(Long combatantId) {
    List<Buff> buffs = buffsMap.get(combatantId);
    if (buffs != null) {
      buffs.removeIf(Buff::isExpired);
    }
  }

  /** 获取战斗单位的所有Buff */
  public List<Buff> getBuffs(Long combatantId) {
    return buffsMap.getOrDefault(combatantId, List.of());
  }

  /** 获取战斗单位的特定类型Buff */
  public List<Buff> getBuffsByType(Long combatantId, BuffType type) {
    return getBuffs(combatantId).stream().filter(b -> b.getType() == type).toList();
  }

  /** 检查战斗单位是否有控制效果 */
  public boolean hasControl(Long combatantId) {
    return getBuffs(combatantId).stream().anyMatch(Buff::isControl);
  }

  /** 检查战斗单位是否有沉默效果 */
  public boolean hasSilence(Long combatantId) {
    return getBuffsByType(combatantId, BuffType.SILENCE).stream().anyMatch(b -> !b.isExpired());
  }

  /** 计算防御力修正（考虑破甲效果） */
  public double getDefenseModifier(Long combatantId) {
    double modifier = 1.0;
    for (Buff buff : getBuffsByType(combatantId, BuffType.ARMOR_BREAK)) {
      modifier -= buff.getValue() * buff.getStackCount();
    }
    return Math.max(0.1, modifier);
  }

  /** 计算速度修正（考虑减速效果） */
  public double getSpeedModifier(Long combatantId) {
    double modifier = 1.0;
    for (Buff buff : getBuffsByType(combatantId, BuffType.SLOW)) {
      modifier -= buff.getValue();
    }
    return Math.max(0.1, modifier);
  }

  /** 计算攻击力修正（考虑增益效果） */
  public double getAttackModifier(Long combatantId) {
    double modifier = 1.0;
    for (Buff buff : getBuffsByType(combatantId, BuffType.ATTACK_BUFF)) {
      modifier += buff.getValue();
    }
    return modifier;
  }

  /** 计算防御力修正（考虑增益效果） */
  public double getDefenseBonusModifier(Long combatantId) {
    double modifier = 1.0;
    for (Buff buff : getBuffsByType(combatantId, BuffType.DEFENSE_BUFF)) {
      modifier += buff.getValue();
    }
    return modifier;
  }

  /**
   * 处理持续伤害和治疗效果
   *
   * @return 总伤害/治疗量（正值为治疗，负值为伤害）
   */
  public int processOverTimeEffects(Long combatantId) {
    int totalEffect = 0;
    List<Buff> buffs = getBuffs(combatantId);

    for (Buff buff : buffs) {
      if (buff.isExpired()) continue;

      if (buff.getType() == BuffType.DOT) {
        int damage = (int) (buff.getValue() * buff.getStackCount());
        totalEffect -= damage;
        log.debug("持续伤害: {} 造成 {} 伤害", buff.getSource(), damage);
      } else if (buff.getType() == BuffType.HEAL) {
        int heal = (int) (buff.getValue() * buff.getStackCount());
        totalEffect += heal;
        log.debug("持续治疗: {} 恢复 {} 生命", buff.getSource(), heal);
      }

      buff.tick();
    }

    removeExpiredBuffs(combatantId);
    return totalEffect;
  }

  /** 清除战斗单位的所有Buff */
  public void clearAllBuffs(Long combatantId) {
    buffsMap.remove(combatantId);
  }

  /** 清除所有Buff数据 */
  public void clear() {
    buffsMap.clear();
  }
}
