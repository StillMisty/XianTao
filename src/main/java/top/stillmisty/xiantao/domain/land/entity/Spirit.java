package top.stillmisty.xiantao.domain.land.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import top.stillmisty.xiantao.domain.land.enums.EmotionState;
import top.stillmisty.xiantao.domain.land.enums.MBTIPersonality;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Table("xt_spirit")
@Accessors(chain = true)
@Data(staticConstructor = "create")
public class Spirit extends Model<Spirit> {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private Long fudiId;

    private Integer formId;

    private Integer energy;

    private Integer affection;

    private Integer affectionMax;

    private EmotionState emotionState;

    private MBTIPersonality mbtiType;

    private LocalDateTime lastEnergyUpdate;

    private LocalDateTime lastGiftTime;

    @Column(onInsertValue = "now()")
    private LocalDateTime createTime;

    @Column(onUpdateValue = "now()", onInsertValue = "now()")
    private LocalDateTime updateTime;

    public int getEnergyMax(int tribulationStage) {
        return 100 + tribulationStage * 20;
    }

    public void restoreEnergy(int tribulationStage) {
        if (lastEnergyUpdate == null || energy == null) {
            lastEnergyUpdate = LocalDateTime.now();
            return;
        }

        int maxEnergy = getEnergyMax(tribulationStage);
        if (energy >= maxEnergy) return;

        long hoursElapsed = java.time.Duration.between(lastEnergyUpdate, LocalDateTime.now()).toHours();
        if (hoursElapsed <= 0) return;

        int recoveryRate = (int) (maxEnergy * 0.05 * hoursElapsed);
        if (recoveryRate > 0) {
            energy = Math.min(maxEnergy, energy + recoveryRate);
        }
    }

    public int calculateEnergyConsumption(int baseCost) {
        int aff = affection != null ? affection : 0;
        int maxAff = affectionMax != null ? affectionMax : 1000;
        double discount = Math.min(0.5, (double) aff / maxAff);
        return (int) Math.max(1, baseCost * (1.0 - discount));
    }

    public boolean deductEnergy(int cost) {
        if (energy == null) energy = 0;
        energy = Math.max(0, energy - cost);
        if (energy <= 0) {
            emotionState = EmotionState.FATIGUED;
            return true;
        }
        return false;
    }

    public void addAffection(int amount) {
        int maxAff = affectionMax != null ? affectionMax : 1000;
        affection = Math.clamp(
                (affection != null ? affection : 0) + amount,
                0,
                maxAff
        );
    }

    public void updateEmotionState() {
        if (energy != null && energy <= 0) {
            emotionState = EmotionState.FATIGUED;
            return;
        }

        int aff = affection != null ? affection : 0;
        if (aff >= 800) {
            emotionState = EmotionState.HAPPY;
        } else if (aff >= 400) {
            emotionState = EmotionState.CALM;
        } else {
            emotionState = EmotionState.ANXIOUS;
        }
    }
}
