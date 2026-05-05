package top.stillmisty.xiantao.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import top.stillmisty.xiantao.domain.bounty.BountyRewardItem;
import top.stillmisty.xiantao.domain.bounty.BountyRewardPool;
import top.stillmisty.xiantao.domain.bounty.entity.Bounty;
import top.stillmisty.xiantao.domain.bounty.entity.UserBounty;
import top.stillmisty.xiantao.domain.bounty.repository.BountyRepository;
import top.stillmisty.xiantao.domain.bounty.repository.UserBountyRepository;
import top.stillmisty.xiantao.domain.bounty.vo.BountyStatusVO;
import top.stillmisty.xiantao.domain.bounty.vo.BountyVO;
import top.stillmisty.xiantao.domain.item.entity.ItemTemplate;
import top.stillmisty.xiantao.domain.item.enums.ItemType;
import top.stillmisty.xiantao.domain.item.repository.ItemTemplateRepository;
import top.stillmisty.xiantao.domain.map.entity.MapNode;
import top.stillmisty.xiantao.domain.map.entity.SpecialtyEntry;
import top.stillmisty.xiantao.domain.map.repository.MapNodeRepository;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.domain.user.enums.UserStatus;

@DisplayName("BountyService 测试")
@ExtendWith(MockitoExtension.class)
class BountyServiceTest {

  @Mock private UserStateService userStateService;
  @Mock private MapNodeRepository mapNodeRepository;
  @Mock private BountyRepository bountyRepository;
  @Mock private UserBountyRepository userBountyRepository;
  @Mock private ItemTemplateRepository itemTemplateRepository;
  @Mock private StackableItemService stackableItemService;

  @InjectMocks private BountyService bountyService;

  private final Long userId = 1L;
  private final Long mapId = 10L;
  private final Long bountyId = 100L;

  private User createUser(UserStatus status) {
    return User.create().setId(userId).setStatus(status).setLocationId(mapId).setLevel(5);
  }

  private MapNode createMapNode() {
    MapNode node = MapNode.create();
    node.setId(mapId);
    node.setName("青山镇");
    return node;
  }

  private Bounty createBounty(int requireLevel, int durationMinutes) {
    Bounty bounty = new Bounty();
    bounty.setId(bountyId);
    bounty.setMapId(mapId);
    bounty.setName("采药童子");
    bounty.setDescription("药铺缺人手");
    bounty.setDurationMinutes(durationMinutes);
    bounty.setRequireLevel(requireLevel);
    bounty.setEventWeight(100);
    bounty.setRewards(List.of(new BountyRewardPool.RareItem(100, 1, 1, "培元丹")));
    return bounty;
  }

  private UserBounty createUserBounty(int durationMinutes, long minutesAgo) {
    UserBounty ub = new UserBounty();
    ub.setUserId(userId);
    ub.setBountyId(bountyId);
    ub.setBountyName("采药童子");
    ub.setStartTime(LocalDateTime.now().minusMinutes(minutesAgo));
    ub.setDurationMinutes(durationMinutes);
    ub.setRewards(List.of(new BountyRewardItem.ItemReward(1L, "培元丹", 1).toMap()));
    ub.setStatus("active");
    return ub;
  }

  // ===================== listBounties =====================

  @Test
  @DisplayName("listBounties — 非 IDLE 状态也能查看悬赏列表")
  void listBounties_inNonIdleStatus_shouldStillWork() {
    User user = createUser(UserStatus.BOUNTY);
    MapNode mapNode = createMapNode();
    Bounty bounty = createBounty(1, 5);

    when(userStateService.getUser(userId)).thenReturn(user);
    when(mapNodeRepository.findById(mapId)).thenReturn(Optional.of(mapNode));
    when(bountyRepository.findByMapId(mapId)).thenReturn(List.of(bounty));

    List<BountyVO> result = bountyService.listBounties(userId);

    assertEquals(1, result.size());
    assertEquals("采药童子", result.getFirst().name());
  }

  @Test
  @DisplayName("listBounties — 等级不足的悬赏被过滤")
  void listBounties_shouldFilterByLevel() {
    User user = createUser(UserStatus.IDLE).setLevel(1);
    MapNode mapNode = createMapNode();
    Bounty highBounty = createBounty(10, 5);
    when(userStateService.getUser(userId)).thenReturn(user);
    when(mapNodeRepository.findById(mapId)).thenReturn(Optional.of(mapNode));
    when(bountyRepository.findByMapId(mapId)).thenReturn(List.of(highBounty));

    List<BountyVO> result = bountyService.listBounties(userId);

    assertTrue(result.isEmpty());
  }

  // ===================== getBountyStatus =====================

  @Test
  @DisplayName("getBountyStatus — 返回进行中悬赏状态含描述")
  void getBountyStatus_shouldReturnStatusWithDescription() {
    UserBounty userBounty = createUserBounty(5, 3);
    Bounty bounty = createBounty(1, 5);
    when(userBountyRepository.findActiveByUserId(userId)).thenReturn(Optional.of(userBounty));
    when(bountyRepository.findById(bountyId)).thenReturn(Optional.of(bounty));

    BountyStatusVO result = bountyService.getBountyStatus(userId);

    assertEquals("采药童子", result.bountyName());
    assertEquals("药铺缺人手", result.description());
    assertEquals(3, result.minutesElapsed());
    assertEquals(2, result.minutesRemaining());
    assertEquals(5, result.durationMinutes());
  }

  @Test
  @DisplayName("getBountyStatus — 无进行中悬赏抛异常")
  void getBountyStatus_whenNoActiveBounty_shouldThrow() {
    when(userBountyRepository.findActiveByUserId(userId)).thenReturn(Optional.empty());

    assertThrows(IllegalStateException.class, () -> bountyService.getBountyStatus(userId));
  }

