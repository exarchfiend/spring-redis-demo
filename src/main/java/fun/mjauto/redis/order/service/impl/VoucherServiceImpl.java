package fun.mjauto.redis.order.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import fun.mjauto.redis.order.entity.SeckillVoucher;
import fun.mjauto.redis.order.entity.Voucher;
import fun.mjauto.redis.order.mapper.VoucherMapper;
import fun.mjauto.redis.order.service.SeckillVoucherService;
import fun.mjauto.redis.order.service.VoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author MJ
 * @description
 * @date 2023/11/6
 */
@Service
public class VoucherServiceImpl extends ServiceImpl<VoucherMapper, Voucher> implements VoucherService {
    private final SeckillVoucherService seckillVoucherService;
    @Autowired
    public VoucherServiceImpl(SeckillVoucherService seckillVoucherService) {
        this.seckillVoucherService = seckillVoucherService;
    }
    @Override
    public Voucher getVoucherById(Long id) {
        Voucher voucher = this.getById(id);
        SeckillVoucher seckillVoucher = seckillVoucherService.getById(id);
        voucher.setStock(seckillVoucher.getStock())
                .setBeginTime(seckillVoucher.getBeginTime())
                .setEndTime(seckillVoucher.getEndTime());
        return voucher;
    }
}
