package top.stillmisty.xiantao.infrastructure.mybatis.handler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.postgresql.util.PGobject;
import tools.jackson.databind.ObjectMapper;
import top.stillmisty.xiantao.domain.bounty.BountyRewardItem;

/** BountyRewardItem 列表专用 type handler，通过 Map 中间层支持 sealed interface 的序列化 */
public class BountyRewardListTypeHandler extends BaseTypeHandler<List<BountyRewardItem>> {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @Override
  public void setNonNullParameter(
      PreparedStatement ps, int i, List<BountyRewardItem> parameter, JdbcType jdbcType)
      throws SQLException {
    try {
      List<Map<String, Object>> maps = parameter.stream().map(BountyRewardItem::toMap).toList();
      PGobject pgObject = new PGobject();
      pgObject.setType("jsonb");
      pgObject.setValue(OBJECT_MAPPER.writeValueAsString(maps));
      ps.setObject(i, pgObject);
    } catch (Exception e) {
      throw new SQLException("Failed to serialize BountyRewardItem list to JSONB", e);
    }
  }

  @Override
  public List<BountyRewardItem> getNullableResult(ResultSet rs, String columnName)
      throws SQLException {
    return deserialize(rs.getString(columnName));
  }

  @Override
  public List<BountyRewardItem> getNullableResult(ResultSet rs, int columnIndex)
      throws SQLException {
    return deserialize(rs.getString(columnIndex));
  }

  @Override
  public List<BountyRewardItem> getNullableResult(CallableStatement cs, int columnIndex)
      throws SQLException {
    return deserialize(cs.getString(columnIndex));
  }

  @SuppressWarnings("unchecked")
  private List<BountyRewardItem> deserialize(String jsonString) throws SQLException {
    if (jsonString == null || jsonString.trim().isEmpty()) {
      return List.of();
    }
    try {
      List<Map<String, Object>> raw = OBJECT_MAPPER.readValue(jsonString, List.class);
      if (raw == null) return List.of();
      return raw.stream()
          .filter(item -> item instanceof Map)
          .map(item -> (Map<String, Object>) item)
          .map(BountyRewardItem::parseOne)
          .toList();
    } catch (Exception e) {
      throw new SQLException("Failed to deserialize BountyRewardItem list: " + jsonString, e);
    }
  }
}
