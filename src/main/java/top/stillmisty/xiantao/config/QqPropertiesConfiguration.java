package top.stillmisty.xiantao.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/** QQ 配置属性注册。独立于条件装配：凭证缺失时也要能读取配置并给出启动提示。 */
@Configuration
@EnableConfigurationProperties(QqProperties.class)
public class QqPropertiesConfiguration {}
