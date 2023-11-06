package fun.mjauto.redis.shop.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import fun.mjauto.redis.common.dto.ApiResponse;
import fun.mjauto.redis.common.service.CacheService;
import fun.mjauto.redis.shop.entity.Shop;
import fun.mjauto.redis.shop.mapper.ShopMapper;
import fun.mjauto.redis.shop.service.ShopService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

import static fun.mjauto.redis.common.constant.RedisConstants.*;

/**
 * @author MJ
 * @description
 * @date 2023/11/5
 */
@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements ShopService {
    private final CacheService cacheService;
    @Autowired
    public ShopServiceImpl(CacheService cacheClient) {
        this.cacheService = cacheClient;
    }

    @Override
    public ApiResponse<?> selectShopById(Long id) {
        // 缓存穿透 是指客户端请求的数据在缓存中和数据库中都不存在 这样缓存永远不会生效 这些请求都会打到数据库
        // 缓存空对象解决缓存穿透
        // Shop shop = cacheService
        //         .queryWithPassThrough(CACHE_SHOP_KEY, id, Shop.class, this::getById, CACHE_SHOP_TTL, TimeUnit.MINUTES);

        // 缓存击穿 就是一个被高并发访问并且缓存重建业务较复杂的key突然失效了 无数的请求访问会在瞬间给数据库带来巨大的冲击
        // 1.互斥锁解决缓存击穿
        // Shop shop = cacheService
        //         .queryWithMutex(CACHE_SHOP_KEY, id, Shop.class, this::getById, CACHE_SHOP_TTL, TimeUnit.MINUTES);

        // 2.逻辑过期解决缓存击穿  测试 10L TimeUnit.SECONDS
        Shop shop = cacheService
                 .queryWithLogicalExpire(CACHE_SHOP_KEY, id, Shop.class, this::getById, CACHE_SHOP_TTL, TimeUnit.MINUTES);

        if (shop == null) {
            return new ApiResponse<>(500,"店铺不存在",null);
        }
        // 7.返回
        return new ApiResponse<>(200,"成功",shop);
    }
}
