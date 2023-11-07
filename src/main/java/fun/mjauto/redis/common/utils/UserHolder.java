package fun.mjauto.redis.common.utils;

import fun.mjauto.redis.auth.dto.UserDTO;

/**
 * @author MJ
 * @description
 * @date 2023/11/6
 */
public class UserHolder {
    private static final ThreadLocal<UserDTO> tl = new ThreadLocal<>();

    public static void saveUser(UserDTO user){
        tl.set(user);
    }

    public static UserDTO getUser(){
        return tl.get();
    }

    public static void removeUser(){
        tl.remove();
    }
}
