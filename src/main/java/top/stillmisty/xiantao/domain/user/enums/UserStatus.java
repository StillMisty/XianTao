package top.stillmisty.xiantao.domain.user.enums;

import com.mybatisflex.annotation.EnumValue;
import lombok.Getter;

/**
 * 用户状态枚举
 */
@Getter
public enum UserStatus {

    /**
     * 空闲
     */
    IDLE("idle", "空闲"),

    /**
     * 历练
     */
    EXERCISING("exercising", "历练"),

    /**
     * 打坐
     */
    MEDITATING("meditating", "打坐"),

    /**
     * 赶路
     */
    RUNNING("running", "赶路");


    @EnumValue
    private final String code;
    private final String name;

    UserStatus(String code, String name) {
        this.code = code;
        this.name = name;
    }
}
