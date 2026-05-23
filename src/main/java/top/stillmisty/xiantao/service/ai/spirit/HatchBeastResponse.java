package top.stillmisty.xiantao.service.ai.spirit;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/**
 * 灵兽孵化操作结果。
 *
 * <p>将兽卵放入指定兽栏孵化。孵化需要时间，成熟后可收取产出。 {@code quality} 为灵兽品质（普通/精良/稀有/史诗/传说），{@code tier} 为阶位（1-10）。
 */
public record HatchBeastResponse(
    @JsonPropertyDescription("孵化所在兽栏的地块编号") String position,
    @JsonPropertyDescription("孵出的灵兽名称") String beastName,
    @JsonPropertyDescription("灵兽品质：COMMON=普通, UNCOMMON=精良, RARE=稀有, EPIC=史诗, LEGENDARY=传说")
        String quality,
    @JsonPropertyDescription("灵兽阶位（1-10，越高越强）") int tier,
    @JsonPropertyDescription("孵化成熟所需小时数") long matureHours) {}
