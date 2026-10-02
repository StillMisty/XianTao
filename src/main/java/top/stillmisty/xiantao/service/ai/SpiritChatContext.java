package top.stillmisty.xiantao.service.ai;

import java.util.List;
import top.stillmisty.xiantao.domain.fudi.entity.Fudi;
import top.stillmisty.xiantao.domain.fudi.entity.Spirit;
import top.stillmisty.xiantao.domain.worldevent.entity.WorldEvent;

/** 单次地灵对话的预加载数据：福地、地灵和进行中事件，避免每次 Tool 调用都重复查询。 */
public record SpiritChatContext(Fudi fudi, Spirit spirit, List<WorldEvent> activeEvents) {}
