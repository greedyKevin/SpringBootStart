package com.augurit.service.controller;

import com.augurit.service.constant.Constant;
import com.augurit.service.entity.User;
import com.augurit.service.service.UserService;
import com.augurit.service.utils.jwt.JwtUtils;
import com.augurit.service.utils.redis.RedisUtils;
import com.augurit.service.utils.result.ResultResponse;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 登录注册
 *
 * @author huang jiahui
 * @date 2021/11/20 13:52
 */
@RestController
@AllArgsConstructor
public class LoginController {

    private final UserService userService;
    private final RedisUtils redisUtils;
    private final Cache<String, Object> caffeineCache;

    /**
     * 登录
     *
     * @param account  账号
     * @param password 密码
     * @return {@link ResultResponse}<{@link Object}>
     */
    @PostMapping("/login")
    public ResultResponse<Object> login(String account, String password, HttpServletRequest request,
                                        HttpServletResponse response) {
        boolean result = userService.login(account, password, request, response);

        if (result) {
            return ResultResponse.success(null);
        }

        return ResultResponse.fail("账号密码错误", null);
    }

    /**
     * 登出
     *
     * @return {@link ResultResponse}<{@link Object}>
     */
    @PostMapping("/logout")
    public ResultResponse<Object> logout(HttpServletRequest request) {
        String userId = JwtUtils.verifyJwtToken(request.getHeader(Constant.AUTHORIZATION));
        redisUtils.del(userId);
        return ResultResponse.success(null);
    }

    /**
     * 注册新用户
     *
     * @param code 注册码
     * @return {@link ResultResponse}<{@link Object}>
     */
    @PostMapping("/register")
    public ResultResponse<Object> register(String code, User user) {
        boolean result = userService.register(code, user);
        if (result) {
            return ResultResponse.success(null);
        }
        return ResultResponse.fail("授权码错误", null);
    }
}
