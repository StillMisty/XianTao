package top.stillmisty.xiantao.service.combat;

import java.util.EnumMap;
import java.util.Map;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.monster.Buff;
import top.stillmisty.xiantao.domain.monster.Combatant;
import top.stillmisty.xiantao.domain.monster.enums.BuffType;
import top.stillmisty.xiantao.domain.skill.enums.EffectType;

/**
 * EffectType → EffectHandler 注册表
 *
 * <p>新增效果类型只需在此注册新 handler，无需修改 {@link DefaultCombatEngine}。
 */
@Component
public class EffectHandlerRegistry {

  private final Map<EffectType, EffectHandler> handlers = new EnumMap<>(EffectType.class);

  public EffectHandlerRegistry() {
    register(EffectType.DAMAGE, this::handleDamage);
    register(EffectType.AOE_DAMAGE, this::handleAoeDamage);
    register(EffectType.MULTI_HIT, this::handleMultiHit);
    register(EffectType.ARMOR_BREAK, this::handleArmorBreak);
    register(EffectType.SLOW, this::handleSlow);
    register(EffectType.DOT, this::handleDot);
    register(EffectType.DODGE, this::handleDodge);
    register(EffectType.COUNTER, this::handleCounter);
    register(EffectType.REFLECT, this::handleReflect);
    register(EffectType.CLEANSE, this::handleCleanse);
    register(EffectType.EXECUTE, this::handleExecute);
    register(EffectType.LIFESTEAL, this::handleLifesteal);
    register(EffectType.HEAL, this::handleHeal);
    register(EffectType.ATTACK_BUFF, this::handleAttackBuff);
    register(EffectType.DEFENSE_BUFF, this::handleDefenseBuff);
    register(EffectType.SPEED_BUFF, this::handleSpeedBuff);
    register(EffectType.STUN, this::handleStun);
    register(EffectType.FREEZE, this::handleFreeze);
    register(EffectType.SILENCE, this::handleSilence);
    // passive-only effects — should not reach active handler
    register(EffectType.RESIST_BUFF, ctx -> EffectHandler.EffectResult.NONE);
    register(EffectType.HP_BUFF, ctx -> EffectHandler.EffectResult.NONE);
    register(EffectType.SURVIVE_LETHAL, ctx -> EffectHandler.EffectResult.NONE);
  }

  private void register(EffectType type, EffectHandler handler) {
    handlers.put(type, handler);
  }

  public @Nullable EffectHandler getHandler(EffectType type) {
    return handlers.get(type);
  }

  public boolean hasHandler(EffectType type) {
    return handlers.containsKey(type);
  }

  // ===================== handlers =====================

  private EffectHandler.EffectResult handleDamage(EffectHandler.EffectContext ctx) {
    int dmg =
        ctx.damageCalculator()
            .calculateEffectDamage(ctx.attacker(), ctx.defender(), ctx.effect(), ctx.buffManager());
    return new EffectHandler.EffectResult(dmg, false, false);
  }

  private EffectHandler.EffectResult handleAoeDamage(EffectHandler.EffectContext ctx) {
    int base =
        ctx.damageCalculator()
            .calculateEffectDamage(ctx.attacker(), ctx.defender(), ctx.effect(), ctx.buffManager());
    for (Combatant other : ctx.defenderTeam().members()) {
      if (!other.equals(ctx.defender()) && other.isAlive()) {
        int aoeDmg = (int) (base * 0.6);
        other.takeDamage(aoeDmg);
      }
    }
    return new EffectHandler.EffectResult(base, false, false);
  }

  private EffectHandler.EffectResult handleMultiHit(EffectHandler.EffectContext ctx) {
    int hits = ctx.effect().value() != null ? ctx.effect().value().intValue() : 3;
    int dmg =
        ctx.damageCalculator()
                .calculateEffectDamage(
                    ctx.attacker(), ctx.defender(), ctx.effect(), ctx.buffManager())
            * hits;
    return new EffectHandler.EffectResult(dmg, false, false);
  }

  private EffectHandler.EffectResult handleArmorBreak(EffectHandler.EffectContext ctx) {
    applyDebuff(
        ctx, BuffType.ARMOR_BREAK, ctx.effect().value() != null ? ctx.effect().value() : 0.2, 3);
    return new EffectHandler.EffectResult(0, true, false);
  }

  private EffectHandler.EffectResult handleSlow(EffectHandler.EffectContext ctx) {
    applyDebuff(ctx, BuffType.SLOW, ctx.effect().value() != null ? ctx.effect().value() : 0.3, 2);
    return new EffectHandler.EffectResult(0, true, false);
  }

  private EffectHandler.EffectResult handleDot(EffectHandler.EffectContext ctx) {
    double value = ctx.effect().value() != null ? ctx.effect().value() : 0.15;
    int duration = ctx.effect().duration() != null ? ctx.effect().duration() : 3;
    int maxStacks = ctx.effect().maxStacks() != null ? ctx.effect().maxStacks() : 3;
    ctx.buffManager()
        .addBuff(
            ctx.defender().getId(),
            Buff.builder()
                .type(BuffType.DOT)
                .value(ctx.attacker().getAttack() * value)
                .remainingTurns(duration)
                .source(ctx.skill().getName())
                .stackable(true)
                .maxStacks(maxStacks)
                .build());
    return new EffectHandler.EffectResult(0, false, true);
  }

