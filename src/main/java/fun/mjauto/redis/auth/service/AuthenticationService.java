package fun.mjauto.redis.auth.service;

import fun.mjauto.redis.auth.dto.LoginFormDTO;
import fun.mjauto.redis.common.dto.ApiResponse;

/**
 * @author MJ
 * @description
 * @date 2023/11/5
 */
public interface AuthenticationService {
    ApiResponse<?> sendCode(String phone);

    ApiResponse<?> login(LoginFormDTO loginForm);
}
