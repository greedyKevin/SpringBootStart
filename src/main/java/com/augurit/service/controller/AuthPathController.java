package com.augurit.service.controller;

import com.augurit.service.constant.Constant;
import com.augurit.service.service.AuthPathService;
import com.augurit.service.utils.jwt.JwtUtils;
import com.augurit.service.utils.result.ResultResponse;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 开放路径controller
 *
 * @author huang jiahui
 * @date 2022/1/7 14:17
 */
@RestController
@AllArgsConstructor
@RequestMapping("/authPath")
public class AuthPathController {

    private final AuthPathService authPathService;

    /**
     * 获取路径权限
     *
     * @return {@link ResultResponse}<{@link List}<{@link String}>>
     */
    @GetMapping("/list")
    public ResultResponse<List<String>> list(HttpServletRequest request) throws Exception {
        String userId = JwtUtils.verifyJwtToken(request.getHeader(Constant.AUTHORIZATION));
        List<String> result = authPathService.getAll(userId);
        return ResultResponse.success(result);
    }

    /**
     * 添加路径权限
     *
     * @param path 路径
     * @return {@link ResultResponse}<{@link Object}>
     */
    @PostMapping("/add")
    public ResultResponse<Object> add(HttpServletRequest request, String path) throws Exception {
        String userId = JwtUtils.verifyJwtToken(request.getHeader(Constant.AUTHORIZATION));
        authPathService.add(userId, path);
        return ResultResponse.success(null);
    }

    /**
     * 删除权限路径
     *
     * @param path 路径
     * @return {@link ResultResponse}<{@link Object}>
     */
    @DeleteMapping("delete")
    public ResultResponse<Object> delete(HttpServletRequest request, String path) throws Exception {
        String userId = JwtUtils.verifyJwtToken(request.getHeader(Constant.AUTHORIZATION));
        authPathService.delete(userId, path);
        return ResultResponse.success(null);
    }
}
