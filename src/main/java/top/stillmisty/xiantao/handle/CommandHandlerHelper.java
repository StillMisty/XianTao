package top.stillmisty.xiantao.handle;

import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import top.stillmisty.xiantao.service.BusinessException;
import top.stillmisty.xiantao.service.ServiceResult;

/** 命令处理器公共方法：统一 ServiceResult 模式匹配 + BusinessException 异常转错误文本 */
public final class CommandHandlerHelper {

  private CommandHandlerHelper() {}

  private static final Pattern BRACKET_PATTERN = Pattern.compile("【([^】]+)】");

  /** 将文本中的 【title】 替换为当前格式的加粗样式 */
  public static String applyBoldFormatting(String text, TextFormat fmt) {
    Matcher matcher = BRACKET_PATTERN.matcher(text);
    if (!matcher.find()) {
      return text;
    }
    matcher.reset();
    StringBuilder sb = new StringBuilder();
    while (matcher.find()) {
      matcher.appendReplacement(sb, Matcher.quoteReplacement(fmt.bold(matcher.group(1))));
    }
    matcher.appendTail(sb);
    return sb.toString();
  }

  /** 调用服务，默认失败格式：fmt.error(msg)，成功由 onSuccess 格式化 */
  public static <T> String safeCall(
      Supplier<ServiceResult<T>> serviceCall,
      TextFormat fmt,
      Function<? super T, String> onSuccess) {
    return safeCall(serviceCall, fmt, onSuccess, fmt::error);
  }

  /** 调用服务，自定义失败格式，成功由 onSuccess 格式化 */
  public static <T> String safeCall(
      Supplier<ServiceResult<T>> serviceCall,
      TextFormat fmt,
      Function<? super T, String> onSuccess,
      Function<String, String> onFailure) {
    try {
      return switch (serviceCall.get()) {
        case ServiceResult.Failure(var _, var msg) -> onFailure.apply(msg);
        case ServiceResult.Success(var data) -> onSuccess.apply(data);
      };
    } catch (BusinessException e) {
      return onFailure.apply(e.getMessage());
    }
  }
}
