package top.stillmisty.xiantao.service.sect;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import top.stillmisty.xiantao.domain.sect.entity.SectMember;
import top.stillmisty.xiantao.infrastructure.repository.SectMemberRepository;

/** 宗门查询服务 — 轻量跨服务接口，避免注入整个 SectMemberService */
@Service
@RequiredArgsConstructor
public class SectQueryService {

  private final SectMemberRepository sectMemberRepository;

  public boolean isInSect(Long userId) {
    Optional<SectMember> member = sectMemberRepository.findByUserId(userId);
    return member.isPresent() && member.get().getSectId() != null;
  }

  public @Nullable Long getSectId(Long userId) {
    return sectMemberRepository.findByUserId(userId).map(SectMember::getSectId).orElse(null);
  }

  public boolean isInSameSect(Long userIdA, Long userIdB) {
    Optional<SectMember> memberA = sectMemberRepository.findByUserId(userIdA);
    Optional<SectMember> memberB = sectMemberRepository.findByUserId(userIdB);
    if (memberA.isEmpty() || memberB.isEmpty()) {
      return false;
    }
    Long sectA = memberA.get().getSectId();
    Long sectB = memberB.get().getSectId();
    if (sectA == null || sectB == null) return false;
    return sectA.equals(sectB);
  }

  public boolean areBothRogue(Long userIdA, Long userIdB) {
    return (sectMemberRepository.findByUserId(userIdA).isEmpty()
        && sectMemberRepository.findByUserId(userIdB).isEmpty());
  }
}
