package top.stillmisty.xiantao.service.ai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;
import top.stillmisty.xiantao.domain.sect.entity.Sect;
import top.stillmisty.xiantao.domain.sect.entity.SectMember;
import top.stillmisty.xiantao.domain.sect.enums.ChatType;
import top.stillmisty.xiantao.domain.sect.repository.SectMemberRepository;
import top.stillmisty.xiantao.domain.sect.repository.SectRepository;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.domain.user.repository.UserRepository;
import top.stillmisty.xiantao.service.BusinessException;
import top.stillmisty.xiantao.service.ErrorCode;
import top.stillmisty.xiantao.service.ServiceResult;
import top.stillmisty.xiantao.service.UserContext;
import top.stillmisty.xiantao.service.annotation.Authenticated;
import top.stillmisty.xiantao.service.sect.SectBuildingService;

/** 宗灵对话核心服务 宗灵是宗门意志的化身，LLM 驱动，成员通过自然语言与宗灵对话来执行所有宗门操作 */
@Service
@Slf4j
public class SectSpiritChatService extends AbstractChatService {

  private final SectRepository sectRepository;
  private final SectMemberRepository sectMemberRepository;
  private final SectSpiritTools sectSpiritTools;
  private final SectBuildingService sectBuildingService;
  private final UserRepository userRepository;

  public SectSpiritChatService(
      ChatClient sectChatClient,
      ChatMemory chatMemory,
      SectRepository sectRepository,
      SectMemberRepository sectMemberRepository,
      SectSpiritTools sectSpiritTools,
      SectBuildingService sectBuildingService,
      UserRepository userRepository) {
    super(sectChatClient, chatMemory);
    this.sectRepository = sectRepository;
    this.sectMemberRepository = sectMemberRepository;
    this.sectSpiritTools = sectSpiritTools;
    this.sectBuildingService = sectBuildingService;
    this.userRepository = userRepository;
  }

  @Authenticated
  public ServiceResult<String> chatWithSectSpirit(
      PlatformType platform, String openId, String userInput) {
    Long userId = UserContext.getCurrentUserId();
    return new ServiceResult.Success<>(chatWithSectSpirit(userId, userInput));
  }

  public String chatWithSectSpirit(Long userId, String userInput) {
    try {
      SectMember member =
          sectMemberRepository
              .findByUserId(userId)
              .filter(m -> m.getSectId() != null)
              .orElseThrow(() -> new BusinessException(ErrorCode.SECT_NOT_IN));

      Sect sect =
          sectRepository
              .findById(member.getSectId())
              .orElseThrow(() -> new BusinessException(ErrorCode.SECT_NOT_FOUND));

      sectBuildingService.settleSpiritVein(sect.getId());

      String response =
          callLlm(
              buildPrompt(sect, member),
              userInput,
              ChatType.SECT,
              userId,
              sect.getId(),
              sectSpiritTools);

      log.debug("宗灵对话成功 - userId: {}, sect: {}, input: {}", userId, sect.getName(), userInput);
      return response != null ? response : "宗灵暂时无法回应，请稍后再试。";
    } catch (BusinessException e) {
      return e.getMessage();
    } catch (Exception e) {
      log.error("宗灵对话失败 - userId: {}, error: {}", userId, e.getMessage(), e);
      return "宗灵暂时无法回应，请稍后再试。";
    }
  }

  private String buildPrompt(Sect sect, SectMember member) {
    User user =
        userRepository
            .findById(member.getUserId())
            .orElseThrow(() -> new BusinessException(ErrorCode.SECT_NOT_IN));
    StringBuilder prompt = new StringBuilder();

    prompt.append(
        """
        你是【%s】的宗灵，是宗门本身的意志化身。
        宗灵身份：%s

        【宗门信息】
        - 宗门名称：%s
        - 道统：%s
        - 等级：Lv.%d
        - 资金：%d 灵石
        - 成员：%d/%d
        - 宗主ID：%d

        【当前成员】
        - 道号：%s
        - 职位：%s
        - 贡献值：%d

        【规则】
        1. 你是宗门的意志化身，不是长老也不是NPC，是宗门本身
        2. 成员通过自然语言与你交流，你根据宗门需求调用工具
        3. 严格执行权限控制——根据成员的职位选择性地使用工具
        4. 操作完成后根据执行结果生成人格化回复
        5. 保持宗灵的身份语气，根据宗门道统和宗灵人格种子调整说话风格
        6. 成员问话时，根据问题类型调用相应工具，不要一次性把所有信息都展示出来
        """
            .formatted(
                sect.getName(),
                sect.getSpiritPersonality() != null ? sect.getSpiritPersonality() : "沉稳的宗门意志",
                sect.getName(),
                sect.getEthos() != null ? sect.getEthos() : "",
                sect.getLevel(),
                sect.getFunds(),
                sectMemberRepository.countBySectId(sect.getId()),
                sect.getMaxMembers(),
                sect.getLeaderId(),
                user.getNickname(),
                member.getPosition().getName(),
                member.getContribution()));

    if (sect.getVerse() != null && !sect.getVerse().isBlank()) {
      prompt.append("\n诗号：").append(sect.getVerse());
    }

    if (sect.getNotice() != null && !sect.getNotice().isBlank()) {
      prompt.append("\n公告：").append(sect.getNotice());
    }

    if (sect.getLastEventText() != null && !sect.getLastEventText().isBlank()) {
      prompt.append("\n当前宗门事件：").append(sect.getLastEventText());
    }

    return prompt.toString();
  }
}
