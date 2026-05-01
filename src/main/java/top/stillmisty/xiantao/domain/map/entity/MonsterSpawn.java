package top.stillmisty.xiantao.domain.map.entity;

/**
 * 怪物刷怪配置
 *
 * @param weight 选中权重
 * @param min    最小数量
 * @param max    最大数量
 */
public record MonsterSpawn(int weight, int min, int max) {
}
