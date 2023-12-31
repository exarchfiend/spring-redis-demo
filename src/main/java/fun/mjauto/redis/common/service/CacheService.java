package fun.mjauto.redis.common.service;

import cn.hutool.json.JSONUtil;
import fun.mjauto.redis.shop.entity.Shop;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * @author MJ
 * @description
 * @date 2023/11/5
 */
public interface CacheService {
    public void set(String key, Object value, Long time, TimeUnit unit);

    void setWithLogicalExpire(String key, Object value, Long time, TimeUnit unit);

    /**
     * 缓存空对象解决缓存穿透
     *
     * @param keyPrefix  redis的key字段
     * @param id         实体类id。
     * @param type       实体类类型。
     * @param dbFallback 查询数据库函数。
     * @param time       过期时间数值。
     * @param unit       过期时间单位。
     * @return 查询到的实体类
     */
    public <R, ID> R queryWithPassThrough(String keyPrefix, ID id, Class<R> type, Function<ID, R> dbFallback, Long time, TimeUnit unit);

    public <R, ID> R queryWithMutex(String keyPrefix, ID id, Class<R> type, Function<ID, R> dbFallback, Long time, TimeUnit unit);

    public <R, ID> R queryWithLogicalExpire(String keyPrefix, ID id, Class<R> type, Function<ID, R> dbFallback, Long time, TimeUnit unit);
}
