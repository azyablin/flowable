package com.tander.flowable.client.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.tander.flowable.client.mybatis.interceptor.JobInterceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MybatisPlusConfig {

    @Bean
    @ConditionalOnProperty(name = "app.job.interceptor.enabled", havingValue = "true")
    public MybatisPlusInterceptor mybatisPlusInterceptor(JobInterceptor jobInterceptor) {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        interceptor.addInnerInterceptor(jobInterceptor);
        return interceptor;
    }
}