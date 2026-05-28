package top.stillmisty.xiantao.infrastructure.mapper;

import com.mybatisflex.core.BaseMapper;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import top.stillmisty.xiantao.domain.item.entity.StackableItem;

@Mapper
public interface StackableItemMapper extends BaseMapper<StackableItem> {

  @Select("SELECT * FROM xt_inventory_item WHERE user_id = #{userId} AND name = #{name}")
  List<StackableItem> selectByUserIdAndName(
      @Param("userId") Long userId, @Param("name") String name);

  @Update(
      "UPDATE xt_inventory_item SET quantity = quantity - #{qty} WHERE id = #{id} AND user_id = #{userId} AND quantity >= #{qty}")
  int reduceQuantityById(@Param("id") Long id, @Param("userId") Long userId, @Param("qty") int qty);

  @Update(
      "INSERT INTO xt_inventory_item (user_id, template_id, item_type, name, quantity, tags, properties, properties_hash, tradable, create_time, update_time) "
          + "VALUES (#{item.userId}, #{item.templateId}, #{item.itemType}, #{item.name}, #{item.quantity}, COALESCE(#{item.tags}::jsonb, '[]'::jsonb), "
          + "COALESCE(#{item.properties}::jsonb, '{}'::jsonb), #{item.propertiesHash}, "
          + "#{item.tradable}, NOW(), NOW()) "
          + "ON CONFLICT (user_id, template_id, properties_hash) DO UPDATE SET quantity = xt_inventory_item.quantity + #{item.quantity}, update_time = NOW()")
  int upsertIncrementQuantity(@Param("item") StackableItem item);

  @Update("DELETE FROM xt_inventory_item WHERE id = #{id} AND quantity <= 0")
  int deleteIfZeroQuantity(@Param("id") Long id);

  @Select(
      "SELECT * FROM xt_inventory_item WHERE user_id = #{userId} AND tags @> #{tagsJson}::jsonb")
  List<StackableItem> selectByUserIdAndAllTags(
      @Param("userId") Long userId, @Param("tagsJson") String tagsJson);

  @Select(
      "SELECT * FROM xt_inventory_item WHERE user_id = #{userId} AND tags ?| #{tagsArray}::text[]")
  List<StackableItem> selectByUserIdAndAnyTag(
      @Param("userId") Long userId, @Param("tagsArray") String[] tagsArray);
}
