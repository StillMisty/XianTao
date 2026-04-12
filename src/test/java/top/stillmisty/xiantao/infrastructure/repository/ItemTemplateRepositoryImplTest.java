package top.stillmisty.xiantao.infrastructure.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import top.stillmisty.xiantao.domain.item.entity.ItemTemplate;
import top.stillmisty.xiantao.domain.item.repository.ItemTemplateRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ItemTemplateRepository JSONB标签搜索测试
 */
@SpringBootTest
class ItemTemplateRepositoryImplTest {

    @Autowired
    private ItemTemplateRepository repository;

    @Test
    void testFindByTag() {
        // 测试单个标签搜索
        List<ItemTemplate> results = repository.findByTag("ore");
        
        assertNotNull(results);
        System.out.println("找到包含 'ore' 标签的物品数量: " + results.size());
        results.forEach(item -> 
            System.out.println("  - " + item.getName() + ": " + item.getTags())
        );
        
        // 验证所有结果都包含 'ore' 标签
        results.forEach(item -> 
            assertTrue(item.hasTag("ore"), 
                "物品 '" + item.getName() + "' 应该包含 'ore' 标签")
        );
    }

    @Test
    void testFindByTags_AnyMatch() {
        // 测试多个标签的OR匹配（包含任一标签）
        List<ItemTemplate> results = repository.findByTags(List.of("ore", "herb"));
        
        assertNotNull(results);
        System.out.println("\n找到包含 'ore' 或 'herb' 标签的物品数量: " + results.size());
        results.forEach(item -> 
            System.out.println("  - " + item.getName() + ": " + item.getTags())
        );
        
        // 验证所有结果至少包含一个标签
        results.forEach(item -> 
            assertTrue(item.hasTag("ore") || item.hasTag("herb"),
                "物品 '" + item.getName() + "' 应该包含 'ore' 或 'herb' 标签")
        );
    }

    @Test
    void testFindByAllTags_AndMatch() {
        // 测试多个标签的AND匹配（包含所有标签）
        List<ItemTemplate> results = repository.findByAllTags(List.of("herb", "rare"));
        
        assertNotNull(results);
        System.out.println("\n找到同时包含 'herb' 和 'rare' 标签的物品数量: " + results.size());
        results.forEach(item -> 
            System.out.println("  - " + item.getName() + ": " + item.getTags())
        );
        
        // 验证所有结果都包含两个标签
        results.forEach(item -> {
            assertTrue(item.hasTag("herb"), 
                "物品 '" + item.getName() + "' 应该包含 'herb' 标签");
            assertTrue(item.hasTag("rare"), 
                "物品 '" + item.getName() + "' 应该包含 'rare' 标签");
        });
    }

    @Test
    void testFindByTag_EmptyResult() {
        // 测试不存在的标签
        List<ItemTemplate> results = repository.findByTag("nonexistent_tag");
        
        assertNotNull(results);
        assertTrue(results.isEmpty(), "不存在的标签应该返回空列表");
        System.out.println("\n不存在的标签搜索结果: " + results.size());
    }

    @Test
    void testFindByTag_NullAndEmpty() {
        // 测试null和空字符串
        List<ItemTemplate> result1 = repository.findByTag(null);
        List<ItemTemplate> result2 = repository.findByTag("");
        List<ItemTemplate> result3 = repository.findByTag("   ");
        
        assertTrue(result1.isEmpty(), "null标签应该返回空列表");
        assertTrue(result2.isEmpty(), "空字符串标签应该返回空列表");
        assertTrue(result3.isEmpty(), "空白字符串标签应该返回空列表");
    }

    @Test
    void testFindByTags_EmptyList() {
        // 测试空标签列表
        List<ItemTemplate> result = repository.findByTags(List.of());
        assertTrue(result.isEmpty(), "空标签列表应该返回空列表");
    }

    @Test
    void testFindByAllTags_CaseInsensitive() {
        // 测试大小写不敏感
        List<ItemTemplate> results1 = repository.findByTag("ORE");
        List<ItemTemplate> results2 = repository.findByTag("ore");
        
        assertEquals(results1.size(), results2.size(), 
            "标签搜索应该是大小写不敏感的");
    }
}
