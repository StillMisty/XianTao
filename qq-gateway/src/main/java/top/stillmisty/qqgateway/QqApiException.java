package top.stillmisty.qqgateway;

import org.jspecify.annotations.Nullable;

/** QQ OpenAPI 调用异常。 */
public class QqApiException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  /** HTTP 状态码，0 表示未收到响应（网络错误）。 */
  private final int httpStatus;

  /** 平台错误码（响应体 {@code code} 字段），可能为 null。 */
  private final @Nullable String code;

  /** 平台建议的重试等待毫秒数（Retry-After 头），0 表示未提供。 */
  private final long retryAfterMillis;

  public QqApiException(int httpStatus, @Nullable String code, String message) {
    this(httpStatus, code, message, null, 0);
  }

  public QqApiException(
      int httpStatus, @Nullable String code, String message, @Nullable Throwable cause) {
    this(httpStatus, code, message, cause, 0);
  }

  public QqApiException(
      int httpStatus,
      @Nullable String code,
      String message,
      @Nullable Throwable cause,
      long retryAfterMillis) {
    super(message, cause);
    this.httpStatus = httpStatus;
    this.code = code;
    this.retryAfterMillis = retryAfterMillis;
  }

  public int httpStatus() {
    return httpStatus;
  }

  public @Nullable String code() {
    return code;
  }

  public long retryAfterMillis() {
    return retryAfterMillis;
  }

  /** 是否为频率限制（HTTP 429 或平台限流错误码）。 */
  public boolean isRateLimited() {
    return httpStatus == 429 || "11244".equals(code);
  }

  /** Access Token 失效，需要刷新后重试。 */
  public boolean isTokenInvalid() {
    return httpStatus == 401 || "11242".equals(code);
  }

  /** 平台 5xx，通常为网关抖动，可退避重试。 */
  public boolean isServerError() {
    return httpStatus >= 500;
  }
}
