package top.stillmisty.xiantao.domain.user.entity;


import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.keygen.KeyGenerators;
import lombok.Data;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 跨平台授权绑定表实体
 */
@Data
@Table("xt_user_auth")
public class UserAuth {

    /**
     * 绑定记录主键
     */
    @Id(keyType = KeyType.Generator, value = KeyGenerators.uuid)
    private UUID id;

    /**
     * 关联的游戏角色 ID
     */
    private UUID userId;

    /**
     * 平台类型
     */
    private PlatformType platform;

    /**
     * 平台方的唯一标识 ID
     */
    private String platformOpenId;

    /**
     * 绑定发生的时间
     */
    private LocalDateTime bindTime;

    /**
     * 创建新绑定
     */
    public static UserAuth init(PlatformType platform, String openId, UUID userId) {
        UserAuth userAuth = new UserAuth();
        userAuth.bindTime = LocalDateTime.now();
        userAuth.platform = platform;
        userAuth.platformOpenId = openId;
        userAuth.userId = userId;
        return userAuth;
    }
}
