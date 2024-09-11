package com.heima.shangpin;
import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.heima.shangpin.mapper")
public class ShangPinApplication {
    public static void main(String[] args) {
        SpringApplication.run(ShangPinApplication.class,args);
    }
}
