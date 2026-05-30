package top.stillmisty.xiantao.infrastructure.mybatis.handler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.jspecify.annotations.Nullable;
import org.postgresql.util.PGobject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.databind.ObjectMapper;
import top.stillmisty.xiantao.domain.event.EffectData;

/** EffectData 专用 type handler — 根据 JSON 结构判断具体子类型 */
public class EffectDataTypeHandler extends BaseTypeHandler<EffectData> {

  private static final Logger log = LoggerFactory.getLogger(EffectDataTypeHandler.class);
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @Override
  public void setNonNullParameter(
      PreparedStatement ps, int i, EffectData parameter, JdbcType jdbcType) throws SQLException {
    try {
      PGobject pgObject = new PGobject();
      pgObject.setType("jsonb");
      pgObject.setValue(OBJECT_MAPPER.writeValueAsString(parameter));
      ps.setObject(i, pgObject);
    } catch (Exception e) {
      throw new SQLException("Failed to serialize EffectData to JSONB", e);
    }
  }

  @Override
  @SuppressWarnings("NullAway")
  public EffectData getNullableResult(ResultSet rs, String columnName) throws SQLException {
    return deserialize(rs.getString(columnName));
  }

  @Override
  @SuppressWarnings("NullAway")
  public EffectData getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
    return deserialize(rs.getString(columnIndex));
  }

  @Override
  @SuppressWarnings("NullAway")
  public EffectData getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
    return deserialize(cs.getString(columnIndex));
  }

  @SuppressWarnings("unchecked")
  private @Nullable EffectData deserialize(String jsonString) throws SQLException {
    if (jsonString == null || jsonString.trim().isEmpty() || "{}".equals(jsonString.trim())) {
      return null;
    }
    try {
      Map<String, Object> map = OBJECT_MAPPER.readValue(jsonString, Map.class);
      if (map.containsKey("choice")) {
        return OBJECT_MAPPER.readValue(jsonString, EffectData.ChoiceOptions.class);
      }
      return EffectData.ActivityConfig.fromMap(map);
    } catch (Exception e) {
      throw new SQLException("Failed to deserialize EffectData: " + jsonString, e);
    }
  }
}
