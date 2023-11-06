package fun.mjauto.redis.common.config;

import fun.mjauto.redis.auth.filter.AuthenticationFilter;
import fun.mjauto.redis.auth.filter.RefreshTokenFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author MJ
 * @description
 * @date 2023/11/5
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    private final StringRedisTemplate stringRedisTemplate;

    @Autowired
    public WebMvcConfig(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 登录拦截器
        registry.addInterceptor(new AuthenticationFilter())
                .excludePathPatterns(
                        "/auth/code",
                        "/auth/login"
//                        "/shop/select/id"
                ).order(15);
        // token刷新的拦截器
        registry.addInterceptor(new RefreshTokenFilter(stringRedisTemplate))
                .excludePathPatterns(
                        "/auth/code",
                        "/auth/login"
                ).order(10);
    }
}
