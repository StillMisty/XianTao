package top.stillmisty.xiantao.infrastructure.mapper;

import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import top.stillmisty.xiantao.domain.team.entity.Team;

@Mapper
public interface TeamMapper extends BaseMapper<Team> {

  @Update("UPDATE team SET member_count = member_count + 1 WHERE id = #{teamId}")
  int incrementMemberCount(@Param("teamId") Long teamId);

  @Update(
      "UPDATE team SET member_count = member_count - 1 WHERE id = #{teamId} AND member_count > 0")
  int decrementMemberCount(@Param("teamId") Long teamId);
}
