package fun.mjauto.redis.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import fun.mjauto.redis.order.entity.Order;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author MJ
 * @description
 * @date 2023/11/6
 */
@Mapper
public interface VoucherOrderMapper extends BaseMapper<Order> {
}