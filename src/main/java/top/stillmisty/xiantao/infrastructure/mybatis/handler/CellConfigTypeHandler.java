package top.stillmisty.xiantao.infrastructure.mybatis.handler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.jspecify.annotations.Nullable;
import org.postgresql.util.PGobject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.databind.ObjectMapper;
import top.stillmisty.xiantao.domain.fudi.entity.CellConfig;

/** CellConfig 专用 type handler，读取 cell_type 列判断具体子类型 */
public class CellConfigTypeHandler extends BaseTypeHandler<CellConfig> {

  private static final Logger log = LoggerFactory.getLogger(CellConfigTypeHandler.class);
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @Override
  public void setNonNullParameter(
      PreparedStatement ps, int i, CellConfig parameter, JdbcType jdbcType) throws SQLException {
    try {
      PGobject pgObject = new PGobject();
      pgObject.setType("jsonb");
      pgObject.setValue(OBJECT_MAPPER.writeValueAsString(parameter));
      ps.setObject(i, pgObject);
    } catch (Exception e) {
      throw new SQLException("Failed to serialize CellConfig to JSONB", e);
    }
  }

  @Override
  @SuppressWarnings("NullAway")
  public CellConfig getNullableResult(ResultSet rs, String columnName) throws SQLException {
    return deserialize(rs.getString(columnName), getCellType(rs));
  }

  @Override
  @SuppressWarnings("NullAway")
  public CellConfig getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
    return deserialize(rs.getString(columnIndex), getCellType(rs));
  }

  @Override
  @SuppressWarnings("NullAway")
  public CellConfig getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
    return deserialize(cs.getString(columnIndex), (String) null);
  }

  private @Nullable String getCellType(ResultSet rs) {
    try {
      return rs.getString("cell_type");
    } catch (SQLException e) {
      return null;
    }
  }

  private @Nullable CellConfig deserialize(String jsonString, @Nullable String cellType)
      throws SQLException {
    if (jsonString == null || jsonString.trim().isEmpty() || "{}".equals(jsonString.trim())) {
      return null;
    }
    try {
      Class<?> targetType = resolveConcreteType(cellType);
      return (CellConfig) OBJECT_MAPPER.readValue(jsonString, targetType);
    } catch (Exception e) {
      throw new SQLException("Failed to deserialize CellConfig: " + jsonString, e);
    }
  }

  private Class<?> resolveConcreteType(@Nullable String cellType) {
    if ("FARM".equals(cellType)) return CellConfig.FarmConfig.class;
    if ("PEN".equals(cellType)) return CellConfig.PenConfig.class;
    if (cellType != null) {
      log.warn("Unknown cell_type '{}', falling back to EmptyConfig", cellType);
    }
    return CellConfig.EmptyConfig.class;
  }
}
