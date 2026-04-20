package com.ruoyi.manifest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 提单物流业务模块
 */
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.ruoyi")
@SpringBootApplication
public class RuoyiManifestApplication {
    public static void main(String[] args) {
        SpringApplication.run(RuoyiManifestApplication.class, args);
        System.out.println("(♥◠‿◠)ﾉﾞ  物流提单模块启动成功   ლ(´ڡ`ლ)ﾞ  ");
    }
}
