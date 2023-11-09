package fun.mjauto.redis.order.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import fun.mjauto.redis.common.dto.ApiResponse;
import fun.mjauto.redis.common.service.CacheService;
import fun.mjauto.redis.common.utils.UserHolder;
import fun.mjauto.redis.order.entity.Order;
import fun.mjauto.redis.order.entity.SeckillVoucher;
import fun.mjauto.redis.order.mapper.VoucherOrderMapper;
import fun.mjauto.redis.order.service.SeckillVoucherService;
import fun.mjauto.redis.order.service.VoucherOrderService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author MJ
 * @description
 * @date 2023/11/6
 */
@Slf4j
@Service
public class VoucherOrderServiceImpl extends ServiceImpl<VoucherOrderMapper, Order> implements VoucherOrderService {
    private final SeckillVoucherService seckillVoucherService;
    private final CacheService cacheService;
    private final RedissonClient redissonClient;
    private final StringRedisTemplate stringRedisTemplate;

    private static final DefaultRedisScript<Long> SECKILL_SCRIPT;

    static {
        SECKILL_SCRIPT = new DefaultRedisScript<>();
        SECKILL_SCRIPT.setLocation(new ClassPathResource("seckill.lua"));
        SECKILL_SCRIPT.setResultType(Long.class);
    }

    @Autowired
    public VoucherOrderServiceImpl(SeckillVoucherService seckillVoucherService, CacheService cacheService, RedissonClient redissonClient, StringRedisTemplate stringRedisTemplate) {
        this.seckillVoucherService = seckillVoucherService;
        this.cacheService = cacheService;
        this.redissonClient = redissonClient;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    private static final ExecutorService SECKILL_ORDER_EXECUTOR = Executors.newSingleThreadExecutor();

    @PostConstruct
    private void init() {
        SECKILL_ORDER_EXECUTOR.submit(new VoucherOrderHandler());
    }

    private class VoucherOrderHandler implements Runnable {

        @Override
        public void run() {
            while (true) {
                try {
                    // 1.获取消息队列中的订单信息 XREADGROUP GROUP g1 c1 COUNT 1 BLOCK 2000 STREAMS s1 >
                    List<MapRecord<String, Object, Object>> list = stringRedisTemplate.opsForStream().read(
                            Consumer.from("g1", "c1"),
                            StreamReadOptions.empty().count(1).block(Duration.ofSeconds(2)),
                            StreamOffset.create("stream.orders", ReadOffset.lastConsumed())
                    );
                    // 2.判断订单信息是否为空
                    if (list == null || list.isEmpty()) {
                        // 如果为null，说明没有消息，继续下一次循环
                        continue;
                    }
                    // 解析数据
                    MapRecord<String, Object, Object> record = list.get(0);
                    Map<Object, Object> value = record.getValue();
                    Order order = BeanUtil.fillBeanWithMap(value, new Order(), true);
                    // 3.创建订单
                    createVoucherOrder(order);
                    // 4.确认消息 XACK
                    stringRedisTemplate.opsForStream().acknowledge("s1", "g1", record.getId());
                } catch (Exception e) {
                    log.error("处理订单异常", e);
                    handlePendingList();
                }
            }
        }

        private void handlePendingList() {
            while (true) {
                try {
                    // 1.获取pending-list中的订单信息 XREADGROUP GROUP g1 c1 COUNT 1 BLOCK 2000 STREAMS s1 0
                    List<MapRecord<String, Object, Object>> list = stringRedisTemplate.opsForStream().read(
                            Consumer.from("g1", "c1"),
                            StreamReadOptions.empty().count(1),
                            StreamOffset.create("stream.orders", ReadOffset.from("0"))
                    );
                    // 2.判断订单信息是否为空
                    if (list == null || list.isEmpty()) {
                        // 如果为null，说明没有异常消息，结束循环
                        break;
                    }
                    // 解析数据
                    MapRecord<String, Object, Object> record = list.get(0);
                    Map<Object, Object> value = record.getValue();
                    Order order = BeanUtil.fillBeanWithMap(value, new Order(), true);
                    // 3.创建订单
                    createVoucherOrder(order);
                    // 4.确认消息 XACK
                    stringRedisTemplate.opsForStream().acknowledge("s1", "g1", record.getId());
                } catch (Exception e) {
                    log.error("处理订单异常", e);
                }
            }
        }
    }


    @Override
    public ApiResponse<?> createOrder(Long id) {
        Long userId = UserHolder.getUser().getId();
        long orderId = cacheService.nextGlobalUniqueId("order");
        // 1.执行lua脚本
        Long result = stringRedisTemplate.execute(
                SECKILL_SCRIPT,
                Collections.emptyList(),
                id.toString(), String.valueOf(userId), String.valueOf(orderId), String.valueOf(System.currentTimeMillis())
        );
        if (result == null) {
            return new ApiResponse<>(500, "系统错误", null);
        }
        // 2.判断结果是否为0
        return switch (result.intValue()) {
            case 0 -> new ApiResponse<>(200, "成功", null);
            case 1 -> new ApiResponse<>(500, "库存不足", null);
            case 2 -> new ApiResponse<>(500, "不能重复下单", null);
            default -> new ApiResponse<>(500, "系统错误", null);
        };
    }

    private void createVoucherOrder(Order order) {
        Long userId = order.getUserId();
        Long voucherId = order.getVoucherId();

        // 创建锁对象
        RLock lock = redissonClient.getLock("lock:order" + userId);
        // 尝试获取锁
        boolean isLock = lock.tryLock();
        // 判断
        if (!isLock) {
            // 获取锁失败，直接返回失败或者重试
            log.info("不能重复下单");
            return;
        }

        try {
            // 根据优惠券id和用户id查询订单信息
            long count = query()
                    .eq("voucher_id", voucherId)
                    .eq("user_id", userId)
                    .count();
            // 判断用户是否已经有订单
            if (count > 0) {
                log.info("不能重复下单");
                return;
            }
            // 扣减库存
            boolean success = seckillVoucherService.update()
                    .setSql("stock = stock - 1") // set stock = stock - 1
                    .eq("voucher_id", voucherId) // where id = ?
                    // .eq("stock",seckillVoucher.getStock()) // 乐观锁存在卖不完的问题
                    .gt("stock", 0) // and stock > 0 // 修改条件
                    .update();
            if (!success) {
                // 库存不足
                log.info("库存不足");
                return;
            }
            // 创建订单
            save(order);
            // 返回订单
            log.info("秒杀成功");
        } finally {
            cacheService.unLock("order" + userId);
        }
    }
}
