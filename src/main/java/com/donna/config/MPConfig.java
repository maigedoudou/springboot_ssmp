package com.donna.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MPConfig {
    //MP拦截器
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor(){
     MybatisPlusInterceptor interceptor=new MybatisPlusInterceptor();
     interceptor.addInnerInterceptor(new PaginationInnerInterceptor());
     return interceptor;
    }
}
