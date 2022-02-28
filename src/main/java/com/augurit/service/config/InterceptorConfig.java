package com.augurit.service.config;

import com.augurit.service.config.interceptor.LoginInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

/**
 * 登录拦截器配置
 *
 * @author huang jiahui
 * @date 2021/11/20 13:47
 */
@Configuration
public class InterceptorConfig implements WebMvcConfigurer {

    /**
     * 登录拦截器
     */
    @Resource
    private LoginInterceptor loginInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // "/favicon.ico" druid池
       registry.addInterceptor(loginInterceptor).addPathPatterns("/**").excludePathPatterns(
               "/login",
               "/register",
               "/logout",
               "/favicon.ico",
               "/file/downloadWithoutAuth/**",
               "/version/**",
               "/getTableData");
    }
}
