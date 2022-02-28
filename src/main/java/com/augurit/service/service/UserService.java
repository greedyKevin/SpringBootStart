package com.augurit.service.service;

import com.augurit.service.config.exception.ServiceException;
import com.augurit.service.constant.Constant;
import com.augurit.service.constant.RedisKeyConstant;
import com.augurit.service.entity.User;
import com.augurit.service.mapper.UserMapper;
import com.augurit.service.utils.jwt.JwtUtils;
import com.augurit.service.utils.redis.RedisUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 用户service
 *
 * @author huang jiahui
 * @date 2021/11/20 13:55
 */
@Service
@AllArgsConstructor
@Slf4j
public class UserService extends ServiceImpl<UserMapper,User> {

    private final RedisUtils redisUtils;

    // todo 修改到配置文件
    private static final String CODE = "augurit214410";

    public boolean login(String account, String password, HttpServletRequest request, HttpServletResponse response) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getAccount, account)
                .eq(User::getPassword, password);
        User result = getOne(queryWrapper);

        String token =JwtUtils.createJwtToken(result);

        response.setHeader(Constant.AUTHORIZATION,token);

        String key = result.getId();

        key = RedisKeyConstant.USER.concat(key);

        redisUtils.set(key,result,60 *30);

        return true;
    }

    public boolean register(String code,User user){

        if(!StringUtils.hasText(user.getAccount())){
            throw new ServiceException("账号不能为空");
        }

        if(!StringUtils.hasText(user.getPassword())){
            throw new ServiceException("密码不能为空");
        }

        if(!StringUtils.hasText(user.getName())){
            throw new ServiceException("姓名不能为空");
        }

        if (CODE.equals(code)){
            save(user);
            return true;
        }

        return false;
    }

}
