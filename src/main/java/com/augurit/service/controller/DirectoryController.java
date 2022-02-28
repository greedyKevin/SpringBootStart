package com.augurit.service.controller;

import com.augurit.service.constant.Constant;
import com.augurit.service.service.FileService;
import com.augurit.service.utils.jwt.JwtUtils;
import com.augurit.service.utils.result.ResultResponse;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * 文件夹相关
 *
 * @author huang jiahui
 * @date 2022/1/18 16:48
 */
@RestController
@AllArgsConstructor
public class DirectoryController {

    private final FileService fileService;

    /**
     * 重命名文件夹
     * @param path 原路径
     * @param name 新文件夹名
     * @return {@link ResultResponse}<{@link Object}>
     */
    @PutMapping("/renameDirectory")
    public ResultResponse<Object> renameDirectory(HttpServletRequest request,
                                                      String path, String name) throws Exception{
        String userId = JwtUtils.verifyJwtToken(request.getHeader(Constant.AUTHORIZATION));

        fileService.renameDirectory(userId,path,name);

        return ResultResponse.success(null);
    }
}
