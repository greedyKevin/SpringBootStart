package com.kevin.start.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

/**
 * 拦截路径配置
 *
 * @author huang jiahui
 * @date 2021/11/20 13:47
 */
@Configuration
public class LoginConfig implements WebMvcConfigurer {

    @Resource
    private LoginInterceptor loginInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
       registry.addInterceptor(loginInterceptor).addPathPatterns("/**").excludePathPatterns("/login","/add");
    }
}
