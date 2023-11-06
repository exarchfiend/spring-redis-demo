package fun.mjauto.redis.auth.dto;

import lombok.Data;

/**
 * @author MJ
 * @description
 * @date 2023/11/5
 */
@Data
public class LoginFormDTO {
    private String phone;
    private String code;
    private String password;
}
