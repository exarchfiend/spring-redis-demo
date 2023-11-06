package fun.mjauto.redis.shop.controller;

import fun.mjauto.redis.common.dto.ApiResponse;
import fun.mjauto.redis.shop.service.ShopService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author MJ
 * @description
 * @date 2023/11/5
 */
@RestController
@RequestMapping("/shop")
public class ShopController {
    private final ShopService shopService;

    @Autowired
    public ShopController(ShopService shopService) {
        this.shopService = shopService;
    }

    @PostMapping("/select/id")
    public ApiResponse<?> selectShopById(@RequestParam("id") Long id){
        return shopService.selectShopById(id);
    }
}
