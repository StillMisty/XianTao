package top.stillmisty.xiantao.service.ai;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.service.UserContext;
import top.stillmisty.xiantao.service.beast.BeastBreedingService;
import top.stillmisty.xiantao.service.beast.BeastCombatService;

/** 灵兽管理工具 — 出战/召回、进化、放生、孵化、繁育 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SpiritBeastTools {

  private final ToolExecutor toolExecutor;
  private final BeastCombatService beastCombatService;
  private final BeastBreedingService beastBreedingService;

  @Tool(description = "灵兽管理：出战/召回、入栏（栏外休憩灵兽回归兽栏）、进化、放生、孵化")
  @Transactional
  public ManageBeastResponse manageBeast(
      @ToolParam(description = "兽栏地块编号") String position,
      @ToolParam(description = BEAST_ACTION_DESCRIPTION) BeastAction action,
      @ToolParam(description = "HATCH 时为兽卵名称，PEN 时为栏外灵兽名称（可省略取第一只），其他操作不需要", required = false)
          String value) {
    return toolExecutor.execute(
        "manageBeast",
        () -> {
          Long userId = UserContext.requireCurrentUserId();
          String result =
              switch (action) {
                case DEPLOY -> beastCombatService.toggleDeploy(userId, position);
                case PEN -> beastCombatService.penRestedBeast(userId, position, value);
                case EVOLVE -> {
                  beastBreedingService.evolveBeastInternal(userId, position);
                  yield "进化成功";
                }
                case RELEASE -> {
                  var vo = beastBreedingService.releaseBeastInternal(userId, position);
                  yield "放生了 %s，获得 %d 份灵兽精华".formatted(vo.beastName(), vo.essenceAmount());
                }
                case HATCH -> {
                  beastBreedingService.hatchBeastByInputInternal(userId, position, value);
                  yield "孵化成功";
                }
              };
          return new ManageBeastResponse(result);
        });
  }

  @Tool(description = "让两只灵兽繁育后代。需提供两个兽栏地块编号，要求一阴一阳、均已成年、不在休养/冷却中")
  @Transactional
  public BreedBeastsResponse breedBeasts(
      @ToolParam(description = "第一只灵兽的兽栏地块编号") String position1,
      @ToolParam(description = "第二只灵兽的兽栏地块编号") String position2) {
    return toolExecutor.execute(
        "breedBeasts",
        () -> {
          Long userId = UserContext.requireCurrentUserId();
          var r = beastBreedingService.breed(userId, position1, position2);
          return new BreedBeastsResponse(
              r.parent1Name(),
              r.parent1Gender(),
              r.parent2Name(),
              r.parent2Gender(),
              r.offspringEggName(),
              r.offspringQuality(),
              r.inheritedTraits(),
              r.cooldownHours());
        });
  }

  // ===== Response records =====

  public record ManageBeastResponse(@JsonPropertyDescription("操作结果描述") String result) {}

  public record BreedBeastsResponse(
      @JsonPropertyDescription("父方名称") String parent1Name,
      @JsonPropertyDescription("父方性别") String parent1Gender,
      @JsonPropertyDescription("母方名称") String parent2Name,
      @JsonPropertyDescription("母方性别") String parent2Gender,
      @JsonPropertyDescription("后代兽卵名称") String offspringEggName,
      @JsonPropertyDescription("后代品质") String offspringQuality,
      @JsonPropertyDescription("继承的变异词条") java.util.List<String> inheritedTraits,
      @JsonPropertyDescription("繁育冷却小时数") long cooldownHours) {}

  /** 枚举参数描述（供 @ToolParam 引用） */
  public static final String BEAST_ACTION_DESCRIPTION =
      "操作类型: DEPLOY(出战/召回) PEN(栏外休憩灵兽入栏) EVOLVE(进化) RELEASE(放生) HATCH(孵化)";

  public enum BeastAction {
    DEPLOY,
    PEN,
    EVOLVE,
    RELEASE,
    HATCH
  }
}
