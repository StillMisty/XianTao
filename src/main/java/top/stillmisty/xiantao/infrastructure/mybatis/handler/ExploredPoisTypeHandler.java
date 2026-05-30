package top.stillmisty.xiantao.infrastructure.mybatis.handler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.jspecify.annotations.Nullable;
import org.postgresql.util.PGobject;
import tools.jackson.databind.ObjectMapper;
import top.stillmisty.xiantao.domain.dungeon.entity.DungeonInstance;

/** DungeonInstance.exploredPois JSONB type handler */
public class ExploredPoisTypeHandler
    extends BaseTypeHandler<List<DungeonInstance.ExploredPoiRecord>> {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  @Override
  public void setNonNullParameter(
      PreparedStatement ps,
      int i,
      List<DungeonInstance.ExploredPoiRecord> parameter,
      JdbcType jdbcType)
      throws SQLException {
    try {
      PGobject pg = new PGobject();
      pg.setType("jsonb");
      pg.setValue(MAPPER.writeValueAsString(parameter));
      ps.setObject(i, pg);
    } catch (Exception e) {
      throw new SQLException("Failed to serialize explored_pois", e);
    }
  }

  @Override
  public @Nullable List<DungeonInstance.ExploredPoiRecord> getNullableResult(
      ResultSet rs, String columnName) throws SQLException {
    return deserialize(rs.getString(columnName));
  }

  @Override
  public @Nullable List<DungeonInstance.ExploredPoiRecord> getNullableResult(
      ResultSet rs, int columnIndex) throws SQLException {
    return deserialize(rs.getString(columnIndex));
  }

  @Override
  public @Nullable List<DungeonInstance.ExploredPoiRecord> getNullableResult(
      CallableStatement cs, int columnIndex) throws SQLException {
    return deserialize(cs.getString(columnIndex));
  }

  @SuppressWarnings("NullAway")
  private List<DungeonInstance.ExploredPoiRecord> deserialize(@Nullable String json)
      throws SQLException {
    if (json == null || json.trim().isEmpty()) return null;
    try {
      return MAPPER.readValue(
          json,
          MAPPER
              .getTypeFactory()
              .constructCollectionType(List.class, DungeonInstance.ExploredPoiRecord.class));
    } catch (Exception e) {
      throw new SQLException("Failed to deserialize explored_pois: " + json, e);
    }
  }
}
