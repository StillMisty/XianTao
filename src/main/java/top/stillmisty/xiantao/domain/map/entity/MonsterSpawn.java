package top.stillmisty.xiantao.domain.map.entity;

/**
 * 怪物生成配置
 * 用于定义地图节点的遇怪池
 */
public record MonsterSpawn(
        /*
          权重
         */
        int weight,

        /*
          最小数量
         */
        int min,

        /*
          最大数量
         */
        int max
) {
    /**
     * 创建怪物生成配置
     */
    public static MonsterSpawn of(int weight, int min, int max) {
        return new MonsterSpawn(weight, min, max);
    }

    /**
     * 创建默认配置（1只怪物）
     */
    public static MonsterSpawn of(int weight) {
        return new MonsterSpawn(weight, 1, 1);
    }
}
