package com.heima.trade.service.Impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.api.client.CartClient;
import com.heima.api.client.ItemClient;
import com.heima.api.dto.ItemDTO;
import com.heima.api.dto.OrderDetailDTO;
import com.heima.trade.entity.dto.OrderFormDTO;
import com.heima.trade.entity.pojo.Order;
import com.heima.trade.entity.pojo.OrderDetail;
import com.heima.trade.mapper.OrderMapper;
import com.heima.trade.service.IOrderDetailService;
import com.heima.trade.service.IOrderService;
import com.hmall.common.exception.BadRequestException;
import com.hmall.common.utils.UserContext;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2023-05-05
 */
@Service
@RequiredArgsConstructor
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements IOrderService {

    //一个用来发送请求，一个用来发现服务实例
//    private final RestTemplate restTemplate;

//    private DiscoveryClient discoveryClient;
    private final IOrderDetailService detailService;
    private final ItemClient itemClient;
    private final CartClient cartClient;

    @Override
    //标记事务的起点，便于TM识别
    @GlobalTransactional
    public Long createOrder(OrderFormDTO orderFormDTO) {
        // 1.订单数据
        Order order = new Order();
        // 1.1.查询商品
        List<OrderDetailDTO> detailDTOS = orderFormDTO.getDetails();
        // 1.2.获取商品id和数量的Map
        Map<Long, Integer> itemNumMap = detailDTOS.stream()
                .collect(Collectors.toMap(OrderDetailDTO::getItemId, OrderDetailDTO::getNum));
        Set<Long> itemIds = itemNumMap.keySet();
        // 1.3.查询商品
//        List<ItemDTO> items = itemService.queryItemByIds(itemIds);
//        if (items == null || items.size() < itemIds.size()) {
//            throw new BadRequestException("商品不存在");
//        }
        //获取服务实例列表,参数名是被请求的模块的application的name
//        List<ServiceInstance> serviceInstances = discoveryClient.getInstances("shangpin-service");
////        //负载均衡
////        ServiceInstance serviceInstance = serviceInstances.get(RandomUtil.randomInt(serviceInstances.size()));
////        //获取uri
////        URI uri = serviceInstance.getUri();
////        //发送请求,五个参数
////        restTemplate.exchange(
////                uri + "/items?ids=${ids}",
////                HttpMethod.GET,
////                null,
////                new ParameterizedTypeReference<List<ItemDTO>>() {},
////                CollUtil.join(,",")
////        )

        //OpenFeign直接拉取
        List<ItemDTO> items = itemClient.queryItemByIds(itemIds);
        if (items == null || items.size() < itemIds.size()) {
            throw new BadRequestException("商品不存在");
        }


        // 1.4.基于商品价格、购买数量计算商品总价：totalFee
        int total = 0;
        for (ItemDTO item : items) {
            total += item.getPrice() * itemNumMap.get(item.getId());
        }
        order.setTotalFee(total);
        // 1.5.其它属性
        order.setPaymentType(orderFormDTO.getPaymentType());
        order.setUserId(UserContext.getUser());
        order.setStatus(1);
        // 1.6.将Order写入数据库order表中
        save(order);

        // 2.保存订单详情
        List<OrderDetail> details = buildDetails(order.getId(), items, itemNumMap);
        detailService.saveBatch(details);

        // 3.清理购物车商品
        cartClient.deleteCartItemByIds(itemIds);

        // 4.扣减库存
        try {
            itemClient.deductStock(detailDTOS);
        } catch (Exception e) {
            throw new RuntimeException("库存不足！");
        }
        return order.getId();
    }

    @Override
    public void markOrderPaySuccess(Long orderId) {
        Order order = new Order();
        order.setId(orderId);
        order.setStatus(2);
        order.setPayTime(LocalDateTime.now());
        updateById(order);
    }

    private List<OrderDetail> buildDetails(Long orderId, List<ItemDTO> items, Map<Long, Integer> numMap) {
        List<OrderDetail> details = new ArrayList<>(items.size());
        for (ItemDTO item : items) {
            OrderDetail detail = new OrderDetail();
            detail.setName(item.getName());
            detail.setSpec(item.getSpec());
            detail.setPrice(item.getPrice());
            detail.setNum(numMap.get(item.getId()));
            detail.setItemId(item.getId());
            detail.setImage(item.getImage());
            detail.setOrderId(orderId);
            details.add(detail);
        }
        return details;
    }
}
