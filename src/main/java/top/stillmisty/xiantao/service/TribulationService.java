package top.stillmisty.xiantao.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.stillmisty.xiantao.domain.beast.entity.Beast;
import top.stillmisty.xiantao.domain.beast.repository.BeastRepository;
import top.stillmisty.xiantao.domain.fudi.entity.Fudi;
import top.stillmisty.xiantao.domain.fudi.entity.FudiCell;
import top.stillmisty.xiantao.domain.fudi.enums.CellType;
import top.stillmisty.xiantao.domain.fudi.enums.EmotionState;
import top.stillmisty.xiantao.domain.fudi.enums.MutationTrait;
import top.stillmisty.xiantao.domain.fudi.repository.FudiCellRepository;
import top.stillmisty.xiantao.domain.fudi.repository.FudiRepository;
import top.stillmisty.xiantao.domain.fudi.repository.SpiritRepository;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class TribulationService {

    private final FudiRepository fudiRepository;
    private final FudiCellRepository fudiCellRepository;
    private final SpiritRepository spiritRepository;
    private final BeastRepository beastRepository;
    private final FudiHelper fudiHelper;

    /**
     * 尝试触发天劫判定，返回结果文本；如果未触发返回 null
     *
     * @param fudi         福地
     * @param playerLevel  玩家境界
     * @param playerStr    玩家战力值
     * @param forceTrigger 是否强制触发（忽略冷却）
     * @return 天劫结果文本，未触发返回 null
     */
    public String resolveTribulation(Fudi fudi, int playerLevel, int playerStr, boolean forceTrigger) {
        LocalDateTime referenceTime = fudi.getLastTribulationTime() != null
                ? fudi.getLastTribulationTime()
                : fudi.getCreateTime();

        if (!forceTrigger && java.time.Duration.between(referenceTime, LocalDateTime.now()).toDays() < 7) {
            return null;
        }

        int attack = playerLevel * 80 + fudi.getTribulationStage() * 200;
        int defense = calculateTribulationDefense(fudi, playerStr);

        fudi.setLastTribulationTime(LocalDateTime.now());

        boolean compassionTriggered = checkTribulationCompassion(fudi, attack, defense);

        if (defense > attack) {
            return compassionTriggered ? null : applyTribulationWin(fudi, attack, defense);
        } else if (compassionTriggered) {
            return applyTribulationCompassion(fudi, attack, defense);
        } else {
            return applyTribulationLoss(fudi, attack, defense);
        }
    }

    public int calculateTribulationDefense(Fudi fudi, int playerStr) {
        int defense = playerStr * 10 + fudi.getTribulationStage() * 50;

        List<Beast> beasts = beastRepository.findByFudiId(fudi.getId());
        for (Beast beast : beasts) {
            if (!Boolean.TRUE.equals(beast.getIsDeployed())) continue;
            if (beast.getHpCurrent() <= 0) continue;

            int tier = beast.getTier();
            double power = tier * 10.0;
            List<String> traits = beast.getMutationTraits();
            if (traits != null && traits.contains(MutationTrait.GUARDIAN.getCode())) {
                power *= 1.5;
            }
            defense += (int) power;
        }

        return defense;
    }

    private boolean checkTribulationCompassion(Fudi fudi, int attack, int defense) {
        var spirit = spiritRepository.findByFudiId(fudi.getId()).orElse(null);
        int affection = spirit != null ? spirit.getAffection() : 0;
        return affection >= 800 && defense >= attack * 0.8 && defense < attack;
    }

    private String applyTribulationCompassion(Fudi fudi, int attack, int defense) {
        int oldStage = fudi.getTribulationStage();
        int newWinStreak = fudi.getTribulationWinStreak() + 1;
        int newStage = oldStage + 1;

        fudi.setTribulationWinStreak(newWinStreak);
        fudi.setTribulationStage(newStage);

        int stoneReward = newWinStreak * 100;
        fudiHelper.addSpiritStones(fudi.getUserId(), stoneReward);

        spiritRepository.findByFudiId(fudi.getId()).ifPresent(spirit -> {
            spirit.setEnergy(0);
            spirit.setEmotionState(EmotionState.FATIGUED);
            spirit.setLastEnergyUpdate(LocalDateTime.now());
            spiritRepository.save(spirit);
        });

        return "🛡️⚡ 天劫降临！地灵燃烧灵体为你挡下了天雷……\n" +
                "   攻击力：" + attack + " ｜ 防御力：" + defense + " ｜ 怜悯庇护！\n" +
                "   劫数：" + oldStage + " → " + newStage + " ｜ 连胜×" + newWinStreak + "\n" +
                "   灵石奖励：+" + stoneReward + "\n" +
                "   精力归零，地灵陷入疲惫…";
    }

    private String applyTribulationWin(Fudi fudi, int attack, int defense) {
        int oldStage = fudi.getTribulationStage();
        int newWinStreak = fudi.getTribulationWinStreak() + 1;
        int newStage = oldStage + 1;

        fudi.setTribulationWinStreak(newWinStreak);
        fudi.setTribulationStage(newStage);

        int stoneReward = newWinStreak * 100;
        fudiHelper.addSpiritStones(fudi.getUserId(), stoneReward);

        var spirit = spiritRepository.findByFudiId(fudi.getId()).orElse(null);
        int oldAffection = spirit != null ? spirit.getAffection() : 0;
        if (spirit != null) {
            spirit.addAffection(5);
            spirit.setEmotionState(EmotionState.EXCITED);
            spiritRepository.save(spirit);
        }

        return String.format(
                """
                        ⚡ 天劫降临！福地成功抵御！
                           攻击力：%d ｜ 防御力：%d ｜ 胜利！
                           劫数：%d → %d ｜ 连胜×%d
                           灵石奖励：+%d ｜ 好感度：%d → %d""",
                attack, defense,
                oldStage, newStage, newWinStreak,
                stoneReward, oldAffection, spirit != null ? spirit.getAffection() : 0
        );
    }

    private String applyTribulationLoss(Fudi fudi, int attack, int defense) {
        int oldWinStreak = fudi.getTribulationWinStreak();

        var spirit = spiritRepository.findByFudiId(fudi.getId()).orElse(null);
        int oldAffection = spirit != null ? spirit.getAffection() : 0;

        int diff = attack - defense;
        int occupiedCount = (int) fudiCellRepository.findByFudiId(fudi.getId()).stream()
                .filter(cell -> cell.getCellType() != CellType.EMPTY)
                .count();
        double ratio = Math.min(1.0, (double) diff / attack);
        int clearCount = Math.clamp((int) Math.ceil(ratio * occupiedCount), 1, occupiedCount);

        List<FudiCell> occupiedCells = fudiCellRepository.findByFudiId(fudi.getId()).stream()
                .filter(cell -> cell.getCellType() != CellType.EMPTY)
                .toList();
        List<FudiCell> cellsToDestroy = new ArrayList<>(occupiedCells);
        Collections.shuffle(cellsToDestroy);
        cellsToDestroy = cellsToDestroy.subList(0, Math.min(clearCount, cellsToDestroy.size()));

        for (FudiCell cell : cellsToDestroy) {
            cell.setCellType(CellType.EMPTY);
            cell.setConfig(new HashMap<>());
            fudiCellRepository.save(cell);
        }

        fudi.setTribulationWinStreak(0);

        if (spirit != null) {
            spirit.addAffection(-clearCount);
            spirit.setEmotionState(EmotionState.ANGRY);
            spiritRepository.save(spirit);
        }

        return String.format(
                """
                        ⚡ 天劫降临！福地未能抵御…
                           攻击力：%d ｜ 防御力：%d ｜ 差额：%d
                           连胜×%d → 中断，被毁地块：%d 个
                           好感度：%d → %d""",
                attack, defense, diff,
                oldWinStreak, clearCount,
                oldAffection, spirit != null ? spirit.getAffection() : 0
        );
    }
}
