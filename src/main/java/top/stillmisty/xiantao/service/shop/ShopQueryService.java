package top.stillmisty.xiantao.service.shop;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.item.entity.Equipment;
import top.stillmisty.xiantao.domain.item.entity.EquipmentTemplate;
import top.stillmisty.xiantao.domain.item.entity.ItemTemplate;
import top.stillmisty.xiantao.domain.item.entity.StackableItem;
import top.stillmisty.xiantao.domain.shop.entity.ShopNpc;
import top.stillmisty.xiantao.domain.shop.entity.ShopProduct;
import top.stillmisty.xiantao.domain.shop.enums.ProductType;
import top.stillmisty.xiantao.domain.shop.vo.EquipmentListVO;
import top.stillmisty.xiantao.domain.shop.vo.PlayerItemsVO;
import top.stillmisty.xiantao.domain.shop.vo.ProductListVO;
import top.stillmisty.xiantao.infrastructure.repository.EquipmentRepository;
import top.stillmisty.xiantao.infrastructure.repository.EquipmentTemplateRepository;
import top.stillmisty.xiantao.infrastructure.repository.ItemTemplateRepository;
import top.stillmisty.xiantao.infrastructure.repository.ShopNpcRepository;
import top.stillmisty.xiantao.infrastructure.repository.ShopProductRepository;
import top.stillmisty.xiantao.infrastructure.repository.StackableItemRepository;
import top.stillmisty.xiantao.service.BusinessException;
import top.stillmisty.xiantao.service.ErrorCode;
import top.stillmisty.xiantao.service.ServiceResult;
import top.stillmisty.xiantao.service.ai.ShopChatContext;
import top.stillmisty.xiantao.service.player.UserStateService;

/**
 * 商店查询服务
 *
 * <p>负责商店商品列表、玩家物品查询等只读操作，从ShopService中提取以降低耦合。
 */
@Service
@RequiredArgsConstructor
public class ShopQueryService {

  private final ShopNpcRepository shopNpcRepository;
  private final ShopProductRepository shopProductRepository;
  private final ItemTemplateRepository itemTemplateRepository;
  private final EquipmentTemplateRepository equipmentTemplateRepository;
  private final StackableItemRepository stackableItemRepository;
  private final EquipmentRepository equipmentRepository;
  private final UserStateService userStateService;
  private final PriceEngine priceEngine;

  /**
   * 获取商品列表（公开API）
   *
   * @param userId 用户ID
   * @return 商品列表结果
   */
  @Transactional(readOnly = true)
  public ServiceResult<ProductListVO> listProducts(Long userId) {
    var user = userStateService.loadUserReadOnly(userId);
    ShopNpc npc = findByLocation(user.getLocationId());
    return new ServiceResult.Success<>(listProducts(npc));
  }

  /**
   * 获取商品列表（内部API，供缓存调用）
   *
   * @param userId 用户ID
   * @return 商品列表
   */
  @Cacheable(cacheNames = "shop_products", key = "#userId")
  public ProductListVO listProductsInternal(Long userId) {
    ShopChatContext chatCtx = ShopChatContext.current();
    if (chatCtx != null) {
      return listProducts(chatCtx.npc());
    }
    var user = userStateService.loadUserReadOnly(userId);
    ShopNpc npc = findByLocation(user.getLocationId());
    return listProducts(npc);
  }

  /**
   * 查询玩家装备列表
   *
   * @param userId 用户ID
   * @return 装备列表
   */
  @Transactional(readOnly = true)
  @Cacheable(cacheNames = "shop_player_items", key = "'equipment:' + #userId")
  public EquipmentListVO queryPlayerEquipment(Long userId) {
    List<Equipment> unequipped = equipmentRepository.findUnequippedByUserId(userId);
    List<EquipmentListVO.EquipmentEntry> entries =
        unequipped.stream()
            .map(
                e ->
                    new EquipmentListVO.EquipmentEntry(
                        e.getId(),
                        e.getName(),
                        e.getRarity().getName(),
                        e.getForgeLevel(),
                        e.getAffixes() != null
                            ? e.getAffixes().keySet().stream()
                                .limit(3)
                                .collect(Collectors.joining("、"))
                            : "无"))
            .toList();
    return new EquipmentListVO(entries);
  }

