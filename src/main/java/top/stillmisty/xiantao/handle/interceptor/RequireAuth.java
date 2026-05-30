package top.stillmisty.xiantao.handle.interceptor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import love.forte.simbot.quantcat.common.annotations.Interceptor;

/** 要求认证的注解 标记在监听方法上，表示该方法需要认证才能执行 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Interceptor(value = AuthInterceptorFactory.class, priority = 100)
public @interface RequireAuth {}
