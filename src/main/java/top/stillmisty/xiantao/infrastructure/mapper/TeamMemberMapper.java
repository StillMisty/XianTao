package top.stillmisty.xiantao.infrastructure.mapper;

import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import top.stillmisty.xiantao.domain.team.entity.TeamMember;

@Mapper
public interface TeamMemberMapper extends BaseMapper<TeamMember> {}
