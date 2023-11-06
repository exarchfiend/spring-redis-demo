package fun.mjauto.redis.common.aggregate;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author MJ
 * @description
 * @date 2023/11/6
 */
@Data
public class LogicalExpiryRedisData {
    private LocalDateTime ExpireTime;
    private Object data;
}
