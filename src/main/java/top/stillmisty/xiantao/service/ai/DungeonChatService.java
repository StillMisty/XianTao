package top.stillmisty.xiantao.service.ai;

import java.util.List;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;
import top.stillmisty.xiantao.domain.chat.enums.ChatType;
import top.stillmisty.xiantao.domain.dungeon.entity.DungeonInstance;
import top.stillmisty.xiantao.domain.dungeon.entity.DungeonSpiritState;
import top.stillmisty.xiantao.domain.dungeon.entity.DungeonTemplate;
import top.stillmisty.xiantao.domain.user.entity.Player;
import top.stillmisty.xiantao.infrastructure.repository.DungeonInstanceRepository;
import top.stillmisty.xiantao.infrastructure.repository.DungeonProgressRepository;
import top.stillmisty.xiantao.infrastructure.repository.DungeonSpiritStateRepository;
import top.stillmisty.xiantao.infrastructure.repository.DungeonTemplateRepository;
import top.stillmisty.xiantao.service.BusinessException;
import top.stillmisty.xiantao.service.ErrorCode;
import top.stillmisty.xiantao.service.ServiceResult;
import top.stillmisty.xiantao.service.player.PlayerLoader;

@Service
public class DungeonChatService extends AbstractChatService {

  private static final ChatReplies REPLIES =
      new ChatReplies("秘境之灵暂时无法回应...", "秘境操作失败", "秘境之灵暂时无法回应，请稍后再试。");

  private final DungeonTemplateRepository dungeonTemplateRepository;
  private final DungeonInstanceRepository instanceRepository;
  private final DungeonSpiritStateRepository spiritStateRepository;
  private final DungeonProgressRepository progressRepository;
  private final DungeonStateBuilder stateBuilder;
  private final DungeonExplorationTools dungeonExplorationTools;
  private final DungeonNavigationTools dungeonNavigationTools;
  private final DungeonFavorTools dungeonFavorTools;
  private final DungeonSpiritStateHelper spiritStateHelper;
  private final PlayerLoader playerLoader;

  public DungeonChatService(
      ChatClient dungeonChatClient,
      ChatMemory chatMemory,
      AiChatRateLimiter rateLimiter,
      DungeonTemplateRepository dungeonTemplateRepository,
      DungeonInstanceRepository instanceRepository,
      DungeonSpiritStateRepository spiritStateRepository,
      DungeonProgressRepository progressRepository,
      DungeonStateBuilder stateBuilder,
      DungeonExplorationTools dungeonExplorationTools,
      DungeonNavigationTools dungeonNavigationTools,
      DungeonFavorTools dungeonFavorTools,
      DungeonSpiritStateHelper spiritStateHelper,
      PlayerLoader playerLoader) {
    super(dungeonChatClient, chatMemory, rateLimiter);
    this.dungeonTemplateRepository = dungeonTemplateRepository;
    this.instanceRepository = instanceRepository;
    this.spiritStateRepository = spiritStateRepository;
    this.progressRepository = progressRepository;
    this.stateBuilder = stateBuilder;
    this.dungeonExplorationTools = dungeonExplorationTools;
    this.dungeonNavigationTools = dungeonNavigationTools;
    this.dungeonFavorTools = dungeonFavorTools;
    this.spiritStateHelper = spiritStateHelper;
    this.playerLoader = playerLoader;
  }

  public ServiceResult<String> chatWithDungeon(Long userId, String userInput) {
    return converse(userId, REPLIES, () -> buildTurn(userId, userInput));
  }

  private ChatTurn buildTurn(Long userId, String userInput) {
    Player user = playerLoader.load(userId);
    if (user.getActivityTargetId() == null) {
      throw new BusinessException(ErrorCode.DUNGEON_NO_ACTIVE_INSTANCE);
    }

    DungeonInstance instance =
        instanceRepository
            .findById(user.getActivityTargetId())
            .filter(DungeonInstance::isActive)
            .orElseThrow(() -> new BusinessException(ErrorCode.DUNGEON_NO_ACTIVE_INSTANCE));

    DungeonTemplate dungeon =
        dungeonTemplateRepository
            .findById(instance.getDungeonId())
            .orElseThrow(() -> new BusinessException(ErrorCode.DUNGEON_NOT_FOUND, ""));

    DungeonSpiritState spiritState = null;
    if (dungeon.hasSpirit()) {
      spiritState = findOrCreateSpiritState(instance, userId);
    }

    String systemPrompt = stateBuilder.buildSystemPrompt(dungeon, instance, spiritState);
    Long dungeonId = dungeon.getId();
    return new ChatTurn(
        ChatType.DUNGEON,
        userId,
        instance.getId(),
        systemPrompt,
        userInput,
        List.of(dungeonExplorationTools, dungeonNavigationTools, dungeonFavorTools),
        new DungeonChatContext(user, instance, dungeon, spiritState),
        // 互动计数原子累加，无需先查后改；LLM 调用成功后计入
        () -> progressRepository.incrementInteractionCount(userId, dungeonId));
  }

  public String buildStatusOverview(Long userId) {
    Player user = playerLoader.load(userId);
    if (user.getActivityTargetId() == null) {
      return "你当前不在任何秘境中。";
    }

    DungeonInstance instance =
        instanceRepository
            .findById(user.getActivityTargetId())
            .filter(DungeonInstance::isActive)
            .orElse(null);
    if (instance == null) {
      return "你当前不在任何秘境中。";
    }

    DungeonTemplate dungeon =
        dungeonTemplateRepository.findById(instance.getDungeonId()).orElse(null);
    if (dungeon == null) {
      return "秘境数据异常。";
    }

    DungeonSpiritState spiritState =
        spiritStateRepository.findByInstanceIdAndUserId(instance.getId(), userId).orElse(null);

    return stateBuilder.buildStatusOverview(dungeon, instance, spiritState);
  }

  private DungeonSpiritState findOrCreateSpiritState(DungeonInstance instance, Long userId) {
    return spiritStateHelper.findOrCreate(instance.getId(), instance.getDungeonId(), userId);
  }
}
