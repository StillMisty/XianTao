package top.stillmisty.xiantao.handle.onebotv11;

import lombok.RequiredArgsConstructor;
import love.forte.simbot.event.MessageEvent;
import love.forte.simbot.quantcat.common.annotations.ContentTrim;
import love.forte.simbot.quantcat.common.annotations.Filter;
import love.forte.simbot.quantcat.common.annotations.Listener;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.user.entity.UserAuth;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.service.ItemService;
import top.stillmisty.xiantao.service.UserAuthService;

@Component
@RequiredArgsConstructor
public class StatusHandle {
    private final UserAuthService userAuthService;
    private final ItemService itemService;

    @Listener
    @ContentTrim
    @Filter("状态")
    public void status(MessageEvent event) {
        var userAuth = userAuthService.findUserIdByOpenId(PlatformType.ONE_BOT_V11, event.getAuthorId());

        if (userAuth.isEmpty()) {
            event.replyBlocking("输入“我要修仙 [道号]”进入仙途吧！");
            return;
        }

        var characterStatus = itemService.getCharacterStatus(userAuth.get().getUserId());
        event.replyBlocking(characterStatus.toString());
    }
}
