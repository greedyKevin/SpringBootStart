package com.keivn.start.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.keivn.start.entity.User;
import com.keivn.start.mapper.UserMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

/**
 * todo
 *
 * @author huang jiahui
 * @date 2021/11/20 13:55
 */
@Service
@AllArgsConstructor
public class UserService {
    private final UserMapper userMapper;

    public User add(User user) {
        userMapper.insert(user);
        return user;
    }

    public void login(User user, HttpServletRequest request) {
        QueryWrapper<User> queryWrapper = new QueryWrapper();
        queryWrapper.eq("account", user.getAccount())
                .eq("password", user.getPassword());
        User result = userMapper.selectOne(queryWrapper);

        if(result == null){
            return;
        }
        request.getSession().setAttribute("user",result);
        return;
    }

}
