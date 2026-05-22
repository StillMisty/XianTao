package top.stillmisty.xiantao.service.ai;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.service.UserContext;
import top.stillmisty.xiantao.service.sect.SectMemberService;
import top.stillmisty.xiantao.service.sect.SectSharedSkillService;
import top.stillmisty.xiantao.service.sect.SectShopService;

/** 宗门所有成员可用的工具 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SectMemberTools {

  private final SectShopService sectShopService;
  private final SectSharedSkillService sectSharedSkillService;
  private final SectMemberService sectMemberService;

  @Tool(description = "查看宗门贡献商店商品列表，含价格和库存")
  public SectToolResponse queryShop() {
    try {
      Long userId = UserContext.requireCurrentUserId();
      String result = sectShopService.getShop(userId);
      return new SectToolResponse(true, result);
    } catch (Exception e) {
      log.error("查询贡献商店失败", e);
      return new SectToolResponse(false, e.getMessage());
    }
  }

  @Tool(description = "消耗贡献值兑换贡献商店中的商品")
  @Transactional
  public SectToolResponse exchangeShopItem(@ToolParam(description = "商品编号") long shopItemId) {
    try {
      Long userId = UserContext.requireCurrentUserId();
      String result = sectShopService.exchangeShopItem(userId, shopItemId);
      return new SectToolResponse(true, result);
    } catch (Exception e) {
      log.error("兑换商品失败: shopItemId={}", shopItemId, e);
      return new SectToolResponse(false, e.getMessage());
    }
  }

  @Tool(description = "查看宗门共享功法列表，含功法详情和学习消耗")
  public SectToolResponse querySharedSkills() {
    try {
      Long userId = UserContext.requireCurrentUserId();
      String result = sectSharedSkillService.getSharedSkills(userId);
      return new SectToolResponse(true, result);
    } catch (Exception e) {
      log.error("查询共享功法失败", e);
      return new SectToolResponse(false, e.getMessage());
    }
  }

  @Tool(description = "消耗贡献值学习指定的共享功法")
  @Transactional
  public SectToolResponse learnSharedSkill(@ToolParam(description = "共享功法编号") long sharedSkillId) {
    try {
      Long userId = UserContext.requireCurrentUserId();
      String result = sectSharedSkillService.learnSharedSkill(userId, sharedSkillId);
      return new SectToolResponse(true, result);
    } catch (Exception e) {
      log.error("学习共享功法失败: sharedSkillId={}", sharedSkillId, e);
      return new SectToolResponse(false, e.getMessage());
    }
  }

  @Tool(description = "提交功法玉简到宗门，换取贡献值")
  @Transactional
  public SectToolResponse submitSkillJade(@ToolParam(description = "玉简名称") String jadeName) {
    try {
      Long userId = UserContext.requireCurrentUserId();
      String result = sectSharedSkillService.submitSkillJade(userId, jadeName);
      return new SectToolResponse(true, result);
    } catch (Exception e) {
      log.error("提交功法玉简失败: jadeName={}", jadeName, e);
      return new SectToolResponse(false, e.getMessage());
    }
  }

  @Tool(description = "捐献灵石获取贡献值（兑换比例 10:1）每次捐献下限 1000 灵石")
  @Transactional
  public SectToolResponse donateStones(@ToolParam(description = "捐献灵石数量，最低 1000") long amount) {
    try {
      Long userId = UserContext.requireCurrentUserId();
      String result = sectMemberService.donateStones(userId, amount);
      return new SectToolResponse(true, result);
    } catch (Exception e) {
      log.error("捐献灵石失败: amount={}", amount, e);
      return new SectToolResponse(false, e.getMessage());
    }
  }

  @Tool(description = "获取当前宗门事件对应的任务列表，无事件时返回空")
  public SectToolResponse queryTasks() {
    try {
      Long userId = UserContext.requireCurrentUserId();
      String result = sectMemberService.getTasks(userId);
      return new SectToolResponse(true, result);
    } catch (Exception e) {
      log.error("查询宗门任务失败", e);
      return new SectToolResponse(false, e.getMessage());
    }
  }
}
