package top.stillmisty.xiantao.domain.user.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import java.time.LocalDateTime;
import lombok.Data;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;

/** 跨平台授权绑定表实体 */
@Data
@Table("xt_user_auth")
public class UserAuth {

  /** 绑定记录主键 */
  @Id(keyType = KeyType.Auto)
  private Long id;

  /** 关联的游戏角色 ID */
  private Long userId;

  /** 平台类型 */
  private PlatformType platform;

  /** 平台方的唯一标识 ID */
  private String platformOpenId;

  /** 绑定发生的时间 */
  @Column(onInsertValue = "now()")
  private LocalDateTime createTime;

  public boolean matches(PlatformType platform, String openId) {
    return this.platform == platform && this.platformOpenId.equals(openId);
  }

  /** 创建新绑定 */
  public static UserAuth init(PlatformType platform, String openId, Long userId) {
    UserAuth userAuth = new UserAuth();
    userAuth.platform = platform;
    userAuth.platformOpenId = openId;
    userAuth.userId = userId;
    return userAuth;
  }
}
