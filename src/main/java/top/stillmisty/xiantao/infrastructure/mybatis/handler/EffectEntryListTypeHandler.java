package top.stillmisty.xiantao.infrastructure.mybatis.handler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.jspecify.annotations.Nullable;
import org.postgresql.util.PGobject;
import tools.jackson.databind.ObjectMapper;
import top.stillmisty.xiantao.service.activity.effect.EffectEntry;
import top.stillmisty.xiantao.service.activity.effect.EffectParams;
import top.stillmisty.xiantao.service.activity.effect.SubEventEffectType;

/** List<EffectEntry> JSONB type handler — 效果列表的类型安全序列化/反序列化 */
public class EffectEntryListTypeHandler extends BaseTypeHandler<List<EffectEntry>> {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  @Override
  public void setNonNullParameter(
      PreparedStatement ps, int i, List<EffectEntry> parameter, JdbcType jdbcType)
      throws SQLException {
    try {
      List<Map<String, Object>> rawList = new ArrayList<>(parameter.size());
      for (EffectEntry entry : parameter) {
        rawList.add(entry.toEffectMap());
      }
      PGobject pgObject = new PGobject();
      pgObject.setType("jsonb");
      pgObject.setValue(MAPPER.writeValueAsString(rawList));
      ps.setObject(i, pgObject);
    } catch (Exception e) {
      throw new SQLException("Failed to serialize EffectEntry list to JSONB", e);
    }
  }

  @Override
  @SuppressWarnings("NullAway")
  public List<EffectEntry> getNullableResult(ResultSet rs, String columnName) throws SQLException {
    return deserialize(rs.getString(columnName));
  }

  @Override
  @SuppressWarnings("NullAway")
  public List<EffectEntry> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
    return deserialize(rs.getString(columnIndex));
  }

  @Override
  @SuppressWarnings("NullAway")
  public List<EffectEntry> getNullableResult(CallableStatement cs, int columnIndex)
      throws SQLException {
    return deserialize(cs.getString(columnIndex));
  }

  @SuppressWarnings("unchecked")
  private @Nullable List<EffectEntry> deserialize(String jsonString) throws SQLException {
    if (jsonString == null || jsonString.trim().isEmpty() || "[]".equals(jsonString.trim())) {
      return List.of();
    }
    try {
      var typeRef = MAPPER.getTypeFactory().constructCollectionType(List.class, Map.class);
      List<Map<String, Object>> rawList = MAPPER.readValue(jsonString, typeRef);
      List<EffectEntry> result = new ArrayList<>(rawList.size());
      for (Map<String, Object> raw : rawList) {
        Object typeObj = raw.remove("type");
        if (!(typeObj instanceof String typeStr)) continue;
        SubEventEffectType type = SubEventEffectType.fromCode(typeStr);
        if (type == null) continue;
        EffectParams params = EffectParams.fromMap(type, raw);
        result.add(new EffectEntry(type, params));
      }
      return result;
    } catch (Exception e) {
      throw new SQLException("Failed to deserialize EffectEntry list: " + jsonString, e);
    }
  }
}