  private EffectHandler.EffectResult handleDodge(EffectHandler.EffectContext ctx) {
    applyBuff(ctx, BuffType.DODGE);
    return new EffectHandler.EffectResult(0, false, true);
  }

  private EffectHandler.EffectResult handleCounter(EffectHandler.EffectContext ctx) {
    applyBuff(ctx, BuffType.COUNTER);
    return new EffectHandler.EffectResult(0, false, true);
  }

  private EffectHandler.EffectResult handleReflect(EffectHandler.EffectContext ctx) {
    applyBuff(ctx, BuffType.REFLECT);
    return new EffectHandler.EffectResult(0, false, true);
  }

  private EffectHandler.EffectResult handleCleanse(EffectHandler.EffectContext ctx) {
    ctx.buffManager().removeDebuffs(ctx.attacker().getId());
    return new EffectHandler.EffectResult(0, false, true);
  }

  private EffectHandler.EffectResult handleExecute(EffectHandler.EffectContext ctx) {
    int baseDmg =
        ctx.damageCalculator()
            .calculateEffectDamage(ctx.attacker(), ctx.defender(), ctx.effect(), ctx.buffManager());
    int maxHp = ctx.defender().getMaxHp();
    if (maxHp <= 0) maxHp = 1;
    double hpRatio = (double) ctx.defender().getHp() / maxHp;
    double threshold = ctx.effect().value() != null ? ctx.effect().value() : 0.3;
    int dmg = (int) (baseDmg * (hpRatio < threshold ? 2.0 : 1.0));
    return new EffectHandler.EffectResult(dmg, false, false);
  }

  private EffectHandler.EffectResult handleLifesteal(EffectHandler.EffectContext ctx) {
    int dmg =
        ctx.damageCalculator()
            .calculateNormalDamage(ctx.attacker(), ctx.defender(), ctx.buffManager());
    double ratio = ctx.effect().value() != null ? ctx.effect().value() : 0.33;
    ctx.attacker().heal((int) (dmg * ratio));
    return new EffectHandler.EffectResult(dmg, false, false);
  }

  private EffectHandler.EffectResult handleHeal(EffectHandler.EffectContext ctx) {
    double ratio = ctx.effect().value() != null ? ctx.effect().value() : 0.5;
    int maxHp = ctx.attacker().getMaxHp();
    if (maxHp > 0) {
      ctx.attacker().heal((int) (maxHp * ratio));
    }
    return new EffectHandler.EffectResult(0, false, true);
  }

  private EffectHandler.EffectResult handleAttackBuff(EffectHandler.EffectContext ctx) {
    applyBuff(ctx, BuffType.ATTACK_BUFF);
    return new EffectHandler.EffectResult(0, false, true);
  }

  private EffectHandler.EffectResult handleDefenseBuff(EffectHandler.EffectContext ctx) {
    applyBuff(ctx, BuffType.DEFENSE_BUFF);
    return new EffectHandler.EffectResult(0, false, true);
  }

  private EffectHandler.EffectResult handleSpeedBuff(EffectHandler.EffectContext ctx) {
    applyBuff(ctx, BuffType.SPEED_BUFF);
    return new EffectHandler.EffectResult(0, false, true);
  }

  private EffectHandler.EffectResult handleStun(EffectHandler.EffectContext ctx) {
    applyControl(ctx, BuffType.STUN);
    return new EffectHandler.EffectResult(0, true, false);
  }

  private EffectHandler.EffectResult handleFreeze(EffectHandler.EffectContext ctx) {
    applyControl(ctx, BuffType.FREEZE);
    return new EffectHandler.EffectResult(0, true, false);
  }

  private EffectHandler.EffectResult handleSilence(EffectHandler.EffectContext ctx) {
    applyControl(ctx, BuffType.SILENCE);
    return new EffectHandler.EffectResult(0, true, false);
  }

  // ===================== 辅助方法 =====================

  private void applyBuff(EffectHandler.EffectContext ctx, BuffType type) {
    double value = ctx.effect().value() != null ? ctx.effect().value() : 0.2;
    int duration = ctx.effect().duration() != null ? ctx.effect().duration() : 3;
    ctx.buffManager()
        .addBuff(
            ctx.attacker().getId(),
            Buff.builder()
                .type(type)
                .value(value)
                .remainingTurns(duration)
                .source(ctx.skill().getName())
                .build());
  }

  private void applyDebuff(
      EffectHandler.EffectContext ctx, BuffType type, double value, int defaultDuration) {
    int duration = ctx.effect().duration() != null ? ctx.effect().duration() : defaultDuration;
    ctx.buffManager()
        .addBuff(
            ctx.defender().getId(),
            Buff.builder()
                .type(type)
                .value(value)
                .remainingTurns(duration)
                .source(ctx.skill().getName())
                .build());
  }

  private void applyControl(EffectHandler.EffectContext ctx, BuffType type) {
    int duration = ctx.effect().duration() != null ? ctx.effect().duration() : 1;
    // 回合开始时的持续效果处理会先 tick 并移除到期 buff，
    // 因此控制时长 +1 才能真正覆盖 duration 个无法行动的回合
    ctx.buffManager()
        .addBuff(
            ctx.defender().getId(),
            Buff.builder()
                .type(type)
                .value(1.0)
                .remainingTurns(duration + 1)
                .source(ctx.skill().getName())
                .build());
  }
}
