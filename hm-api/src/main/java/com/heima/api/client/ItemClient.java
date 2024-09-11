package com.heima.api.client;

import com.heima.api.client.fallback.ItemClientFallback;
import com.heima.api.config.DefaultFeignConfig;
import com.heima.api.dto.ItemDTO;
import com.heima.api.dto.OrderDetailDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collection;
import java.util.List;


//@FeignClient("item-service") ：声明服务名称
//@GetMapping ：声明请求方式
//@GetMapping("/items") ：声明请求路径
//@RequestParam("ids") Collection<Long> ids ：声明请求参数
//List<ItemDTO> ：返回值类型
@FeignClient(value = "shangpin-service",
            configuration = DefaultFeignConfig.class,
            fallbackFactory = ItemClientFallback.class)
public interface ItemClient {

    @GetMapping("/items")
    List<ItemDTO> queryItemByIds(@RequestParam("ids") Collection<Long> ids);

    @PutMapping("/items/stock/deduct")
    void deductStock(@RequestBody List<OrderDetailDTO> items);
}