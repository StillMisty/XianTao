package top.stillmisty.xiantao.infrastructure.mybatis.handler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.jspecify.annotations.Nullable;
import org.postgresql.util.PGobject;
import tools.jackson.databind.ObjectMapper;
import top.stillmisty.xiantao.domain.dungeon.entity.DungeonTemplate;

/** DungeonTemplate.spiritConfig JSONB type handler */
public class SpiritConfigTypeHandler extends BaseTypeHandler<DungeonTemplate.SpiritConfig> {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  @Override
  public void setNonNullParameter(
      PreparedStatement ps, int i, DungeonTemplate.SpiritConfig parameter, JdbcType jdbcType)
      throws SQLException {
    try {
      PGobject pg = new PGobject();
      pg.setType("jsonb");
      pg.setValue(MAPPER.writeValueAsString(parameter));
      ps.setObject(i, pg);
    } catch (Exception e) {
      throw new SQLException("Failed to serialize spirit_config", e);
    }
  }

  @Override
  @SuppressWarnings("NullAway")
  public DungeonTemplate.SpiritConfig getNullableResult(ResultSet rs, String columnName)
      throws SQLException {
    return deserialize(rs.getString(columnName));
  }

  @Override
  @SuppressWarnings("NullAway")
  public DungeonTemplate.SpiritConfig getNullableResult(ResultSet rs, int columnIndex)
      throws SQLException {
    return deserialize(rs.getString(columnIndex));
  }

  @Override
  @SuppressWarnings("NullAway")
  public DungeonTemplate.SpiritConfig getNullableResult(CallableStatement cs, int columnIndex)
      throws SQLException {
    return deserialize(cs.getString(columnIndex));
  }

  @SuppressWarnings("NullAway")
  private DungeonTemplate.SpiritConfig deserialize(@Nullable String json) throws SQLException {
    if (json == null || json.trim().isEmpty()) return null;
    try {
      return MAPPER.readValue(json, DungeonTemplate.SpiritConfig.class);
    } catch (Exception e) {
      throw new SQLException("Failed to deserialize spirit_config: " + json, e);
    }
  }
}
