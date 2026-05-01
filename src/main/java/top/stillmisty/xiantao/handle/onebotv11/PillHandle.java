package top.stillmisty.xiantao.handle.onebotv11;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import love.forte.simbot.event.MessageEvent;
import love.forte.simbot.quantcat.common.annotations.ContentTrim;
import love.forte.simbot.quantcat.common.annotations.Filter;
import love.forte.simbot.quantcat.common.annotations.FilterValue;
import love.forte.simbot.quantcat.common.annotations.Listener;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.handle.command.PillCommandHandler;

import java.util.Arrays;
import java.util.List;

/**
 * 炼丹系统监听器
 * 处理丹方查询、学习、炼丹、服用等命令
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PillHandle {

    private final PillCommandHandler pillCommandHandler;

    /**
     * 处理丹方列表命令
     * 格式：丹方列表
     */
    @Listener
    @ContentTrim
    @Filter("丹方列表")
    public void recipeList(MessageEvent event) {
        log.debug("收到丹方列表查询请求 - AuthorId: {}", event.getAuthorId());
        String response = pillCommandHandler.handleRecipeList(
                PlatformType.ONE_BOT_V11,
                event.getAuthorId().toString()
        );
        event.replyBlocking(response);
    }

    /**
     * 处理丹方详情命令
     * 格式：丹方 [名称]
     * 示例：丹方 天元丹
     */
    @Listener
    @ContentTrim
    @Filter("丹方 {{recipeName}}")
    public void recipeDetail(MessageEvent event, @FilterValue("recipeName") String recipeName) {
        log.debug("收到丹方详情查询请求 - AuthorId: {}, RecipeName: {}", event.getAuthorId(), recipeName);
        String response = pillCommandHandler.handleRecipeDetail(
                PlatformType.ONE_BOT_V11,
                event.getAuthorId().toString(),
                recipeName
        );
        event.replyBlocking(response);
    }

    /**
     * 处理学习丹方命令
     * 格式：学丹方 [名称]
     * 示例：学丹方 天元丹
     */
    @Listener
    @ContentTrim
    @Filter("学丹方 {{recipeName}}")
    public void learnRecipe(MessageEvent event, @FilterValue("recipeName") String recipeName) {
        log.debug("收到学习丹方请求 - AuthorId: {}, RecipeName: {}", event.getAuthorId(), recipeName);
        String response = pillCommandHandler.handleLearnRecipe(
                PlatformType.ONE_BOT_V11,
                event.getAuthorId().toString(),
                recipeName
        );
        event.replyBlocking(response);
    }

    /**
     * 处理自动炼丹命令
     * 格式：炼 [丹方名]
     * 示例：炼 天元丹
     */
    @Listener
    @ContentTrim
    @Filter("炼 {{recipeName}}")
    public void refineAuto(MessageEvent event, @FilterValue("recipeName") String recipeName) {
        log.debug("收到自动炼丹请求 - AuthorId: {}, RecipeName: {}", event.getAuthorId(), recipeName);
        String response = pillCommandHandler.handleRefineAuto(
                PlatformType.ONE_BOT_V11,
                event.getAuthorId().toString(),
                recipeName
        );
        event.replyBlocking(response);
    }

    /**
     * 处理手动炼丹命令
     * 格式：炼 [药材1]×N [药材2]×M ...
     * 示例：炼 灵草×3 火灵花×2
     */
    @Listener
    @ContentTrim
    @Filter("炼 {{herbInput}}")
    public void refineManual(MessageEvent event, @FilterValue("herbInput") String herbInput) {
        log.debug("收到手动炼丹请求 - AuthorId: {}, HerbInput: {}", event.getAuthorId(), herbInput);
        // 解析药材输入
        List<String> herbInputs = Arrays.asList(herbInput.split("\\s+"));
        String response = pillCommandHandler.handleRefineManual(
                PlatformType.ONE_BOT_V11,
                event.getAuthorId().toString(),
                herbInputs
        );
        event.replyBlocking(response);
    }

    /**
     * 处理服用丹药命令
     * 格式：服用 [名称/编号]
     * 示例：服用 天元丹
     */
    @Listener
    @ContentTrim
    @Filter("服用 {{pillName}}")
    public void takePill(MessageEvent event, @FilterValue("pillName") String pillName) {
        log.debug("收到服用丹药请求 - AuthorId: {}, PillName: {}", event.getAuthorId(), pillName);
        String response = pillCommandHandler.handleTakePill(
                PlatformType.ONE_BOT_V11,
                event.getAuthorId().toString(),
                pillName
        );
        event.replyBlocking(response);
    }
}