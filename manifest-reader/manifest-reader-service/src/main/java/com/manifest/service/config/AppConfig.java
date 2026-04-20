package com.manifest.service.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Date;

@Configuration
public class AppConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }

    /**
     * LibreOffice 转换器配置
     */
    @Bean
    public org.jodconverter.core.DocumentConverter documentConverter() {
        return org.jodconverter.local.LocalConverter.builder()
                .officeHome("/Applications/LibreOffice.app/Contents") // Mac 默认路径
                .build();
    }

    @Component
    static class MetaFill implements MetaObjectHandler {
        @Override
        public void insertFill(MetaObject meta) {
            this.strictInsertFill(meta, "createTime", Date.class, new Date());
            this.strictInsertFill(meta, "updateTime", Date.class, new Date());
            this.strictInsertFill(meta, "createBy",   String.class, "system");
            this.strictInsertFill(meta, "updateBy",   String.class, "system");
        }
        @Override
        public void updateFill(MetaObject meta) {
            this.strictUpdateFill(meta, "updateTime", Date.class, new Date());
            this.strictUpdateFill(meta, "updateBy",   String.class, "system");
        }
    }
}
