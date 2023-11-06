package fun.mjauto.redis.auth.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import fun.mjauto.redis.auth.dto.LoginFormDTO;
import fun.mjauto.redis.auth.dto.UserDTO;
import fun.mjauto.redis.auth.entity.User;
import fun.mjauto.redis.auth.mapper.AuthenticationMapper;
import fun.mjauto.redis.auth.service.AuthenticationService;
import fun.mjauto.redis.auth.utils.RegexUtils;
import fun.mjauto.redis.common.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static fun.mjauto.redis.common.constant.RedisConstants.*;
import static fun.mjauto.redis.common.constant.SystemConstants.USER_NICK_NAME_PREFIX;

/**
 * @author MJ
 * @description
 * @date 2023/11/5
 */
@Slf4j
@Service
public class AuthenticationServiceImpl extends ServiceImpl<AuthenticationMapper, User> implements AuthenticationService {
    private final StringRedisTemplate stringRedisTemplate;

    @Autowired
    public AuthenticationServiceImpl(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public ApiResponse<?> sendCode(String phone) {
        // 1.校验手机号
        if (RegexUtils.isPhoneInvalid(phone)) {
            // 2.如果不符合，返回错误信息
            return new ApiResponse<>(500, "验证码获取失败", null);
        }
        // 3.符合，生成验证码(模拟)
        String code = RandomUtil.randomNumbers(6);

        // 4.保存验证码到 redis
        stringRedisTemplate.opsForValue().set(LOGIN_CODE_KEY + phone, code, LOGIN_CODE_TTL, TimeUnit.MINUTES);

        // 5.发送验证码(模拟)
        log.debug("短信验证码发送成功，验证码：{}", code);
        // 返回成功
        return new ApiResponse<>(200, "成功", null);
    }

    @Override
    public ApiResponse<?> login(LoginFormDTO loginForm) {
        // 1.校验手机号
        String phone = loginForm.getPhone();
        if (RegexUtils.isPhoneInvalid(phone)) {
            // 2.如果不符合，返回错误信息
            return new ApiResponse<>(500, "手机号格式错误", null);
        }
        // 3.从redis获取验证码并校验
        String cacheCode = stringRedisTemplate.opsForValue().get(LOGIN_CODE_KEY + phone);
        String code = loginForm.getCode();
        if (cacheCode == null || !cacheCode.equals(code)) {
            // 不一致，报错
            return new ApiResponse<>(500, "验证码错误", null);
        }

        // 验证成功需要删除redis中的验证码

        // 4.一致，根据手机号查询用户 select * from tb_user where phone = ?
        User user = query().eq("phone", phone).one();

        // 5.判断用户是否存在
        if (user == null) {
            // 6.不存在，创建新用户并保存
            user = createUserWithPhone(phone);
        }

        // 7.保存用户信息到 redis中
        // 7.1.随机生成token，作为登录令牌
        String token = UUID.randomUUID().toString(true);
        // 7.2.将User对象转为HashMap存储
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        Map<String, Object> userMap = BeanUtil.beanToMap(userDTO, new HashMap<>(),
                CopyOptions.create()
                        .setIgnoreNullValue(true)
                        .setFieldValueEditor((fieldName, fieldValue) -> fieldValue.toString()));
        // 7.3.存储
        String tokenKey = LOGIN_USER_KEY + token;
        stringRedisTemplate.opsForHash().putAll(tokenKey, userMap);
        // 7.4.设置token有效期
        stringRedisTemplate.expire(tokenKey, LOGIN_USER_TTL, TimeUnit.MINUTES);

        // 8.返回token
        return new ApiResponse<>(200, "成功", token);
    }

    private User createUserWithPhone(String phone) {
        // 1.创建用户
        User user = new User();
        user.setPhone(phone);
        user.setNickName(USER_NICK_NAME_PREFIX + RandomUtil.randomString(10));
        // 2.保存用户
        save(user);
        return user;
    }
}
