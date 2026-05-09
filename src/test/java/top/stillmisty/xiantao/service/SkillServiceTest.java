package top.stillmisty.xiantao.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import top.stillmisty.xiantao.domain.item.entity.ItemTemplate;
import top.stillmisty.xiantao.domain.item.entity.StackableItem;
import top.stillmisty.xiantao.domain.item.enums.ItemType;
import top.stillmisty.xiantao.domain.item.repository.ItemTemplateRepository;
import top.stillmisty.xiantao.domain.item.repository.StackableItemRepository;
import top.stillmisty.xiantao.domain.skill.entity.PlayerSkill;
import top.stillmisty.xiantao.domain.skill.entity.Skill;
import top.stillmisty.xiantao.domain.skill.repository.PlayerSkillRepository;
import top.stillmisty.xiantao.domain.skill.repository.SkillRepository;
import top.stillmisty.xiantao.domain.skill.vo.SkillSlotResult;
import top.stillmisty.xiantao.domain.user.entity.User;

@DisplayName("SkillService 测试")
@ExtendWith(MockitoExtension.class)
class SkillServiceTest {

  @Mock private UserStateService userStateService;
  @Mock private SkillRepository skillRepository;
  @Mock private PlayerSkillRepository playerSkillRepository;
  @Mock private StackableItemRepository stackableItemRepository;
  @Mock private StackableItemService stackableItemService;
  @Mock private ItemTemplateRepository itemTemplateRepository;

  @InjectMocks private SkillService skillService;

  private final Long userId = 1L;
  private final Long skillId = 100L;

  private Skill createSkill() {
    Skill skill = new Skill();
    skill.setId(skillId);
    skill.setName("御剑术");
    skill.setDescription("基础剑术");
    skill.setLevelRequirement(1);
    skill.setCooldownSeconds(0);
    return skill;
  }

  private Skill createSkill(Long id, String name, int levelReq) {
    Skill skill = new Skill();
    skill.setId(id);
    skill.setName(name);
    skill.setLevelRequirement(levelReq);
    skill.setCooldownSeconds(0);
    return skill;
  }

  private User createUser(int level) {
    return User.create().setId(userId).setLevel(level).setNickname("测试");
  }

  // ===================== learnFromJade =====================

  @Test
  @DisplayName("learnFromJade — 无玉简时返回失败")
  void learnFromJade_whenNoJadeItems_shouldReturnFailure() {
    when(stackableItemRepository.findByUserId(userId)).thenReturn(List.of());

    SkillSlotResult result = skillService.learnFromJade(userId, "1");

    assertFalse(result.isSuccess());
    assertTrue(result.getMessage().contains("没有法决玉简"));
  }

  @Test
  @DisplayName("learnFromJade — 无法匹配指定玉简返回失败")
  void learnFromJade_whenJadeNotFound_shouldReturnFailure() {
    StackableItem jade = new StackableItem();
    jade.setId(1L);
    jade.setName("未知玉简");
    jade.setItemType(ItemType.SKILL_JADE);
    when(stackableItemRepository.findByUserId(userId)).thenReturn(List.of(jade));

    // input "2" should be out of range for 1-item list
    SkillSlotResult result = skillService.learnFromJade(userId, "2");

    assertFalse(result.isSuccess());
    assertTrue(result.getMessage().contains("找不到匹配"));
  }

  @Test
  @DisplayName("learnFromJade — 已学会时返回失败")
  void learnFromJade_whenAlreadyLearned_shouldReturnFailure() {
    Skill skill = createSkill();
    StackableItem jade = createJadeItem(1L, skillId);
    setUpJadeMock(skill, jade);
    when(playerSkillRepository.findByUserIdAndSkillId(userId, skillId))
        .thenReturn(Optional.of(new PlayerSkill()));

    SkillSlotResult result = skillService.learnFromJade(userId, "1");

    assertFalse(result.isSuccess());
    assertTrue(result.getMessage().contains("已经学会"));
  }

  @Test
  @DisplayName("learnFromJade — 等级不足返回失败")
  void learnFromJade_whenLevelTooLow_shouldReturnFailure() {
    Skill skill = createSkill(100L, "高级术", 20);
    StackableItem jade = createJadeItem(1L, 100L);
    setUpJadeMock(skill, jade);
    when(playerSkillRepository.findByUserIdAndSkillId(userId, 100L)).thenReturn(Optional.empty());
    when(userStateService.loadUser(userId)).thenReturn(createUser(5));

    SkillSlotResult result = skillService.learnFromJade(userId, "1");

    assertFalse(result.isSuccess());
    assertTrue(result.getMessage().contains("境界不足"));
  }

