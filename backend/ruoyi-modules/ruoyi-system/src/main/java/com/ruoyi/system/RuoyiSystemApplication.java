package com.ruoyi.system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 系统模块
 */
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.ruoyi")
@SpringBootApplication
public class RuoyiSystemApplication {
    public static void main(String[] args) {
        SpringApplication.run(RuoyiSystemApplication.class, args);
        System.out.println("(♥◠‿◠)ﾉﾞ  系统模块启动成功   ლ(´ڡ`ლ)ﾞ  ");
    }
}
