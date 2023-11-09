package fun.mjauto.redis.order.controller;

import fun.mjauto.redis.common.dto.ApiResponse;
import fun.mjauto.redis.order.service.VoucherOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author MJ
 * @description
 * @date 2023/11/6
 */
@RestController
@RequestMapping("/order")
public class VoucherOrderController {
    private final VoucherOrderService voucherOrderService;

    @Autowired
    public VoucherOrderController(VoucherOrderService voucherOrderService) {
        this.voucherOrderService = voucherOrderService;
    }

    @PostMapping("/create")
    public ApiResponse<?> createOrder(@RequestParam("id") Long id) {
        return voucherOrderService.createOrder(id);
    }
}
