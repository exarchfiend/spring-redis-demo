package fun.mjauto.redis.auth.filter;

import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import fun.mjauto.redis.common.dto.ApiResponse;
import fun.mjauto.redis.common.utils.UserHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author MJ
 * @description
 * @date 2023/11/5
 */
public class AuthenticationFilter implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1.判断是否需要拦截（ThreadLocal中是否有用户）
        if (UserHolder.getUser() == null) {
            // 没有，需要拦截
            response.setContentType("application/json"); // 设置响应内容类型为JSON
            response.setCharacterEncoding("UTF-8"); // 设置字符编码为UTF-8
            response.getWriter().write(JSONUtil.toJsonStr(new ApiResponse<>(500,"未登录",null)));
            // 拦截
            return false;
        }
        // 有用户，则放行
        return true;
    }
}
