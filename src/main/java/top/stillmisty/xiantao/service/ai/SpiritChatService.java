package top.stillmisty.xiantao.service.ai;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;
import top.stillmisty.xiantao.domain.fudi.entity.Fudi;
import top.stillmisty.xiantao.domain.fudi.entity.Spirit;
import top.stillmisty.xiantao.domain.fudi.entity.SpiritForm;
import top.stillmisty.xiantao.domain.sect.enums.ChatType;
import top.stillmisty.xiantao.infrastructure.repository.FudiRepository;
import top.stillmisty.xiantao.infrastructure.repository.SpiritFormRepository;
import top.stillmisty.xiantao.infrastructure.repository.SpiritRepository;
import top.stillmisty.xiantao.service.BusinessException;
import top.stillmisty.xiantao.service.ErrorCode;
import top.stillmisty.xiantao.service.ServiceResult;

@Service
@Slf4j
public class SpiritChatService extends AbstractChatService {

  private final FudiRepository fudiRepository;
  private final SpiritRepository spiritRepository;
  private final SpiritFormRepository spiritFormRepository;
  private final SpiritPromptTemplates promptTemplates;
  private final SpiritTools spiritTools;
  private final FudiStateBuilder fudiStateBuilder;

  public SpiritChatService(
      ChatClient spiritChatClient,
      ChatMemory chatMemory,
      FudiRepository fudiRepository,
      SpiritRepository spiritRepository,
      SpiritFormRepository spiritFormRepository,
      SpiritPromptTemplates promptTemplates,
      SpiritTools spiritTools,
      FudiStateBuilder fudiStateBuilder) {
    super(spiritChatClient, chatMemory);
    this.fudiRepository = fudiRepository;
    this.spiritRepository = spiritRepository;
    this.spiritFormRepository = spiritFormRepository;
    this.promptTemplates = promptTemplates;
    this.spiritTools = spiritTools;
    this.fudiStateBuilder = fudiStateBuilder;
  }

  public ServiceResult<String> chatWithSpirit(Long userId, String userInput) {
    try {
      String result = chatWithSpiritInternal(userId, userInput);
      return new ServiceResult.Success<>(result != null ? result : "地灵暂时无法回应，请稍后再试。");
    } catch (BusinessException e) {
      return ServiceResult.businessFailure(e.getMessage() != null ? e.getMessage() : "地灵操作失败");
    } catch (Exception e) {
      log.error("地灵对话失败 - userId: {}, error: {}", userId, e.getMessage(), e);
      return ServiceResult.businessFailure("地灵暂时无法回应，请稍后再试。");
    }
  }

  @Nullable String chatWithSpiritInternal(Long userId, String userInput) {
    Fudi fudi =
        fudiRepository
            .findByUserId(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.FUDI_NOT_FOUND));
    Spirit spirit =
        spiritRepository
            .findByFudiId(fudi.getId())
            .orElseThrow(() -> new BusinessException(ErrorCode.SPIRIT_NOT_FOUND));

    fudi.touchOnlineTime();
    spiritRepository.save(spirit);

    String response =
        callLlm(
            buildPrompt(fudi, spirit),
            userInput,
            ChatType.SPIRIT,
            userId,
            fudi.getId(),
            spiritTools);

    log.debug("地灵对话成功 - userId: {}, mbti: {}, input: {}", userId, spirit.getMbtiType(), userInput);
    return response;
  }

  private String buildPrompt(Fudi fudi, Spirit spirit) {
    String cellDetail = fudiStateBuilder.buildCellDetailForLLM(fudi);
    String formName = null;
    if (spirit.getFormId() != null) {
      formName =
          spiritFormRepository.findById(spirit.getFormId()).map(SpiritForm::getName).orElse(null);
    }

    return promptTemplates.buildSpiritPrompt(
        spirit.getMbtiType(),
        fudi.getTribulationStage(),
        spirit.getAffection(),
        cellDetail,
        formName != null ? formName : "未知形态");
  }
}
