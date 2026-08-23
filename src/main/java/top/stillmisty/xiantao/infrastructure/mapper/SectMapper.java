package top.stillmisty.xiantao.infrastructure.mapper;

import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import top.stillmisty.xiantao.domain.sect.entity.Sect;

@Mapper
public interface SectMapper extends BaseMapper<Sect> {

  /** 原子增加宗门资金 */
  @Update("UPDATE sect SET funds = funds + #{amount} WHERE id = #{id}")
  int addFunds(@Param("id") Long id, @Param("amount") long amount);

  /** 原子扣减宗门资金，资金不足返回 0 */
  @Update("UPDATE sect SET funds = funds - #{amount} WHERE id = #{id} AND funds >= #{amount}")
  int deductFundsIfEnough(@Param("id") Long id, @Param("amount") long amount);
}
