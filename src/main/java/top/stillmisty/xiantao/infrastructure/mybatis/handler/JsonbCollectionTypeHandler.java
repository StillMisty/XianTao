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

/**
 * PostgreSQL JSONB 集合类型处理器，专用于 List/Set 字段。
 * Jackson 会根据 propertyType（List/Set 接口）自动选择对应的实现类（ArrayList/HashSet）。
 */
public class JsonbCollectionTypeHandler extends BaseTypeHandler<Object> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final Class<?> propertyType;
    private final Class<?> genericType;

    public JsonbCollectionTypeHandler() {
        this.propertyType = Object.class;
        this.genericType = null;
    }

    public JsonbCollectionTypeHandler(Class<?> propertyType) {
        this.propertyType = propertyType;
        this.genericType = null;
    }

    public JsonbCollectionTypeHandler(
        Class<?> propertyType,
        Class<?> genericType
    ) {
        this.propertyType = propertyType;
        this.genericType = genericType;
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
            throw new SQLException(
                "Failed to serialize collection to JSONB",
                e
            );
        }
    }

    @Override
    public Object getNullableResult(ResultSet rs, String columnName)
        throws SQLException {
        return deserialize(rs.getString(columnName));
    }

    @Override
    public Object getNullableResult(ResultSet rs, int columnIndex)
        throws SQLException {
        return deserialize(rs.getString(columnIndex));
    }

    @Override
    public Object getNullableResult(CallableStatement cs, int columnIndex)
        throws SQLException {
        return deserialize(cs.getString(columnIndex));
    }

    private Object deserialize(String jsonString) throws SQLException {
        if (jsonString == null || jsonString.trim().isEmpty()) {
            return null;
        }
        try {
            if (genericType != null) {
                JavaType javaType = OBJECT_MAPPER.getTypeFactory()
                    .constructCollectionLikeType(propertyType, genericType);
                return OBJECT_MAPPER.readValue(jsonString, javaType);
            }
            return OBJECT_MAPPER.readValue(jsonString, List.class);
        } catch (Exception e) {
            throw new SQLException(
                "Failed to deserialize JSONB to collection: " + jsonString,
                e
            );
        }
    }
}
