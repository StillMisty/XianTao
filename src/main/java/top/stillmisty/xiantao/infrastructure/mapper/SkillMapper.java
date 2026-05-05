package top.stillmisty.xiantao.infrastructure.mapper;

import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import top.stillmisty.xiantao.domain.skill.entity.Skill;

@Mapper
public interface SkillMapper extends BaseMapper<Skill> {}
