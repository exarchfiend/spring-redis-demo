package fun.mjauto.redis.shop.service;

import com.baomidou.mybatisplus.extension.service.IService;
import fun.mjauto.redis.common.dto.ApiResponse;
import fun.mjauto.redis.shop.entity.Shop;

/**
 * @author MJ
 * @description
 * @date 2023/11/5
 */
public interface ShopService extends IService<Shop> {
    ApiResponse<?> selectShopById(Long id);
}