  @Test
  @DisplayName("learnFromJade — 成功学会法决")
  void learnFromJade_whenValid_shouldLearnSkill() {
    Skill skill = createSkill();
    StackableItem jade = createJadeItem(1L, skillId, 2);
    setUpJadeMock(skill, jade);
    when(playerSkillRepository.findByUserIdAndSkillId(userId, skillId))
        .thenReturn(Optional.empty());
    when(userStateService.loadUser(userId)).thenReturn(createUser(5));
    doNothing().when(stackableItemService).reduceStackableItem(anyLong(), anyLong(), anyInt());
    when(playerSkillRepository.save(any()))
        .thenAnswer(
            inv -> {
              PlayerSkill ps = inv.getArgument(0);
              ps.setId(1L);
              return ps;
            });

    SkillSlotResult result = skillService.learnFromJade(userId, "1");

    assertTrue(result.isSuccess());
    assertTrue(result.getMessage().contains("成功学会"));
    assertNotNull(result.getSkill());
    verify(playerSkillRepository).save(any(PlayerSkill.class));
  }

  // ===================== equipSkill =====================

  @Test
  @DisplayName("equipSkill — 无学会法决返回失败")
  void equipSkill_whenNoLearnedSkills_shouldReturnFailure() {
    when(playerSkillRepository.findByUserId(userId)).thenReturn(List.of());

    SkillSlotResult result = skillService.equipSkill(userId, "1");

    assertFalse(result.isSuccess());
    assertTrue(result.getMessage().contains("没有学会任何法决"));
  }

  @Test
  @DisplayName("equipSkill — 已装载返回失败")
  void equipSkill_whenAlreadyEquipped_shouldReturnFailure() {
    PlayerSkill ps = new PlayerSkill();
    ps.setId(1L);
    ps.setSkillId(skillId);
    ps.equip();

    when(playerSkillRepository.findByUserId(userId)).thenReturn(List.of(ps));
    when(skillRepository.findByIds(anyList())).thenReturn(List.of(createSkill()));

    SkillSlotResult result = skillService.equipSkill(userId, "1");

    assertFalse(result.isSuccess());
    assertTrue(result.getMessage().contains("已经在槽位中"));
  }

  // ===================== unequipSkill =====================

  @Test
  @DisplayName("unequipSkill — 无装载法决返回失败")
  void unequipSkill_whenNoEquipped_shouldReturnFailure() {
    when(playerSkillRepository.findEquippedByUserId(userId)).thenReturn(List.of());

    SkillSlotResult result = skillService.unequipSkill(userId, "1");

    assertFalse(result.isSuccess());
    assertTrue(result.getMessage().contains("没有装载"));
  }

  // ===================== getLearnedSkills / getEquippedSkills =====================

  @Test
  @DisplayName("getLearnedSkills — 无限得法决返回空列表")
  void getLearnedSkills_whenNone_shouldReturnEmptyList() {
    when(playerSkillRepository.findByUserId(userId)).thenReturn(List.of());

    var result = skillService.getLearnedSkills(userId);

    assertTrue(result.isEmpty());
  }

  @Test
  @DisplayName("getEquippedSkills — 无装载法决返回空列表")
  void getEquippedSkills_whenNone_shouldReturnEmptyList() {
    when(playerSkillRepository.findEquippedByUserId(userId)).thenReturn(List.of());

    var result = skillService.getEquippedSkills(userId);

    assertTrue(result.isEmpty());
  }

  // ===================== helpers =====================

  private StackableItem createJadeItem(Long id, Long skillId) {
    return createJadeItem(id, skillId, 1);
  }

  private StackableItem createJadeItem(Long id, Long skillId, int quantity) {
    StackableItem jade = new StackableItem();
    jade.setId(id);
    jade.setTemplateId(200L);
    jade.setName("御剑术玉简");
    jade.setItemType(ItemType.SKILL_JADE);
    jade.setQuantity(quantity);
    return jade;
  }

  private void setUpJadeMock(Skill skill, StackableItem jade) {
    when(stackableItemRepository.findByUserId(userId)).thenReturn(List.of(jade));
    when(itemTemplateRepository.findById(jade.getTemplateId()))
        .thenReturn(Optional.of(createJadeTemplate(skill.getId())));
    when(skillRepository.findById(skill.getId())).thenReturn(Optional.of(skill));
  }

  private ItemTemplate createJadeTemplate(Long skillId) {
    ItemTemplate template = new ItemTemplate();
    template.setId(200L);
    template.setName("玉简");
    template.setType(ItemType.SKILL_JADE);
    template.setProperties(Map.of("skill_id", skillId));
    return template;
  }
}
