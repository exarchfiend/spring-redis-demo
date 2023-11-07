package fun.mjauto.redis.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import fun.mjauto.redis.order.entity.Voucher;

/**
 * @author MJ
 * @description
 * @date 2023/11/6
 */
public interface VoucherService extends IService<Voucher> {
    Voucher getVoucherById(Long id);
}
