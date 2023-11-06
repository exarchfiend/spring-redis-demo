package fun.mjauto.redis.auth.controller;

import fun.mjauto.redis.auth.dto.LoginFormDTO;
import fun.mjauto.redis.auth.service.AuthenticationService;
import fun.mjauto.redis.common.dto.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author MJ
 * @description
 * @date 2023/11/5
 */
@RestController
@RequestMapping("/auth")
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    @Autowired
    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/code")
    public ApiResponse<?> sendCode(@RequestParam("phone") String phone){
        return authenticationService.sendCode(phone);
    }

    @PostMapping("/login")
    public ApiResponse<?> login(@RequestBody LoginFormDTO loginForm){
        return authenticationService.login(loginForm);
    }
}
