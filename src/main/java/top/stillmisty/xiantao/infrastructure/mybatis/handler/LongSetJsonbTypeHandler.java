package top.stillmisty.xiantao.infrastructure.mybatis.handler;

import java.util.Set;

/** JSONB Set&lt;Long&gt; 专用处理器，显式钉死元素类型，避免无参路径下 Long 被反序列化为 Integer。 */
public class LongSetJsonbTypeHandler extends JsonbCollectionTypeHandler {

  public LongSetJsonbTypeHandler() {
    super(Set.class, Long.class);
  }
}
