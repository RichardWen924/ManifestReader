package com.ruoyi.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 认证授权中心
 */
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.ruoyi")
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class RuoyiAuthApplication {
    public static void main(String[] args) {
        SpringApplication.run(RuoyiAuthApplication.class, args);
        System.out.println("(♥◠‿◠)ﾉﾞ  认证授权中心启动成功   ლ(´ڡ`ლ)ﾞ  ");
    }
}
