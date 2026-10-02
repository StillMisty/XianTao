package top.stillmisty.xiantao.service.ai;

import java.util.List;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;
import top.stillmisty.xiantao.domain.chat.enums.ChatType;
import top.stillmisty.xiantao.domain.sect.entity.Sect;
import top.stillmisty.xiantao.domain.sect.entity.SectMember;
import top.stillmisty.xiantao.domain.user.entity.Player;
import top.stillmisty.xiantao.infrastructure.repository.SectMemberRepository;
import top.stillmisty.xiantao.infrastructure.repository.SectRepository;
import top.stillmisty.xiantao.infrastructure.repository.UserRepository;
import top.stillmisty.xiantao.service.BusinessException;
import top.stillmisty.xiantao.service.ErrorCode;
import top.stillmisty.xiantao.service.ServiceResult;
import top.stillmisty.xiantao.service.sect.SectBuildingService;
import top.stillmisty.xiantao.service.sect.SectEventGenerator;

/** 宗灵对话核心服务 宗灵是宗门意志的化身，LLM 驱动，成员通过自然语言与宗灵对话来执行所有宗门操作 */
@Service
public class SectSpiritChatService extends AbstractChatService {

  private static final ChatReplies REPLIES =
      new ChatReplies("宗灵暂时无法回应，请稍后再试。", "宗门操作失败", "宗灵暂时无法回应，请稍后再试。");

  private final SectRepository sectRepository;
  private final SectMemberRepository sectMemberRepository;
  private final SectMemberTools sectMemberTools;
  private final SectElderTools sectElderTools;
  private final SectLeaderTools sectLeaderTools;
  private final SectBuildingService sectBuildingService;
  private final SectEventGenerator sectEventGenerator;
  private final UserRepository userRepository;
  private final SectPromptTemplates promptTemplates;

  public SectSpiritChatService(
      ChatClient sectChatClient,
      ChatMemory chatMemory,
      AiChatRateLimiter rateLimiter,
      SectRepository sectRepository,
      SectMemberRepository sectMemberRepository,
      SectMemberTools sectMemberTools,
      SectElderTools sectElderTools,
      SectLeaderTools sectLeaderTools,
      SectBuildingService sectBuildingService,
      SectEventGenerator sectEventGenerator,
      UserRepository userRepository,
      SectPromptTemplates promptTemplates) {
    super(sectChatClient, chatMemory, rateLimiter);
    this.sectRepository = sectRepository;
    this.sectMemberRepository = sectMemberRepository;
    this.sectMemberTools = sectMemberTools;
    this.sectElderTools = sectElderTools;
    this.sectLeaderTools = sectLeaderTools;
    this.sectBuildingService = sectBuildingService;
    this.sectEventGenerator = sectEventGenerator;
    this.userRepository = userRepository;
    this.promptTemplates = promptTemplates;
  }

  public ServiceResult<String> chatWithSectSpirit(Long userId, String userInput) {
    return converse(userId, REPLIES, () -> buildTurn(userId, userInput));
  }

  private ChatTurn buildTurn(Long userId, String userInput) {
    SectMember member =
        sectMemberRepository
            .findByUserId(userId)
            .filter(m -> m.getSectId() != null)
            .orElseThrow(() -> new BusinessException(ErrorCode.SECT_NOT_IN));

    Long sectId = member.getSectId();
    if (sectId == null) throw new BusinessException(ErrorCode.SECT_NOT_IN);
    Sect sect =
        sectRepository
            .findById(sectId)
            .orElseThrow(() -> new BusinessException(ErrorCode.SECT_NOT_FOUND));

    // 对话前刷新：灵脉结算与宗门动态事件
    sectBuildingService.settleSpiritVein(sect.getId());
    sectEventGenerator.ensureEvent(sect);

    List<Object> tools =
        switch (member.getPosition()) {
          case MEMBER -> List.of(sectMemberTools);
          case ELDER -> List.of(sectMemberTools, sectElderTools);
          case LEADER -> List.of(sectMemberTools, sectElderTools, sectLeaderTools);
        };

    return new ChatTurn(
        ChatType.SECT,
        userId,
        sect.getId(),
        buildPrompt(sect, member),
        userInput,
        tools,
        null,
        null);
  }

  private String buildPrompt(Sect sect, SectMember member) {
    Player user =
        userRepository
            .findById(member.getUserId())
            .orElseThrow(() -> new BusinessException(ErrorCode.SECT_NOT_IN));
    long memberCount = sectMemberRepository.countBySectId(sect.getId());
    return promptTemplates.buildSectPrompt(sect, member, user, memberCount);
  }
}
