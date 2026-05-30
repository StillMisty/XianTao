package top.stillmisty.xiantao.service.ai;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;
import top.stillmisty.xiantao.domain.dungeon.entity.DungeonInstance;
import top.stillmisty.xiantao.domain.dungeon.entity.DungeonProgress;
import top.stillmisty.xiantao.domain.dungeon.entity.DungeonSpiritState;
import top.stillmisty.xiantao.domain.dungeon.entity.DungeonTemplate;
import top.stillmisty.xiantao.domain.sect.enums.ChatType;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.infrastructure.repository.DungeonInstanceRepository;
import top.stillmisty.xiantao.infrastructure.repository.DungeonProgressRepository;
import top.stillmisty.xiantao.infrastructure.repository.DungeonSpiritStateRepository;
import top.stillmisty.xiantao.infrastructure.repository.DungeonTemplateRepository;
import top.stillmisty.xiantao.infrastructure.util.TimeUtil;
import top.stillmisty.xiantao.service.BusinessException;
import top.stillmisty.xiantao.service.ErrorCode;
import top.stillmisty.xiantao.service.ServiceResult;
import top.stillmisty.xiantao.service.dungeon.DungeonSpiritStateHelper;
import top.stillmisty.xiantao.service.dungeon.DungeonStateBuilder;
import top.stillmisty.xiantao.service.dungeon.DungeonTools;
import top.stillmisty.xiantao.service.player.UserStateService;

@Slf4j
@Service
public class DungeonChatService extends AbstractChatService {

  private final DungeonTemplateRepository dungeonTemplateRepository;
  private final DungeonInstanceRepository instanceRepository;
  private final DungeonSpiritStateRepository spiritStateRepository;
  private final DungeonProgressRepository progressRepository;
  private final DungeonStateBuilder stateBuilder;
  private final DungeonTools dungeonTools;
  private final DungeonSpiritStateHelper spiritStateHelper;
  private final UserStateService userStateService;

  public DungeonChatService(
      ChatClient dungeonChatClient,
      ChatMemory chatMemory,
      DungeonTemplateRepository dungeonTemplateRepository,
      DungeonInstanceRepository instanceRepository,
      DungeonSpiritStateRepository spiritStateRepository,
      DungeonProgressRepository progressRepository,
      DungeonStateBuilder stateBuilder,
      DungeonTools dungeonTools,
      DungeonSpiritStateHelper spiritStateHelper,
      UserStateService userStateService) {
    super(dungeonChatClient, chatMemory);
    this.dungeonTemplateRepository = dungeonTemplateRepository;
    this.instanceRepository = instanceRepository;
    this.spiritStateRepository = spiritStateRepository;
    this.progressRepository = progressRepository;
    this.stateBuilder = stateBuilder;
    this.dungeonTools = dungeonTools;
    this.spiritStateHelper = spiritStateHelper;
    this.userStateService = userStateService;
  }

  public ServiceResult<String> chatWithDungeon(Long userId, String userInput) {
    try {
      String result = chatInternal(userId, userInput);
      return new ServiceResult.Success<>(result != null ? result : "秘境之灵暂时无法回应...");
    } catch (BusinessException e) {
      return ServiceResult.businessFailure(e.getMessage() != null ? e.getMessage() : "秘境操作失败");
    } catch (Exception e) {
      log.error("秘境对话失败 - userId: {}, error: {}", userId, e.getMessage(), e);
      return ServiceResult.businessFailure("秘境之灵暂时无法回应，请稍后再试。");
    }
  }

  @Nullable String chatInternal(Long userId, String userInput) {
    User user = userStateService.loadUser(userId);
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

    String response =
        callLlm(systemPrompt, userInput, ChatType.DUNGEON, userId, instance.getId(), dungeonTools);

    if (spiritState != null) {
      spiritStateRepository.save(spiritState);
    }

    DungeonProgress progress =
        progressRepository
            .findByUserIdAndDungeonId(userId, dungeon.getId())
            .orElseGet(
                () -> {
                  DungeonProgress p = new DungeonProgress();
                  p.setUserId(userId);
                  p.setDungeonId(dungeon.getId());
                  p.setRewardCount(0);
                  p.setDailyLimit(
                      DungeonProgress.calculateDailyLimit(
                          user.getLevel() != null ? user.getLevel() : 1));
                  p.setFirstClear(false);
                  p.setLastRewardDate(TimeUtil.today());
                  p.setInteractionCount(0);
                  return p;
                });
    progress.setInteractionCount(
        progress.getInteractionCount() != null ? progress.getInteractionCount() + 1 : 1);
    progressRepository.save(progress);

    return response;
  }

  public String buildStatusOverview(Long userId) {
    User user = userStateService.loadUser(userId);
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
