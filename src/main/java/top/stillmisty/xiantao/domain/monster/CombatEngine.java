package top.stillmisty.xiantao.domain.monster;

import top.stillmisty.xiantao.domain.monster.vo.BattleResultVO;

/**
 * 战斗引擎接口
 * 支持不同战斗场景（历练/秘境/PVP）
 */
public interface CombatEngine {

    /**
     * 执行战斗模拟
     *
     * @param context 战斗上下文
     * @return 战斗结果
     */
    BattleResultVO simulate(BattleContext context);
}
