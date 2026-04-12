package top.stillmisty.xiantao.infrastructure.mapper;

import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import top.stillmisty.xiantao.domain.item.entity.ItemTemplate;

import java.util.List;

@Mapper
public interface ItemTemplateMapper extends BaseMapper<ItemTemplate> {

    /**
     * 根据单个标签查找物品模板（使用PostgreSQL JSONB操作符 @>）
     * @param tag 标签名称
     * @return 物品模板列表
     */
    @Select("SELECT * FROM xt_item_template WHERE tags @> CAST(ARRAY[#{tag}] AS jsonb)")
    List<ItemTemplate> selectByTag(@Param("tag") String tag);

    /**
     * 根据多个标签查找物品模板（包含任一标签，使用 ?| 操作符）
     * @param tags 标签列表
     * @return 物品模板列表
     */
    List<ItemTemplate> selectByAnyTags(@Param("tags") List<String> tags);

    /**
     * 根据多个标签查找物品模板（包含所有标签，使用 ?& 操作符）
     * @param tags 标签列表
     * @return 物品模板列表
     */
    List<ItemTemplate> selectByAllTags(@Param("tags") List<String> tags);
}
