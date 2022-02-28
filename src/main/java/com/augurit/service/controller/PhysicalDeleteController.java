package com.augurit.service.controller;

import com.augurit.service.constant.Constant;
import com.augurit.service.service.PhysicalDeleteService;
import com.augurit.service.utils.jwt.JwtUtils;
import com.augurit.service.utils.result.ResultResponse;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * 物理删除
 *
 * @author huang jiahui
 * @date 2022/1/8 13:44
 */
@RestController
@AllArgsConstructor
@RequestMapping("/physical")
public class PhysicalDeleteController {
    private final PhysicalDeleteService physicalDeleteService;

    /**
     * 物理删除单个文件
     *
     * @param path     路径
     * @param fileName 文件名
     * @return {@link ResultResponse}<{@link Object}>
     */
    @DeleteMapping("/delete")
    public ResultResponse<Object> delete(HttpServletRequest request,
                                         String path, String fileName) throws Exception {
        String userId = JwtUtils.verifyJwtToken(request.getHeader(Constant.AUTHORIZATION));
        physicalDeleteService.delete(userId, path, fileName);
        return ResultResponse.success(null);
    }

    /**
     * 物理删除文件夹以及下面的文件
     *
     * @param path 路径
     * @return {@link ResultResponse}<{@link Object}>
     */
    @DeleteMapping("/deleteAll")
    public ResultResponse<Object> deleteDirectory(HttpServletRequest request,
                                                  String path) throws Exception {
        String userId = JwtUtils.verifyJwtToken(request.getHeader(Constant.AUTHORIZATION));
        physicalDeleteService.deleteDirectory(userId, path);
        return ResultResponse.success(null);
    }

}