  @Test
  @DisplayName("getBountyStatus — 悬赏时间已到显示已可结算")
  void getBountyStatus_whenTimeReached_shouldIndicateCompletable() {
    UserBounty userBounty = createUserBounty(5, 10);
    Bounty bounty = createBounty(1, 5);
    when(userBountyRepository.findActiveByUserId(userId)).thenReturn(Optional.of(userBounty));
    when(bountyRepository.findById(bountyId)).thenReturn(Optional.of(bounty));

    BountyStatusVO result = bountyService.getBountyStatus(userId);

    assertTrue(result.minutesElapsed() >= result.durationMinutes());
    assertEquals(0, result.minutesRemaining());
  }

  // ===================== startBounty =====================

  @Test
  @DisplayName("startBounty — 非 IDLE 状态抛异常")
  void startBounty_whenNotIdle_shouldThrow() {
    User user = createUser(UserStatus.BOUNTY);
    when(userStateService.getUser(userId)).thenReturn(user);

    assertThrows(IllegalStateException.class, () -> bountyService.startBounty(userId, bountyId));
  }

  @Test
  @DisplayName("startBounty — 悬赏不属于当前地图抛异常")
  void startBounty_whenBountyNotInMap_shouldThrow() {
    User user = createUser(UserStatus.IDLE).setLocationId(999L);
    MapNode otherMap = MapNode.create();
    otherMap.setId(999L);
    otherMap.setName("其他地图");
    Bounty bounty = createBounty(1, 5);

    when(userStateService.getUser(userId)).thenReturn(user);
    when(bountyRepository.findById(bountyId)).thenReturn(Optional.of(bounty));
    when(mapNodeRepository.findById(999L)).thenReturn(Optional.of(otherMap));

    assertThrows(IllegalArgumentException.class, () -> bountyService.startBounty(userId, bountyId));
  }

  @Test
  @DisplayName("startBounty — 等级不足抛异常")
  void startBounty_whenLevelTooLow_shouldThrow() {
    User user = createUser(UserStatus.IDLE).setLevel(1);
    MapNode mapNode = createMapNode();
    Bounty bounty = createBounty(10, 5);

    when(userStateService.getUser(userId)).thenReturn(user);
    when(bountyRepository.findById(bountyId)).thenReturn(Optional.of(bounty));
    when(mapNodeRepository.findById(mapId)).thenReturn(Optional.of(mapNode));

    assertThrows(IllegalArgumentException.class, () -> bountyService.startBounty(userId, bountyId));
  }

  // ===================== completeBounty =====================

  @Test
  @DisplayName("completeBounty — 非 BOUNTY 状态抛异常")
  void completeBounty_whenNotBounty_shouldThrow() {
    User user = createUser(UserStatus.IDLE);
    when(userStateService.getUser(userId)).thenReturn(user);

    assertThrows(IllegalStateException.class, () -> bountyService.completeBounty(userId));
  }

  @Test
  @DisplayName("completeBounty — 时间未到抛异常")
  void completeBounty_whenTimeNotReached_shouldThrow() {
    User user = createUser(UserStatus.BOUNTY);
    UserBounty userBounty = createUserBounty(10, 3);

    when(userStateService.getUser(userId)).thenReturn(user);
    when(userBountyRepository.findActiveByUserId(userId)).thenReturn(Optional.of(userBounty));

    assertThrows(IllegalArgumentException.class, () -> bountyService.completeBounty(userId));
  }

  // ===================== abandonBounty =====================

  @Test
  @DisplayName("abandonBounty — 非 BOUNTY 状态抛异常")
  void abandonBounty_whenNotBounty_shouldThrow() {
    User user = createUser(UserStatus.IDLE);
    when(userStateService.getUser(userId)).thenReturn(user);

    assertThrows(IllegalStateException.class, () -> bountyService.abandonBounty(userId));
  }

  @Test
  @DisplayName("abandonBounty — 成功放弃后恢复 IDLE")
  void abandonBounty_shouldRestoreIdle() {
    User user = createUser(UserStatus.BOUNTY);
    UserBounty userBounty = createUserBounty(5, 3);

    when(userStateService.getUser(userId)).thenReturn(user);
    when(userBountyRepository.findActiveByUserId(userId)).thenReturn(Optional.of(userBounty));

    String result = bountyService.abandonBounty(userId);

    assertEquals(UserStatus.IDLE, user.getStatus());
    assertEquals("abandoned", userBounty.getStatus());
  }

  // ===================== Reward Range =====================

  @Test
  @DisplayName("startBounty — RareItem 奖励支持 min~max 范围")
  void startBounty_rewardShouldSupportRange() {
    User user = createUser(UserStatus.IDLE);
    MapNode mapNode = createMapNode();
    mapNode.setSpecialties(List.of(new SpecialtyEntry(1L, 100)));
    Bounty bounty = createBounty(3, 5);
    bounty.setRewards(List.of(new BountyRewardPool.RareItem(100, 2, 5, "培元丹")));

    ItemTemplate template = new ItemTemplate();
    template.setId(1L);
    template.setName("培元丹");
    template.setType(ItemType.MATERIAL);

    when(userStateService.getUser(userId)).thenReturn(user);
    when(bountyRepository.findById(bountyId)).thenReturn(Optional.of(bounty));
    when(mapNodeRepository.findById(mapId)).thenReturn(Optional.of(mapNode));
    when(itemTemplateRepository.findByIds(anyList())).thenReturn(List.of(template));

    String msg = bountyService.startBounty(userId, bountyId);

    assertTrue(msg.contains("采药童子"));
  }
}
