package top.stillmisty.xiantao.infrastructure.mybatis.handler;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.postgresql.util.PGobject;
import top.stillmisty.xiantao.domain.bounty.BountyRewardPool;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class BountyRewardListTypeHandler extends BaseTypeHandler<List<BountyRewardPool>> {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final JavaType TYPE = MAPPER.getTypeFactory()
        .constructCollectionType(List.class, BountyRewardPool.class);

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, List<BountyRewardPool> parameter, JdbcType jdbcType) throws SQLException {
        try {
            PGobject pgObject = new PGobject();
            pgObject.setType("jsonb");
            pgObject.setValue(MAPPER.writeValueAsString(parameter));
            ps.setObject(i, pgObject);
        } catch (Exception e) {
            throw new SQLException("Failed to serialize BountyRewardPool list to JSONB", e);
        }
    }

    @Override
    public List<BountyRewardPool> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return parseList(rs.getString(columnName));
    }

    @Override
    public List<BountyRewardPool> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return parseList(rs.getString(columnIndex));
    }

    @Override
    public List<BountyRewardPool> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return parseList(cs.getString(columnIndex));
    }

    private List<BountyRewardPool> parseList(String json) throws SQLException {
        if (json == null || json.isBlank()) return List.of();
        try {
            return MAPPER.readValue(json, TYPE);
        } catch (Exception e) {
            throw new SQLException("Failed to deserialize JSONB to List<BountyRewardPool>: " + json, e);
        }
    }
}
