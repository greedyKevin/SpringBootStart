package com.keivn.start.interceptor;

import com.keivn.start.entity.User;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *  登录拦截器
 * @author huang jiahui
 * @date 2021/11/20 13:43
 */

@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        try {
            User user = (User) request.getSession().getAttribute("user");

            if (user == null) {
                return false;
            }
        }catch (Exception e) {

        }
        return true;
    }
}
