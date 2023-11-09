package fun.mjauto.redis.order.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import fun.mjauto.redis.common.dto.ApiResponse;
import fun.mjauto.redis.common.service.CacheService;
import fun.mjauto.redis.order.entity.SeckillVoucher;
import fun.mjauto.redis.order.entity.Voucher;
import fun.mjauto.redis.order.mapper.VoucherMapper;
import fun.mjauto.redis.order.service.SeckillVoucherService;
import fun.mjauto.redis.order.service.VoucherService;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static fun.mjauto.redis.common.constant.RedisConstants.*;

/**
 * @author MJ
 * @description
 * @date 2023/11/6
 */
@Service
public class VoucherServiceImpl extends ServiceImpl<VoucherMapper, Voucher> implements VoucherService {
    private final SeckillVoucherService seckillVoucherService;
    private final CacheService cacheService;
    private final StringRedisTemplate stringRedisTemplate;

    @Autowired
    public VoucherServiceImpl(SeckillVoucherService seckillVoucherService, CacheService cacheService, StringRedisTemplate stringRedisTemplate) {
        this.seckillVoucherService = seckillVoucherService;
        this.cacheService = cacheService;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public Voucher getVoucherById(Long id) {
        // 查询优惠券
        Voucher voucher = this.getById(id);
        // 判断优惠券是否为空
        if (voucher == null) {
            return null;
        }
        // 判断优惠券类型是否为秒杀
        if (voucher.getType() == 1) {
            SeckillVoucher seckillVoucher = seckillVoucherService.getById(id);
            voucher.setStock(seckillVoucher.getStock())
                    .setBeginTime(seckillVoucher.getBeginTime())
                    .setEndTime(seckillVoucher.getEndTime());
        }
        // 返回
        return voucher;
    }

    @Override
    public ApiResponse<?> selectVoucherById(Long id) {
        // 缓存击穿 就是一个被高并发访问并且缓存重建业务较复杂的key突然失效了 无数的请求访问会在瞬间给数据库带来巨大的冲击
        // 1.互斥锁解决缓存击穿
        Voucher voucher = cacheService
                .queryWithMutex(CACHE_VOUCHER_KEY, id, Voucher.class, this::getVoucherById, CACHE_VOUCHER_TTL, TimeUnit.MINUTES);

        if (voucher == null) {
            return new ApiResponse<>(500, "优惠券不存在", null);
        }

        return new ApiResponse<>(200, "成功", voucher);
    }

    @Override
    public ApiResponse<?> setVoucherStock(Long id) {
        SeckillVoucher seckillVoucher = seckillVoucherService.getById(id);

        if (seckillVoucher == null) {
            return new ApiResponse<>(500, "秒杀优惠券不存在", null);
        }

        Map<String, String> seckillMap = new HashMap<>();
        seckillMap.put("stock", String.valueOf(Long.valueOf(seckillVoucher.getStock())));
        seckillMap.put("beginTime", String.valueOf(Timestamp.valueOf(seckillVoucher.getBeginTime()).getTime()));
        seckillMap.put("endTime", String.valueOf(Timestamp.valueOf(seckillVoucher.getEndTime()).getTime()));
        stringRedisTemplate.opsForHash().putAll("seckill:stock:" + id, seckillMap);
        return new ApiResponse<>(200, "成功", seckillVoucher);
    }
}
