package top.stillmisty.xiantao.infrastructure.mapper;

import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import top.stillmisty.xiantao.domain.bounty.entity.Bounty;

@Mapper
public interface BountyMapper extends BaseMapper<Bounty> {}
