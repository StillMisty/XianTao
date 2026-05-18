package top.stillmisty.xiantao.service;

import lombok.Getter;

/** 业务异常，携带 ErrorCode 和格式化参数 */
@Getter
public class BusinessException extends RuntimeException {

  private final ErrorCode errorCode;

  public BusinessException(ErrorCode errorCode, Object... args) {
    super(errorCode.format(args));
    this.errorCode = errorCode;
  }
}
