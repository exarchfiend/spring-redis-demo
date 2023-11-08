package fun.mjauto.redis.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import fun.mjauto.redis.common.dto.ApiResponse;
import fun.mjauto.redis.order.entity.Order;

/**
 * @author MJ
 * @description
 * @date 2023/11/6
 */
public interface VoucherOrderService extends IService<Order> {
    ApiResponse<?> createOrder(Long id) throws InterruptedException;
}
