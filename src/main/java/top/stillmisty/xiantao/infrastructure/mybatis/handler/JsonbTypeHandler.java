package top.stillmisty.xiantao.infrastructure.mybatis.handler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.postgresql.util.PGobject;
import tools.jackson.databind.ObjectMapper;

/** PostgreSQL JSONB 单一对象类型处理器。直接按 propertyType 序列化/反序列化。 */
public class JsonbTypeHandler extends BaseTypeHandler<Object> {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private final Class<?> propertyType;

  public JsonbTypeHandler() {
    this.propertyType = Object.class;
  }

  public JsonbTypeHandler(Class<?> propertyType) {
    this.propertyType = propertyType;
  }

  @Override
  public void setNonNullParameter(PreparedStatement ps, int i, Object parameter, JdbcType jdbcType)
      throws SQLException {
    try {
      PGobject pgObject = new PGobject();
      pgObject.setType("jsonb");
      pgObject.setValue(OBJECT_MAPPER.writeValueAsString(parameter));
      ps.setObject(i, pgObject);
    } catch (Exception e) {
      throw new SQLException("Failed to serialize object to JSONB", e);
    }
  }

  @Override
  public Object getNullableResult(ResultSet rs, String columnName) throws SQLException {
    return deserialize(rs.getString(columnName));
  }

  @Override
  public Object getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
    return deserialize(rs.getString(columnIndex));
  }

  @Override
  public Object getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
    return deserialize(cs.getString(columnIndex));
  }

  private Object deserialize(String jsonString) throws SQLException {
    if (jsonString == null || jsonString.trim().isEmpty()) {
      return null;
    }
    try {
      return OBJECT_MAPPER.readValue(jsonString, propertyType);
    } catch (Exception e) {
      throw new SQLException("Failed to deserialize JSONB to object: " + jsonString, e);
    }
  }
}
