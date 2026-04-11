package top.stillmisty.xiantao.infrastructure.mybatis.handler;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;
import org.postgresql.util.PGobject;
import tools.jackson.databind.ObjectMapper;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

/**
 * PostgreSQL JSONB Type Handler for MyBatis
 */
@MappedTypes({Object.class, Map.class})
public class PgJsonbTypeHandler extends BaseTypeHandler<Object> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final Class<?> propertyType;

    // 默认构造函数
    public PgJsonbTypeHandler() {
        this.propertyType = Object.class;
    }

    // MyBatis-Flex 自动注入字段类型的构造函数
    public PgJsonbTypeHandler(Class<?> propertyType) {
        this.propertyType = propertyType;
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Object parameter, JdbcType jdbcType) throws SQLException {
        try {
            // 使用 PGobject 显式声明这是 jsonb
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
        return parseJson(rs.getString(columnName));
    }

    @Override
    public Object getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return parseJson(rs.getString(columnIndex));
    }

    @Override
    public Object getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return parseJson(cs.getString(columnIndex));
    }

    private Object parseJson(String jsonString) throws SQLException {
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