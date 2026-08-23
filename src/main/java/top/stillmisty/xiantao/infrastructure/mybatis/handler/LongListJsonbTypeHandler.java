package top.stillmisty.xiantao.infrastructure.mybatis.handler;

import java.util.List;

/** JSONB List&lt;Long&gt; 专用处理器，显式钉死元素类型，避免无参路径下 Long 被反序列化为 Integer。 */
public class LongListJsonbTypeHandler extends JsonbCollectionTypeHandler {

  public LongListJsonbTypeHandler() {
    super(List.class, Long.class);
  }
}
