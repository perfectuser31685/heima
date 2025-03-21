package com.heima.pay.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.pay.entity.dto.PayApplyDTO;
import com.heima.pay.entity.dto.PayOrderFormDTO;
import com.heima.pay.entity.pojo.PayOrder;


/**
 * <p>
 * 支付订单 服务类
 * </p>
 *
 * @author 虎哥
 * @since 2023-05-16
 */
public interface IPayOrderService extends IService<PayOrder> {

    String applyPayOrder(PayApplyDTO applyDTO);

    void tryPayOrderByBalance(PayOrderFormDTO payOrderFormDTO);
}
