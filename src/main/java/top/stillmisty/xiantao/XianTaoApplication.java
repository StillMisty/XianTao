package top.stillmisty.xiantao;

import love.forte.simbot.spring.EnableSimbot;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableSimbot
@EnableAspectJAutoProxy
public class XianTaoApplication {

  static void main(String[] args) {
    SpringApplication.run(XianTaoApplication.class, args);
  }
}
