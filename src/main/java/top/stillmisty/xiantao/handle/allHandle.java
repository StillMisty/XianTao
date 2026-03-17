package top.stillmisty.xiantao.handle;

import love.forte.simbot.event.ChatGroupEvent;
import love.forte.simbot.quantcat.common.annotations.Listener;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

import static java.lang.IO.println;

@Component
public class allHandle {

    @Listener
    public CompletableFuture<?> all(ChatGroupEvent event){
        println("all" + event.toString());
        var group = event.getContent();
        return group.sendAsync("all" + group.getId());
    }
}
