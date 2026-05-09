package top.stillmisty.xiantao.infrastructure.mybatis.handler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.postgresql.util.PGobject;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.ObjectMapper;
import top.stillmisty.xiantao.domain.bounty.BountyRewardPool;

/** BountyRewardPool 列表专用 type handler，通过 Jackson 支持 sealed interface 的序列化 */
public class BountyRewardPoolListTypeHandler extends BaseTypeHandler<List<BountyRewardPool>> {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @Override
  public void setNonNullParameter(
      PreparedStatement ps, int i, List<BountyRewardPool> parameter, JdbcType jdbcType)
      throws SQLException {
    try {
      PGobject pgObject = new PGobject();
      pgObject.setType("jsonb");
      pgObject.setValue(OBJECT_MAPPER.writeValueAsString(parameter));
      ps.setObject(i, pgObject);
    } catch (Exception e) {
      throw new SQLException("Failed to serialize BountyRewardPool list to JSONB", e);
    }
  }

  @Override
  public List<BountyRewardPool> getNullableResult(ResultSet rs, String columnName)
      throws SQLException {
    return deserialize(rs.getString(columnName));
  }

  @Override
  public List<BountyRewardPool> getNullableResult(ResultSet rs, int columnIndex)
      throws SQLException {
    return deserialize(rs.getString(columnIndex));
  }

  @Override
  public List<BountyRewardPool> getNullableResult(CallableStatement cs, int columnIndex)
      throws SQLException {
    return deserialize(cs.getString(columnIndex));
  }

  private List<BountyRewardPool> deserialize(String jsonString) throws SQLException {
    if (jsonString == null || jsonString.trim().isEmpty()) {
      return List.of();
    }
    try {
      JavaType javaType =
          OBJECT_MAPPER
              .getTypeFactory()
              .constructCollectionLikeType(List.class, BountyRewardPool.class);
      return OBJECT_MAPPER.readValue(jsonString, javaType);
    } catch (Exception e) {
      throw new SQLException("Failed to deserialize BountyRewardPool list: " + jsonString, e);
    }
  }
}
