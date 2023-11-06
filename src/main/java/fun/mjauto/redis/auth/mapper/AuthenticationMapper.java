package fun.mjauto.redis.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import fun.mjauto.redis.auth.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author MJ
 * @description
 * @date 2023/11/5
 */
@Mapper
public interface AuthenticationMapper extends BaseMapper<User> {
}
