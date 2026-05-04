package top.stillmisty.xiantao.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("UserContext 测试")
class UserContextTest {

    @AfterEach
    void tearDown() {
        // ScopedValue 绑定的生命周期在虚拟线程中有效
    }

    @Test
    @DisplayName("未绑定时 getCurrentUserId 返回 null")
    void getCurrentUserId_whenNotBound_shouldReturnNull() {
        assertNull(UserContext.getCurrentUserId());
    }

    @Test
    @DisplayName("ScopedValue 绑定后在作用域内可获取值")
    void getCurrentUserId_whenBound_shouldReturnValue() throws Exception {
        Long result = ScopedValue.where(UserContext.CURRENT_USER, 42L)
                .call(() -> UserContext.getCurrentUserId());
        assertEquals(42L, result);
    }

    @Test
    @DisplayName("ScopedValue 作用域外恢复为未绑定")
    void getCurrentUserId_afterScope_shouldNotBeBound() throws Exception {
        Long result = ScopedValue.where(UserContext.CURRENT_USER, 99L)
                .call(() -> UserContext.getCurrentUserId());
        assertEquals(99L, result);

        assertNull(UserContext.getCurrentUserId());
    }

    @Test
    @DisplayName("ScopedValue.newInstance 创建的实例初始未绑定")
    void newInstance_shouldNotBeInitiallyBound() {
        ScopedValue<Long> sv = ScopedValue.newInstance();
        assertFalse(sv.isBound());
    }
}