  /**
   * 查询玩家物品列表
   *
   * @param userId 用户ID
   * @return 物品列表
   */
  @Transactional(readOnly = true)
  @Cacheable(cacheNames = "shop_player_items", key = "'items:' + #userId")
  public PlayerItemsVO queryPlayerItems(Long userId) {
    List<Equipment> unequipped = equipmentRepository.findUnequippedByUserId(userId);
    List<StackableItem> items = stackableItemRepository.findByUserId(userId);

    List<PlayerItemsVO.EquipmentInfo> equipInfos =
        unequipped.stream()
            .map(
                e ->
                    new PlayerItemsVO.EquipmentInfo(
                        e.getId(),
                        e.getName(),
                        e.getRarity().getName(),
                        e.getForgeLevel(),
                        e.getAffixes() != null
                            ? e.getAffixes().keySet().stream()
                                .limit(3)
                                .collect(Collectors.joining("、"))
                            : "无"))
            .toList();

    List<PlayerItemsVO.StackableInfo> itemInfos =
        items.stream()
            .map(
                s ->
                    new PlayerItemsVO.StackableInfo(
                        s.getId(),
                        s.getName(),
                        s.getQuantity(),
                        s.getItemType().getName(),
                        s.getTradable()))
            .toList();

    return new PlayerItemsVO(equipInfos, itemInfos);
  }

  /**
   * 根据位置查找商店NPC
   *
   * @param locationId 位置ID
   * @return 商店NPC
   */
  @Cacheable(cacheNames = "shop_locations", key = "#locationId")
  public ShopNpc findByLocation(Long locationId) {
    return shopNpcRepository
        .findByMapNodeId(locationId)
        .orElseThrow(() -> new BusinessException(ErrorCode.SHOP_NOT_FOUND));
  }

  /**
   * 检查位置是否有商店
   *
   * @param locationId 位置ID
   * @return 是否有商店
   */
  public boolean hasShopAtLocation(Long locationId) {
    return shopNpcRepository.findByMapNodeId(locationId).isPresent();
  }

  /**
   * 根据名称查找可堆叠物品
   *
   * @param userId 用户ID
   * @param name 物品名称
   * @return 物品列表
   */
  public List<StackableItem> findStackableItemsByName(Long userId, String name) {
    return stackableItemRepository.findByUserIdAndNameContaining(userId, name);
  }

  /**
   * 根据名称查找装备
   *
   * @param userId 用户ID
   * @param name 装备名称
   * @return 装备列表
   */
  public List<Equipment> findEquipmentByName(Long userId, String name) {
    return equipmentRepository.findUnequippedByUserId(userId).stream()
        .filter(e -> e.getName().contains(name))
        .toList();
  }

  /**
   * 获取商品列表（内部方法）
   *
   * @param npc 商店NPC
   * @return 商品列表
   */
  public ProductListVO listProducts(ShopNpc npc) {
    List<ShopProduct> products = shopProductRepository.findByShopNpcId(npc.getId());
    List<ProductListVO.ProductEntry> entries = new java.util.ArrayList<>();

    List<Long> itemTemplateIds = new java.util.ArrayList<>();
    List<Long> equipTemplateIds = new java.util.ArrayList<>();
    for (ShopProduct product : products) {
      priceEngine.applyLazyRestock(product);
      if (product.getProductType() == ProductType.ITEM) {
        itemTemplateIds.add(product.getTemplateId());
      } else {
        equipTemplateIds.add(product.getTemplateId());
      }
    }
    Map<Long, ItemTemplate> itemMap =
        itemTemplateIds.isEmpty()
            ? Map.of()
            : itemTemplateRepository.findByIds(itemTemplateIds).stream()
                .collect(Collectors.toMap(ItemTemplate::getId, t -> t));
    Map<Long, EquipmentTemplate> equipMap =
        equipTemplateIds.isEmpty()
            ? Map.of()
            : equipmentTemplateRepository.findByIds(equipTemplateIds).stream()
                .collect(Collectors.toMap(EquipmentTemplate::getId, t -> t));

    for (ShopProduct product : products) {
      String name;
      String extra = "";
      if (product.getProductType() == ProductType.ITEM) {
        var t = itemMap.get(product.getTemplateId());
        name = t != null ? t.getName() : "未知物品";
      } else {
        var t = equipMap.get(product.getTemplateId());
        name = t != null ? t.getName() : "未知装备";
        extra = t != null && t.getSlot() != null ? t.getSlot().getName() : "";
      }
      entries.add(
          new ProductListVO.ProductEntry(
              product.getId(),
              product.getProductType().getCode(),
              name,
              product.getCurrentPrice(),
              product.getCurrentStock(),
              extra));
    }
    return new ProductListVO(npc.getName(), entries);
  }
}
