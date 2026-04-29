package top.stillmisty.xiantao.service;

/**
 * 用户上下文持有者
 * 用于在 Function Calling 中传递当前用户 ID
 */
public class UserContext {

    public static final ScopedValue<Long> CURRENT_USER = ScopedValue.newInstance();

    /**
     * 获取当前用户 ID，未绑定时返回 null
     */
    public static Long getCurrentUserId() {
        return CURRENT_USER.isBound() ? CURRENT_USER.get() : null;
    }
}
