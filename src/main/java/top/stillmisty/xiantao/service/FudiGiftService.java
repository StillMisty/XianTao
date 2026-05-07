package top.stillmisty.xiantao.service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.fudi.entity.Fudi;
import top.stillmisty.xiantao.domain.fudi.entity.Spirit;
import top.stillmisty.xiantao.domain.fudi.entity.SpiritForm;
import top.stillmisty.xiantao.domain.fudi.repository.FudiRepository;
import top.stillmisty.xiantao.domain.fudi.repository.SpiritFormRepository;
import top.stillmisty.xiantao.domain.fudi.repository.SpiritRepository;
import top.stillmisty.xiantao.domain.fudi.vo.GiveGiftVO;
import top.stillmisty.xiantao.domain.item.entity.ItemTemplate;
import top.stillmisty.xiantao.domain.item.entity.StackableItem;
import top.stillmisty.xiantao.domain.item.repository.ItemTemplateRepository;
import top.stillmisty.xiantao.domain.item.repository.StackableItemRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class FudiGiftService {

  private final FudiHelper fudiHelper;
  private final FudiRepository fudiRepository;
  private final SpiritRepository spiritRepository;
  private final SpiritFormRepository spiritFormRepository;
  private final StackableItemRepository stackableItemRepository;
  private final ItemTemplateRepository itemTemplateRepository;

  @Transactional
  public GiveGiftVO giveGift(Long userId, String itemName) {
    Fudi fudi =
        fudiHelper.findAndTouchFudi(userId).orElseThrow(() -> new IllegalStateException("未找到福地"));

    StackableItem gift = resolveGiftItem(userId, itemName);
    ItemTemplate template =
        itemTemplateRepository
            .findById(gift.getTemplateId() != null ? gift.getTemplateId() : (long) 1)
            .orElse(null);

    Spirit spirit =
        spiritRepository
            .findByFudiId(fudi.getId())
            .orElseThrow(() -> new IllegalStateException("地灵不存在"));

    if (spirit.getLastGiftTime() != null
        && spirit.getLastGiftTime().toLocalDate().equals(LocalDateTime.now().toLocalDate())) {
      throw new IllegalStateException("今日已送过礼物，明天再来吧");
    }

    SpiritForm spiritForm = resolveSpiritForm(spirit);
    Set<String> likedTags = getLikedTags(spiritForm);
    Set<String> dislikedTags = getDislikedTags(spiritForm);
    Set<String> itemTags = resolveItemTags(gift, template);

    GiftReaction reaction = calculateAffectionChange(itemTags, likedTags, dislikedTags);

    spirit.addAffection(reaction.change);
    spirit.setLastGiftTime(LocalDateTime.now());

    consumeGiftItem(gift);

    spiritRepository.save(spirit);
    fudiRepository.save(fudi);

    return new GiveGiftVO(
        gift.getName(),
        spirit.getAffection() - reaction.change,
        spirit.getAffection(),
        reaction.change,
        reaction.reaction,
        reaction.isLiked,
        reaction.isDisliked);
  }

  private StackableItem resolveGiftItem(Long userId, String itemName) {
    List<StackableItem> items =
        stackableItemRepository.findByUserId(userId).stream()
            .filter(e -> e.getName().contains(itemName))
            .toList();
    if (items.isEmpty()) {
      throw new IllegalStateException("背包中未找到 [" + itemName + "]");
    }
    if (items.size() > 1) {
      throw new IllegalStateException("找到多个 [" + itemName + "]，请使用更精确的名称");
    }
    return items.getFirst();
  }

  private SpiritForm resolveSpiritForm(Spirit spirit) {
    if (spirit.getFormId() != null) {
      return spiritFormRepository.findById(spirit.getFormId().longValue()).orElse(null);
    }
    return null;
  }

  private Set<String> getLikedTags(SpiritForm spiritForm) {
    return spiritForm != null && spiritForm.getLikedTags() != null
        ? spiritForm.getLikedTags()
        : Set.of();
  }

  private Set<String> getDislikedTags(SpiritForm spiritForm) {
    return spiritForm != null && spiritForm.getDislikedTags() != null
        ? spiritForm.getDislikedTags()
        : Set.of();
  }

  private Set<String> resolveItemTags(StackableItem gift, ItemTemplate template) {
    if (gift.getTags() != null && !gift.getTags().isEmpty()) return gift.getTags();
    if (template != null && template.getTags() != null) return template.getTags();
    return Set.of();
  }

  private GiftReaction calculateAffectionChange(
      Set<String> itemTags, Set<String> likedTags, Set<String> dislikedTags) {
    boolean isLiked = itemTags.stream().anyMatch(likedTags::contains);
    boolean isDisliked = itemTags.stream().anyMatch(dislikedTags::contains);

    int change;
    String reaction;
    if (isLiked) {
      change = 10 + ThreadLocalRandom.current().nextInt(41);
      reaction = "开心";
    } else if (isDisliked) {
      change = -(5 + ThreadLocalRandom.current().nextInt(16));
      reaction = "嫌弃";
    } else {
      change = 1 + ThreadLocalRandom.current().nextInt(3);
      reaction = "平淡";
    }
    return new GiftReaction(change, reaction, isLiked, isDisliked);
  }

  private void consumeGiftItem(StackableItem gift) {
    if (gift.getQuantity() != null && gift.getQuantity() > 1) {
      gift.setQuantity(gift.getQuantity() - 1);
      stackableItemRepository.save(gift);
    } else {
      stackableItemRepository.deleteById(gift.getId());
    }
  }

  private record GiftReaction(int change, String reaction, boolean isLiked, boolean isDisliked) {}
}
