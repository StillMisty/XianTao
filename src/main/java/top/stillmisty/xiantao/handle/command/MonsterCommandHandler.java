package top.stillmisty.xiantao.handle.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.monster.vo.BattleResultVO;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.service.CombatService;
import top.stillmisty.xiantao.service.ServiceResult;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class MonsterCommandHandler {

    private final CombatService combatService;

    public String handleSimulateDuel(PlatformType platform, String openId, int durationMinutes) {
        log.debug("处理战斗模拟请求 - Platform: {}, OpenId: {}, Duration: {}", platform, openId, durationMinutes);
        return switch (combatService.simulateTraining(platform, openId, durationMinutes)) {
            case ServiceResult.Failure(var msg) -> msg;
            case ServiceResult.Success(var vo) -> formatBattleResult(vo);
        };
    }

    private String formatBattleResult(BattleResultVO result) {
        if (result.summary() == null || result.summary().isBlank()) {
            return "战斗结束，但未获得任何战利品。";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("【战斗结算】\n");

        if (result.rounds() > 0) {
            sb.append(String.format("战斗回合: %d\n", result.rounds()));
        }

        sb.append(result.summary());

        // 战斗日志摘要
        List<Map<String, Object>> combatLog = result.combatLog();
        if (combatLog != null && !combatLog.isEmpty()) {
            sb.append("\n【战斗日志】\n");
            int showCount = Math.min(10, combatLog.size());
            for (int i = 0; i < showCount; i++) {
                Map<String, Object> entry = combatLog.get(i);
                String attackType = entry.get("attackType") != null ? entry.get("attackType").toString() : "NORMAL";
                String attacker = entry.get("attackerName") != null ? entry.get("attackerName").toString() : "?";
                String defender = entry.get("defenderName") != null ? entry.get("defenderName").toString() : "?";
                Object dmgObj = entry.get("damageDealt");
                String damage = dmgObj != null ? dmgObj.toString() : "0";
                String skillName = entry.get("skillName") != null ? "[" + entry.get("skillName") + "] " : "";
                boolean isKill = Boolean.TRUE.equals(entry.get("isKill"));

                String typeMark = "SKILL".equals(attackType) ? "法" : "普";
                String line = String.format("  R%d %s%s → %s (%s%s)%s",
                        entry.get("round"), skillName, attacker, defender, typeMark, damage,
                        isKill ? " 击杀!" : "");
                sb.append(line).append("\n");
            }
            if (combatLog.size() > showCount) {
                sb.append(String.format("  ...共 %d 条攻击记录\n", combatLog.size()));
            }
        }

        // 掉落物
        if (result.drops() != null && !result.drops().isEmpty()) {
            sb.append("\n【战利品】\n");
            for (Map<String, Object> drop : result.drops()) {
                String name = drop.get("name") != null ? drop.get("name").toString() : "?";
                int qty = ((Number) drop.getOrDefault("quantity", 1)).intValue();
                sb.append(String.format("  %s x%d\n", name, qty));
            }
        }

        // 法决触发
        if (result.skillProcs() != null && !result.skillProcs().isEmpty()) {
            sb.append("\n【法决触发】\n");
            for (Map<String, Object> proc : result.skillProcs()) {
                String key = proc.get("key") != null ? proc.get("key").toString() : "?";
                int count = ((Number) proc.getOrDefault("count", 0)).intValue();
                sb.append(String.format("  %s x%d\n", key, count));
            }
        }

        return sb.toString();
    }
}
