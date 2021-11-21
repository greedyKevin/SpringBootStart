package com.kevin.start.controller;

import com.kevin.start.entity.User;
import com.kevin.start.result.ResultResponse;
import com.kevin.start.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;


/**
 * todo
 *
 * @author huang jiahui
 * @date 2021/11/20 13:52
 */
@RestController
@AllArgsConstructor
public class LoginController {

    private final UserService userService;

    @PostMapping("/add")
    public ResultResponse add(User user){
        User result = userService.add(user);
        return ResultResponse.success(result);
    }

    @PostMapping("/login")
    public void login(User user, HttpServletRequest request){
        userService.login(user, request);
    }
}
