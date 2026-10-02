package top.stillmisty.xiantao.service.dungeon;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import top.stillmisty.xiantao.domain.dungeon.entity.DungeonTemplate;
import top.stillmisty.xiantao.domain.sect.entity.SectMember;
import top.stillmisty.xiantao.domain.user.entity.Player;
import top.stillmisty.xiantao.infrastructure.repository.DungeonProgressRepository;
import top.stillmisty.xiantao.infrastructure.repository.HiddenCompletionRepository;
import top.stillmisty.xiantao.infrastructure.repository.SectMemberRepository;
import top.stillmisty.xiantao.infrastructure.repository.StackableItemRepository;
import top.stillmisty.xiantao.service.BusinessException;
import top.stillmisty.xiantao.service.ErrorCode;

class DungeonAccessCheckerTest {

  private static final long SECT_ID = 5L;

  @Test
  void sectMemberOfRequiredSectCanAccess() {
    SectMemberRepository memberRepository = mock(SectMemberRepository.class);
    when(memberRepository.findByUserId(1L))
        .thenReturn(Optional.of(SectMember.create().setUserId(1L).setSectId(SECT_ID)));

    assertTrue(checker(memberRepository).canAccess(player(1L), dungeonWithSect(SECT_ID)));
  }

  @Test
  void playerWithoutSectIsRejected() {
    SectMemberRepository memberRepository = mock(SectMemberRepository.class);
    when(memberRepository.findByUserId(2L)).thenReturn(Optional.empty());

    DungeonAccessChecker checker = checker(memberRepository);
    assertFalse(checker.canAccess(player(2L), dungeonWithSect(SECT_ID)));
    BusinessException error =
        assertThrows(
            BusinessException.class,
            () -> checker.checkAccess(player(2L), dungeonWithSect(SECT_ID)));
    assertEquals(ErrorCode.DUNGEON_SECT_RESTRICTED, error.getErrorCode());
  }

  @Test
  void memberOfAnotherSectIsRejected() {
    SectMemberRepository memberRepository = mock(SectMemberRepository.class);
    when(memberRepository.findByUserId(3L))
        .thenReturn(Optional.of(SectMember.create().setUserId(3L).setSectId(SECT_ID + 1)));

    assertFalse(checker(memberRepository).canAccess(player(3L), dungeonWithSect(SECT_ID)));
  }

  @Test
  void conditionWithoutSectIdAcceptsAnySectMember() {
    SectMemberRepository memberRepository = mock(SectMemberRepository.class);
    when(memberRepository.findByUserId(4L))
        .thenReturn(Optional.of(SectMember.create().setUserId(4L).setSectId(SECT_ID)));
    when(memberRepository.findByUserId(5L)).thenReturn(Optional.empty());

    DungeonTemplate anySectDungeon = new DungeonTemplate();
    anySectDungeon.setAccessRules(
        List.of(
            new DungeonTemplate.AccessCondition("SECT", null, null, null, null, null, null, null)));

    DungeonAccessChecker checker = checker(memberRepository);
    assertDoesNotThrow(() -> checker.checkAccess(player(4L), anySectDungeon));
    assertFalse(checker.canAccess(player(5L), anySectDungeon));
  }

  private static DungeonAccessChecker checker(SectMemberRepository memberRepository) {
    return new DungeonAccessChecker(
        mock(StackableItemRepository.class),
        mock(DungeonProgressRepository.class),
        mock(HiddenCompletionRepository.class),
        memberRepository);
  }

  private static DungeonTemplate dungeonWithSect(long sectId) {
    DungeonTemplate dungeon = new DungeonTemplate();
    dungeon.setAccessRules(
        List.of(
            new DungeonTemplate.AccessCondition(
                "SECT", null, null, null, sectId, null, null, null)));
    return dungeon;
  }

  private static Player player(long userId) {
    Player player = new Player();
    player.setId(userId);
    return player;
  }
}
