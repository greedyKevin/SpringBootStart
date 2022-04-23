package com.kevin.start.config;

import com.augurit.service.config.exception.NoAuthException;
import com.augurit.service.constant.Constant;
import com.augurit.service.constant.RedisKeyConstant;
import com.augurit.service.constant.UrlConstant;
import com.augurit.service.entity.User;
import com.augurit.service.service.AuthPathService;
import com.kevin.start.util.JwtUtils;
import com.augurit.service.utils.redis.RedisUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLDecoder;
import java.util.List;

/**
 *  登录拦截器
 * @author huang jiahui
 * @date 2021/11/20 13:43
 */

@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Resource
    private RedisUtils redisUtils;

    @Resource
    private AuthPathService authPathService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 接口地址
        String uri = request.getRequestURI();

        uri = URLDecoder.decode(uri, "UTF-8");

        // 如果是文件接口开头
        if (uri.startsWith(UrlConstant.FILE_GET)) {
            // 替换
            uri = uri.replace(UrlConstant.FILE_GET, "");
            // 获取用户id
            String userId = uri.replaceAll("/.*", "");

            List<String> list = authPathService.getAll(userId);

            if (list != null) {
                for (String regex : list) {
                    if (uri.equals(regex)) {
                        return true;
                    }

                    if (isMatched(uri, regex)) {
                        return true;
                    }
                }
            }
        }

        String key = JwtUtils.verifyJwtToken(request.getHeader(Constant.AUTHORIZATION));

        if (!StringUtils.hasText(key)) {
            throw new NoAuthException("当前用户未登录");
        }

        key = RedisKeyConstant.USER.concat(key);

        com.augurit.service.entity.User result = (User) redisUtils.get(key);

        if (null == result) {
            throw new NoAuthException("登录过期");
        }

        redisUtils.set(key, result, 60 * 30);
        return true;
    }


    private boolean isMatched(String uri, String regex) {
        if (!regex.endsWith(".*")) {
            return false;
        }

        String[] uriArr = uri.split("/");
        String[] regexArr = regex.split("/");

        // 路径比他长
        if (regexArr.length > uriArr.length) {
            return false;
        }

        for (int i = 0; i < regexArr.length - 1; i++) {
            if (!regexArr[i].equals(uriArr[i])) {
                return false;
            }
        }

        return true;
    }
}
