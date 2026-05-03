package top.stillmisty.xiantao.service;

/**
 * 服务层统一返回类型
 * <ul>
 *   <li>{@link Success} — 认证通过 + 业务执行，承载领域 VO</li>
 *   <li>{@link Failure} — 认证失败或业务校验不通过，承载错误码和用户可读错误消息</li>
 * </ul>
 *
 * @param <T> 成功时承载的数据类型
 */
public sealed interface ServiceResult<T> permits ServiceResult.Success, ServiceResult.Failure {

    record Success<T>(T data) implements ServiceResult<T> {
    }

    record Failure<T>(String errorCode, String errorMessage) implements ServiceResult<T> {
    }

    static <T> ServiceResult<T> authFailure(String message) {
        return new Failure<>("AUTH_FAILED", message);
    }

    static <T> ServiceResult<T> businessFailure(String message) {
        return new Failure<>("BUSINESS_ERROR", message);
    }
}
