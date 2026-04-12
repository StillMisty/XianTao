package top.stillmisty.xiantao.service;

/**
 * 用户上下文持有者（ThreadLocal）
 * 用于在 Function Calling 中传递当前用户 ID
 */
public class UserContext {
    
    private static final ThreadLocal<Long> currentUser = new ThreadLocal<>();
    
    /**
     * 设置当前用户 ID
     */
    public static void setCurrentUserId(Long userId) {
        currentUser.set(userId);
    }
    
    /**
     * 获取当前用户 ID
     */
    public static Long getCurrentUserId() {
        return currentUser.get();
    }
    
    /**
     * 清除当前用户 ID（防止内存泄漏）
     */
    public static void clear() {
        currentUser.remove();
    }
}
