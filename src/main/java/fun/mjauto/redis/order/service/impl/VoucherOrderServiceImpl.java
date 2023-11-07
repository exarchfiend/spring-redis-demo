package fun.mjauto.redis.order.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import fun.mjauto.redis.common.dto.ApiResponse;
import fun.mjauto.redis.common.service.CacheService;
import fun.mjauto.redis.common.utils.UserHolder;
import fun.mjauto.redis.order.entity.Order;
import fun.mjauto.redis.order.entity.SeckillVoucher;
import fun.mjauto.redis.order.mapper.VoucherOrderMapper;
import fun.mjauto.redis.order.service.SeckillVoucherService;
import fun.mjauto.redis.order.service.VoucherOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * @author MJ
 * @description
 * @date 2023/11/6
 */
@Service
public class VoucherOrderServiceImpl extends ServiceImpl<VoucherOrderMapper, Order> implements VoucherOrderService {
    private final SeckillVoucherService seckillVoucherService;
    private final CacheService cacheService;

    @Autowired
    public VoucherOrderServiceImpl(SeckillVoucherService seckillVoucherService, CacheService cacheService) {
        this.seckillVoucherService = seckillVoucherService;
        this.cacheService = cacheService;
    }

    @Override
    public ApiResponse<?> createOrder(Long id) {
        // 查询优惠券信息
        SeckillVoucher seckillVoucher = seckillVoucherService.getById(id);
        // 判断活动是否开始
        if (seckillVoucher.getBeginTime().isAfter(LocalDateTime.now())) {
            // 活动尚未开始
            return new ApiResponse<>(500, "活动尚未开始", null);
        }
        if (seckillVoucher.getEndTime().isBefore(LocalDateTime.now())) {
            // 活动已经结束
            return new ApiResponse<>(500, "活动已经结束", null);
        }
        // 判断库存是否充足
        if (seckillVoucher.getStock() < 1) {
            // 库存不足
            return new ApiResponse<>(500, "库存不足", null);
        }

        // 从线程中拿到用户信息
        long userId = UserHolder.getUser().getId();
        // 获取互斥锁
        boolean isLock = cacheService.tryLock("order" + userId);
        // 判断
        if(!isLock){
            // 获取锁失败，直接返回失败或者重试
            return new ApiResponse<>(500, "不能重复下单", null);
        }

        try {
            // 根据优惠券id和用户id查询订单信息
            long count = query()
                    .eq("voucher_id", id)
                    .eq("user_id", userId)
                    .count();
            // 判断用户是否已经有订单
            if (count > 0) {
                return new ApiResponse<>(500, "不能重复下单", null);
            }
            // 扣减库存
            boolean success = seckillVoucherService.update()
                    .setSql("stock = stock - 1") // set stock = stock - 1
                    .eq("voucher_id", id) // where id = ?
                    // .eq("stock",seckillVoucher.getStock()) // 乐观锁存在卖不完的问题
                    .gt("stock", 0) // and stock > 0 // 修改条件
                    .update();
            if (!success) {
                // 库存不足
                return new ApiResponse<>(500, "库存不足", null);
            }
            // 创建订单
            Order order = new Order();
            order.setId(cacheService.nextGlobalUniqueId("order"))
                    .setUserId(userId)
                    .setVoucherId(id);
            save(order);
            // 返回订单
            return new ApiResponse<>(200, "秒杀成功", order);
        }finally {
            cacheService.unLock("order" + userId);
        }
    }
}
