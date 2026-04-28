package top.stillmisty.xiantao.service.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记需要消耗地灵精力的方法
 * 约定：被标注方法的第一个 Long 类型参数为 userId
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ConsumeSpiritEnergy {

    /**
     * 基础精力消耗值
     */
    int value();
}
