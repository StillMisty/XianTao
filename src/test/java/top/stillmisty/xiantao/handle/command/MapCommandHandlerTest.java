package top.stillmisty.xiantao.handle.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import top.stillmisty.xiantao.domain.map.enums.MapType;
import top.stillmisty.xiantao.domain.map.vo.MapInfoVO;
import top.stillmisty.xiantao.handle.NextActions;
import top.stillmisty.xiantao.infrastructure.repository.EquipmentTemplateRepository;
import top.stillmisty.xiantao.infrastructure.repository.ItemTemplateRepository;
import top.stillmisty.xiantao.service.ServiceResult;
import top.stillmisty.xiantao.service.UserContext;
import top.stillmisty.xiantao.service.bounty.BountyService;
import top.stillmisty.xiantao.service.combat.TrainingService;
import top.stillmisty.xiantao.service.map.MapService;
import top.stillmisty.xiantao.service.map.TravelService;
import top.stillmisty.xiantao.util.TextFormat;

class MapCommandHandlerTest {

  @Test
  void mapReplySuggestsAdjacentDestinations() {
    MapService mapService = mock(MapService.class);
    when(mapService.getCurrentMapInfo(1L))
        .thenReturn(
            new ServiceResult.Success<>(
                MapInfoVO.builder()
                    .name("青石镇")
                    .mapType(MapType.SAFE_TOWN)
                    .levelRequirement(1)
                    .adjacentMapNames(List.of("翠竹林", "黑风岭"))
                    .build()));
    MapCommandHandler handler = handler(mapService);

    NextActions.Collected<String> collected =
        NextActions.collect(
            () -> UserContext.withUser(1L, () -> handler.handleMap(TextFormat.get())));

    assertTrue(collected.value().contains("四方可达"), collected.value());
    assertEquals(
        List.of("前往 翠竹林", "前往 黑风岭"),
        collected.suggestions().stream().map(NextActions.Suggestion::command).toList());
  }

  @Test
  void noAdjacentMapsMeansNoSuggestions() {
    MapService mapService = mock(MapService.class);
    when(mapService.getCurrentMapInfo(1L))
        .thenReturn(
            new ServiceResult.Success<>(
                MapInfoVO.builder()
                    .name("青石镇")
                    .mapType(MapType.SAFE_TOWN)
                    .levelRequirement(1)
                    .adjacentMapNames(List.of())
                    .build()));

    NextActions.Collected<String> collected =
        NextActions.collect(
            () -> UserContext.withUser(1L, () -> handler(mapService).handleMap(TextFormat.get())));

    assertEquals(0, collected.suggestions().size());
  }

  private static MapCommandHandler handler(MapService mapService) {
    return new MapCommandHandler(
        mapService,
        mock(TravelService.class),
        mock(TrainingService.class),
        mock(BountyService.class),
        mock(ItemTemplateRepository.class),
        mock(EquipmentTemplateRepository.class));
  }
}
