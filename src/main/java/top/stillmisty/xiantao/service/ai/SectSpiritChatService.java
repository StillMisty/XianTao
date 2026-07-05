package top.stillmisty.xiantao.service.ai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;
import top.stillmisty.xiantao.domain.sect.entity.Sect;
import top.stillmisty.xiantao.domain.sect.entity.SectMember;
import top.stillmisty.xiantao.domain.sect.enums.ChatType;
import top.stillmisty.xiantao.domain.user.entity.Player;
import top.stillmisty.xiantao.infrastructure.repository.SectMemberRepository;
import top.stillmisty.xiantao.infrastructure.repository.SectRepository;
import top.stillmisty.xiantao.infrastructure.repository.UserRepository;
import top.stillmisty.xiantao.service.BusinessException;
import top.stillmisty.xiantao.service.ErrorCode;
import top.stillmisty.xiantao.service.ServiceResult;
import top.stillmisty.xiantao.service.sect.SectBuildingService;

/** 宗灵对话核心服务 宗灵是宗门意志的化身，LLM 驱动，成员通过自然语言与宗灵对话来执行所有宗门操作 */
@Service
@Slf4j
public class SectSpiritChatService extends AbstractChatService {

  private final SectRepository sectRepository;
  private final SectMemberRepository sectMemberRepository;
  private final SectMemberTools sectMemberTools;
  private final SectElderTools sectElderTools;
  private final SectLeaderTools sectLeaderTools;
  private final SectBuildingService sectBuildingService;
  private final UserRepository userRepository;
  private final SectPromptTemplates promptTemplates;

  public SectSpiritChatService(
      ChatClient sectChatClient,
      ChatMemory chatMemory,
      SectRepository sectRepository,
      SectMemberRepository sectMemberRepository,
      SectMemberTools sectMemberTools,
      SectElderTools sectElderTools,
      SectLeaderTools sectLeaderTools,
      SectBuildingService sectBuildingService,
      UserRepository userRepository,
      SectPromptTemplates promptTemplates) {
    super(sectChatClient, chatMemory);
    this.sectRepository = sectRepository;
    this.sectMemberRepository = sectMemberRepository;
    this.sectMemberTools = sectMemberTools;
    this.sectElderTools = sectElderTools;
    this.sectLeaderTools = sectLeaderTools;
    this.sectBuildingService = sectBuildingService;
    this.userRepository = userRepository;
    this.promptTemplates = promptTemplates;
  }

  public ServiceResult<String> chatWithSectSpirit(Long userId, String userInput) {
    try {
      String result = chatWithSectSpiritInternal(userId, userInput);
      return new ServiceResult.Success<>(result);
    } catch (BusinessException e) {
      return ServiceResult.businessFailure(e.getMessage() != null ? e.getMessage() : "宗门操作失败");
    } catch (Exception e) {
      log.error("宗灵对话失败 - userId: {}, error: {}", userId, e.getMessage(), e);
      return ServiceResult.businessFailure("宗灵暂时无法回应，请稍后再试。");
    }
  }

  String chatWithSectSpiritInternal(Long userId, String userInput) {
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

    sectBuildingService.settleSpiritVein(sect.getId());

    String response =
        switch (member.getPosition()) {
          case MEMBER ->
              callLlm(
                  buildPrompt(sect, member),
                  userInput,
                  ChatType.SECT,
                  userId,
                  sect.getId(),
                  sectMemberTools);
          case ELDER ->
              callLlm(
                  buildPrompt(sect, member),
                  userInput,
                  ChatType.SECT,
                  userId,
                  sect.getId(),
                  sectMemberTools,
                  sectElderTools);
          case LEADER ->
              callLlm(
                  buildPrompt(sect, member),
                  userInput,
                  ChatType.SECT,
                  userId,
                  sect.getId(),
                  sectMemberTools,
                  sectElderTools,
                  sectLeaderTools);
        };

    log.debug("宗灵对话成功 - userId: {}, sect: {}, input: {}", userId, sect.getName(), userInput);
    return response != null ? response : "宗灵暂时无法回应，请稍后再试。";
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
