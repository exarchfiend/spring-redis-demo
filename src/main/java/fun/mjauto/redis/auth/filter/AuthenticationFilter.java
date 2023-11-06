package fun.mjauto.redis.auth.filter;

import cn.hutool.core.util.StrUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static fun.mjauto.redis.common.constant.RedisConstants.LOGIN_USER_KEY;
import static fun.mjauto.redis.common.constant.RedisConstants.LOGIN_USER_TTL;

/**
 * @author MJ
 * @description
 * @date 2023/11/5
 */
public class AuthenticationFilter implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1.判断是否需要拦截（是否有用户）
        Boolean hasUser = (Boolean) request.getAttribute("hasUser");
        if (!hasUser) {
            // 没有，需要拦截，设置状态码
            response.setStatus(401);
            // 拦截
            return false;
        }
        // 有用户，则放行
        return true;
    }
}
