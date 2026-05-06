package top.stillmisty.xiantao.infrastructure.mapper;

import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import top.stillmisty.xiantao.domain.user.entity.User;

@Mapper
public interface UserMapper extends BaseMapper<User> {

  @Update(
      "UPDATE xt_user SET spirit_stones = spirit_stones - #{cost} WHERE id = #{userId} AND spirit_stones >= #{cost}")
  int deductSpiritStonesIfEnough(@Param("userId") Long userId, @Param("cost") int cost);
}
