package top.stillmisty.xiantao.handle.onebotv11;

import lombok.RequiredArgsConstructor;
import love.forte.simbot.component.onebot.v11.core.event.message.OneBotMessageEvent;
import love.forte.simbot.quantcat.common.annotations.ContentTrim;
import love.forte.simbot.quantcat.common.annotations.Filter;
import love.forte.simbot.quantcat.common.annotations.FilterValue;
import love.forte.simbot.quantcat.common.annotations.Listener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.domain.user.entity.UserAuth;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.service.UserAuthService;
import top.stillmisty.xiantao.service.UserService;

@Component
@RequiredArgsConstructor
public class UserAuthHandle {
    
    private static final Logger log = LoggerFactory.getLogger(UserAuthHandle.class);
    
    private final UserAuthService userAuthService;
    private final UserService userService;
    
    @Listener
    @ContentTrim
    @Filter("我要修仙 {{nickname}}")
    public void register(OneBotMessageEvent event, @FilterValue("nickname") String nickname) {
        var userAuth = userAuthService.findUserIdByOpenId(PlatformType.ONE_BOT_V11, event.getAuthorId());
        if (userAuth.isPresent()) {
            event.replyBlocking("您已入仙途了哦~");
            return;
        }

        if (nickname.isEmpty()) {
            event.replyBlocking("道号不能为空哦~");
            return;
        }
        
        nickname = nickname.strip();
        
        if (nickname.length() < 4 || nickname.length() > 8) {
            event.replyBlocking("道号长度需在4到8个字符之间~");
            return;
        }

        var newUser = userService.createUser(PlatformType.ONE_BOT_V11, event.getAuthorId(), nickname);

        log.info("创建用户成功 - ID: {}, Nickname: {}", newUser.userId(), newUser.nickname());

        if (!newUser.success()){
            event.replyBlocking("系统错误：用户创建失败，请联系管理员");
            log.error("用户创建失败 - UserId: {}, Nickname: {}", newUser.userId(), newUser.nickname());
            return;
        }


        log.info("准备创建授权记录 - UserId: {}, Platform: {}, OpenId: {}", newUser.userId(), PlatformType.ONE_BOT_V11, event.getAuthorId());
        UserAuth newAuth = UserAuth.init(PlatformType.ONE_BOT_V11, event.getAuthorId().toString(), newUser.userId());
        log.info("UserAuth对象 - ID: {}, UserId: {}, Platform: {}", newAuth.getId(), newAuth.getUserId(), newAuth.getPlatform());
        
        userAuthService.save(newAuth);
        
        event.replyBlocking("欢迎踏入仙途！您的道号为：" + newUser.nickname() + "\n输入 #状态 查看您的角色信息");
    }
}
