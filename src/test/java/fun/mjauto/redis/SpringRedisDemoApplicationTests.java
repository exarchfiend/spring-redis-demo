package fun.mjauto.redis;

import fun.mjauto.redis.common.service.CacheService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@SpringBootTest
class SpringRedisDemoApplicationTests {
    private final CacheService cacheService;

    @Autowired
    public SpringRedisDemoApplicationTests(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    @Test
    void nextId() {
        for (long i = 0; i < 10; i++) {
            System.out.println(cacheService.nextGlobalUniqueId("user"));
        }
    }

    @Test
    void contextLoads() {
        LocalDateTime now = LocalDateTime.now();
        long nowSecond = now.toEpochSecond(ZoneOffset.UTC);
        System.out.println(now);
        System.out.println(nowSecond);
    }
}
