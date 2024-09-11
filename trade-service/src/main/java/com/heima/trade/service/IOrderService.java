package com.heima.trade.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.trade.entity.dto.OrderFormDTO;
import com.heima.trade.entity.pojo.Order;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 虎哥
 * @since 2023-05-05
 */
public interface IOrderService extends IService<Order> {

    Long createOrder(OrderFormDTO orderFormDTO);

    void markOrderPaySuccess(Long orderId);
}
