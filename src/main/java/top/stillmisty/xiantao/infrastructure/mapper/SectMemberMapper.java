package top.stillmisty.xiantao.infrastructure.mapper;

import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import top.stillmisty.xiantao.domain.sect.entity.SectMember;

@Mapper
public interface SectMemberMapper extends BaseMapper<SectMember> {

  /** 原子增加成员贡献 */
  @Update("UPDATE sect_member SET contribution = contribution + #{gain} WHERE user_id = #{userId}")
  int addContribution(@Param("userId") Long userId, @Param("gain") int gain);

  /** 原子扣减成员贡献，贡献不足返回 0 */
  @Update(
      "UPDATE sect_member SET contribution = contribution - #{cost} "
          + "WHERE user_id = #{userId} AND contribution >= #{cost}")
  int deductContributionIfEnough(@Param("userId") Long userId, @Param("cost") int cost);
}
