package top.stillmisty.xiantao.handle.interceptor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import love.forte.simbot.quantcat.common.annotations.Interceptor;

/** 要求GM权限的注解 标记在监听方法上，表示该方法需要GM权限才能执行 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Interceptor(GmInterceptorFactory.class)
public @interface RequireGm {}
