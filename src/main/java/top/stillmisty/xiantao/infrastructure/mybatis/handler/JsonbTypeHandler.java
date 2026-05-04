package top.stillmisty.xiantao.infrastructure.mybatis.handler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;
import org.postgresql.util.PGobject;
import tools.jackson.databind.ObjectMapper;
import top.stillmisty.xiantao.domain.fudi.entity.CellConfig;

/**
 * PostgreSQL JSONB 单独对象类型处理器。
 * 对于 CellConfig，会读取 cell_type 列来判断具体子类型。
 */
@MappedTypes({ CellConfig.class })
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
    public void setNonNullParameter(
        PreparedStatement ps,
        int i,
        Object parameter,
        JdbcType jdbcType
    ) throws SQLException {
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
    public Object getNullableResult(ResultSet rs, String columnName)
        throws SQLException {
        String jsonString = rs.getString(columnName);
        return parseJson(jsonString, getCellType(rs));
    }

    @Override
    public Object getNullableResult(ResultSet rs, int columnIndex)
        throws SQLException {
        String jsonString = rs.getString(columnIndex);
        return parseJson(jsonString, getCellType(rs));
    }

    private String getCellType(ResultSet rs) {
        try {
            return rs.getString("cell_type");
        } catch (SQLException e) {
            return null;
        }
    }

    @Override
    public Object getNullableResult(CallableStatement cs, int columnIndex)
        throws SQLException {
        String jsonString = cs.getString(columnIndex);
        return parseJson(jsonString, null);
    }

    private Object parseJson(String jsonString, String cellType)
        throws SQLException {
        if (jsonString == null || jsonString.trim().isEmpty()) {
            return null;
        }
        try {
            Class<?> targetType = resolveConcreteType(cellType);
            return OBJECT_MAPPER.readValue(jsonString, targetType);
        } catch (Exception e) {
            throw new SQLException(
                "Failed to deserialize JSONB to object: " + jsonString,
                e
            );
        }
    }

    private Class<?> resolveConcreteType(String cellType) {
        if ("farm".equals(cellType)) return CellConfig.FarmConfig.class;
        if ("pen".equals(cellType)) return CellConfig.PenConfig.class;
        if (CellConfig.class.isAssignableFrom(propertyType)) return CellConfig
            .EmptyConfig.class;
        return propertyType;
    }
}
