package top.stillmisty.xiantao.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ServiceResult 密封接口测试")
class ServiceResultTest {

    @Test
    @DisplayName("Success 创建并携带数据")
    void success_shouldCarryData() {
        var result = new ServiceResult.Success<>("hello");

        assertTrue(result instanceof ServiceResult.Success<String>);
        assertInstanceOf(ServiceResult.Success.class, result);
        assertEquals("hello", result.data());
    }

    @Test
    @DisplayName("Failure 创建并携带错误码和消息")
    void failure_shouldCarryErrorInfo() {
        var result = new ServiceResult.Failure<>("ERR_CODE", "something went wrong");

        assertInstanceOf(ServiceResult.Failure.class, result);
        assertEquals("ERR_CODE", result.errorCode());
        assertEquals("something went wrong", result.errorMessage());
    }

    @Test
    @DisplayName("authFailure 工厂方法创建 AUTH_FAILED 错误码")
    void authFailure_shouldUseAuthFailedCode() {
        var result = ServiceResult.authFailure("用户未认证");

        assertTrue(result instanceof ServiceResult.Failure);
        assertEquals("AUTH_FAILED", ((ServiceResult.Failure<?>) result).errorCode());
        assertEquals("用户未认证", ((ServiceResult.Failure<?>) result).errorMessage());
    }

    @Test
    @DisplayName("businessFailure 工厂方法创建 BUSINESS_ERROR 错误码")
    void businessFailure_shouldUseBusinessErrorCode() {
        var result = ServiceResult.businessFailure("操作失败");

        assertTrue(result instanceof ServiceResult.Failure);
        assertEquals("BUSINESS_ERROR", ((ServiceResult.Failure<?>) result).errorCode());
        assertEquals("操作失败", ((ServiceResult.Failure<?>) result).errorMessage());
    }

    @Test
    @DisplayName("Success 通过密封接口多态引用")
    void success_shouldBeAssignableToServiceResult() {
        ServiceResult<Integer> result = new ServiceResult.Success<>(42);

        assertInstanceOf(ServiceResult.Success.class, result);
    }

    @Test
    @DisplayName("sealed 接口只允许 Success 和 Failure")
    void sealedInterface_shouldOnlyPermitSuccessAndFailure() {
        Class<?>[] permitted = ServiceResult.class.getPermittedSubclasses();

        assertEquals(2, permitted.length);
    }
}
