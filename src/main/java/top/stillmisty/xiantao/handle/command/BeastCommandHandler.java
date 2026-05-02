package top.stillmisty.xiantao.handle.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.beast.vo.BeastStatusVO;
import top.stillmisty.xiantao.domain.fudi.vo.PenCellVO;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.service.BeastService;
import top.stillmisty.xiantao.service.ServiceResult;

import java.util.List;
import java.util.Map;

/**
 * 灵兽命令处理器（纯 View 层）
 * 调用 Service 层获取结构化数据，格式化为纯文本返回
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BeastCommandHandler {

    private final BeastService beastService;

    /**
     * 处理灵兽出战命令
     */
    public String handleDeployBeast(PlatformType platform, String openId, String position) {
        log.debug("处理灵兽出战 - Platform: {}, OpenId: {}, Position: {}", platform, openId, position);
        return switch (beastService.deployBeast(platform, openId, position)) {
            case ServiceResult.Failure(var msg) -> msg;
            case ServiceResult.Success(var result) -> formatDeployResult(result);
        };
    }

    /**
     * 处理灵兽召回命令
     */
    public String handleUndeployBeast(PlatformType platform, String openId, String position) {
        log.debug("处理灵兽召回 - Platform: {}, OpenId: {}, Position: {}", platform, openId, position);
        return switch (beastService.undeployBeast(platform, openId, position)) {
            case ServiceResult.Failure(var msg) -> msg;
            case ServiceResult.Success(var result) -> formatUndeployResult(result);
        };
    }

    /**
     * 处理灵兽恢复命令
     */
    public String handleRecoverBeast(PlatformType platform, String openId, String position) {
        log.debug("处理灵兽恢复 - Platform: {}, OpenId: {}, Position: {}", platform, openId, position);
        return switch (beastService.recoverBeast(platform, openId, position)) {
            case ServiceResult.Failure(var msg) -> msg;
            case ServiceResult.Success(var result) -> formatRecoverResult(result);
        };
    }

    /**
     * 处理灵兽进化命令
     */
    public String handleEvolveBeast(PlatformType platform, String openId, String position, String mode) {
        log.debug("处理灵兽进化 - Platform: {}, OpenId: {}, Position: {}, Mode: {}", platform, openId, position, mode);
        return switch (beastService.evolveBeast(platform, openId, position, mode)) {
            case ServiceResult.Failure(var msg) -> msg;
            case ServiceResult.Success(var result) -> formatEvolveResult(result);
        };
    }

    /**
     * 处理灵兽放生命令
     */
    public String handleReleaseBeast(PlatformType platform, String openId, String position) {
        log.debug("处理灵兽放生 - Platform: {}, OpenId: {}, Position: {}", platform, openId, position);
        return switch (beastService.releaseBeast(platform, openId, position)) {
            case ServiceResult.Failure(var msg) -> msg;
            case ServiceResult.Success(var result) -> formatReleaseResult(result);
        };
    }

    /**
     * 处理查看出战灵兽命令
     */
    public String handleGetDeployedBeasts(PlatformType platform, String openId) {
        log.debug("处理查看出战灵兽 - Platform: {}, OpenId: {}", platform, openId);
        return switch (beastService.getDeployedBeasts(platform, openId)) {
            case ServiceResult.Failure(var msg) -> msg;
            case ServiceResult.Success(var beasts) -> formatDeployedBeasts(beasts);
        };
    }

    // ===================== 文本格式化方法 =====================

    private String formatDeployResult(Map<String, Object> result) {
        Boolean success = (Boolean) result.get("success");
        String message = (String) result.get("message");
        if (Boolean.TRUE.equals(success)) {
            return "【灵兽出战】\n" + message;
        } else {
            return "【出战失败】\n" + message;
        }
    }

    private String formatUndeployResult(Map<String, Object> result) {
        Boolean success = (Boolean) result.get("success");
        String message = (String) result.get("message");
        if (Boolean.TRUE.equals(success)) {
            return "【灵兽召回】\n" + message;
        } else {
            return "【召回失败】\n" + message;
        }
    }

    private String formatRecoverResult(Map<String, Object> result) {
        Boolean success = (Boolean) result.get("success");
        String message = (String) result.get("message");
        if (Boolean.TRUE.equals(success)) {
            return "【灵兽恢复】\n" + message;
        } else {
            return "【恢复失败】\n" + message;
        }
    }

    private String formatEvolveResult(PenCellVO beast) {
        StringBuilder sb = new StringBuilder();
        sb.append("【灵兽进化成功】\n");
        sb.append(String.format("名称：%s\n", beast.getBeastName()));
        sb.append(String.format("等阶：T%d\n", beast.getTier()));
        sb.append(String.format("品质：%s\n", beast.getQuality()));
        sb.append(String.format("战力：%d\n", beast.getPowerScore()));
        return sb.toString();
    }

    private String formatReleaseResult(Map<String, Object> result) {
        String beastName = (String) result.get("beastName");
        Integer tier = (Integer) result.get("tier");
        String quality = (String) result.get("quality");
        return String.format("【灵兽放生成功】\n放生了 %s（T%d %s）\n获得灵兽精华", beastName, tier, quality);
    }

    private String formatDeployedBeasts(List<BeastStatusVO> beasts) {
        if (beasts.isEmpty()) {
            return "【出战灵兽】（空）\n没有出战的灵兽。";
        }
        StringBuilder sb = new StringBuilder("【出战灵兽】\n");
        for (int i = 0; i < beasts.size(); i++) {
            BeastStatusVO beast = beasts.get(i);
            sb.append(String.format("%d. %s（T%d %s）\n", i + 1, beast.beastName(), beast.tier(), beast.quality()));
            sb.append(String.format("   等级：%d\n", beast.level()));
            sb.append(String.format("   HP：%d/%d\n", beast.hpCurrent(), beast.maxHp()));
            sb.append(String.format("   攻击：%d 防御：%d\n", beast.attack(), beast.defense()));
            if (beast.skills() != null && !beast.skills().isEmpty()) {
                sb.append(String.format("   技能：%d个\n", beast.skills().size()));
            }
        }
        return sb.toString();
    }
}