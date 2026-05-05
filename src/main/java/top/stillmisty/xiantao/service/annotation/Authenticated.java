package top.stillmisty.xiantao.service.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记需要自动认证的 Service 方法 约定：被标注方法的前两个参数必须为 (PlatformType, String openId) 可选的第三个参数为
 * UserStatus，表示要求用户处于该状态
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Authenticated {}
