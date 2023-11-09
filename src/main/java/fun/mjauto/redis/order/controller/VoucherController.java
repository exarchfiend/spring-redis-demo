package fun.mjauto.redis.order.controller;

import fun.mjauto.redis.common.dto.ApiResponse;
import fun.mjauto.redis.order.service.VoucherService;
import fun.mjauto.redis.shop.service.ShopService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author MJ
 * @description
 * @date 2023/11/8
 */
@RestController
@RequestMapping("/voucher")
public class VoucherController {
    private final VoucherService voucherService;

    @Autowired
    public VoucherController(VoucherService voucherService) {
        this.voucherService = voucherService;
    }

    @PostMapping("/select/id")
    public ApiResponse<?> selectVoucherById(@RequestParam("id") Long id){
        return voucherService.setVoucherStock(id);
    }
}
