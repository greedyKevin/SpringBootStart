package com.augurit.service.controller;

import com.alibaba.fastjson.JSONObject;
import com.augurit.service.constant.Constant;
import com.augurit.service.service.FileService;
import com.augurit.service.utils.jwt.JwtUtils;
import com.augurit.service.utils.result.ResultResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 文件相关controller
 *
 * @author huang jiahui
 * @date 2021/11/18 13:44
 */
@RestController
@AllArgsConstructor
@Slf4j
@RequestMapping("/file")
public class FileController {

    private final FileService fileService;
    /**
     * 获取用户所有文件
     *
     * @param path 路径
     * @return {@link ResultResponse}<{@link List}<{@link JSONObject}>>
     */
    @GetMapping("getAll")
    public ResultResponse<List<JSONObject>> getAll(HttpServletRequest request, String path) throws Exception {

        String userId = JwtUtils.verifyJwtToken(request.getHeader(Constant.AUTHORIZATION));

        List<JSONObject> list = fileService.getAll(userId, path);
        return ResultResponse.success(list);
    }

    /**
     * 逻辑删除单文件
     *
     * @param path     路径
     * @param fileName 文件名
     * @return {@link ResultResponse}<{@link Object}>
     */
    @DeleteMapping("/delete")
    public ResultResponse<Object> delete(HttpServletRequest request, String path, String fileName) throws Exception {
        String userId = JwtUtils.verifyJwtToken(request.getHeader(Constant.AUTHORIZATION));

        fileService.delete(userId, path, fileName);
        return ResultResponse.success(null);
    }

    /**
     * 逻辑删除所有文件
     *
     * @param path 路径
     * @return {@link ResultResponse}<{@link Object}>
     */
    @DeleteMapping("/deleteAll")
    public ResultResponse<Object> deleteAll(HttpServletRequest request, String path) throws Exception {
        String userId = JwtUtils.verifyJwtToken(request.getHeader(Constant.AUTHORIZATION));

        fileService.deleteDirectory(userId, path);
        return ResultResponse.success(null);
    }

    /**
     * 创建文件夹
     *
     * @param name 文件夹名称以"/"结尾
     * @return {@link ResultResponse}<{@link Object}>
     */
    @PostMapping("/createDirectory")
    public ResultResponse<Object> createDirectory(HttpServletRequest request, String name) throws Exception {
        String userId = JwtUtils.verifyJwtToken(request.getHeader(Constant.AUTHORIZATION));

        fileService.createDirectory(userId, name);

        return ResultResponse.success(null);
    }

//    /**
//     * 读取json文件
//     *
//     * @param fileName 文件名
//     * @param path     路径
//     * @return {@link ResultResponse}<{@link Object}>
//     */
//    @GetMapping("/getJsonFile")
//    public ResultResponse<Object> getJsonFile(HttpServletRequest request, String path, String fileName) throws Exception {
//        String userId = JwtUtils.verifyJwtToken(request.getHeader(Constant.AUTHORIZATION));
//
//        JSONObject jsonObject = fileService.getJsonFile(userId, path, fileName);
//
//        return ResultResponse.success(jsonObject);
//    }

    /**
     * 解压文件
     *
     * @param path     路径
     * @param fileName 文件名
     * @return {@link ResultResponse}<{@link Object}>
     */
    @PostMapping("/unzip")
    public ResultResponse<Object> unzip(HttpServletRequest request, String path, String fileName) throws Exception {
        String userId = JwtUtils.verifyJwtToken(request.getHeader(Constant.AUTHORIZATION));

        fileService.unzip(userId, path, fileName);

        return ResultResponse.success(null);
    }

    /**
     * 获取文件链接
     *
     * @param path     路径
     * @param fileName 文件名
     */
    @GetMapping("/getUrl")
    public ResultResponse<String> getUrl(HttpServletRequest request, String path,
                                         @NotNull String fileName) throws Exception {
        String userId = JwtUtils.verifyJwtToken(request.getHeader(Constant.AUTHORIZATION));
        String result = fileService.getUrl(userId, path, fileName);
        return ResultResponse.success(result);
    }

    /**
     * 获取元数据
     *
     * @param path 路径
     */
    @GetMapping("/getMetadata")
    public ResultResponse<JSONObject> getMetadata(HttpServletRequest request, String path) throws Exception {
        String userId = JwtUtils.verifyJwtToken(request.getHeader(Constant.AUTHORIZATION));

        JSONObject result = fileService.getMetadata(userId, path);

        return ResultResponse.success(result);
    }

    /**
     * 读取回收站内容
     *
     * @return {@link ResultResponse}<{@link JSONObject}>
     */
    @GetMapping("/getTrashBin")
    public ResultResponse<JSONObject> getTrashBin(HttpServletRequest request) throws Exception {
        String userId = JwtUtils.verifyJwtToken(request.getHeader(Constant.AUTHORIZATION));
        JSONObject result = fileService.getTrash(userId);
        return ResultResponse.success(result);
    }


//    @PutMapping("/updateMetadata")
//    public ResultResponse<JSONObject> updateMetadata(HttpServletRequest request,String path) throws Exception{
//        String userId = JwtUtils.verifyJwtToken(request.getHeader(Constant.AUTHORIZATION));
//        fileService.updateMetadata(userId,path);
//        return ResultResponse.success(null);
//    }

    /**
     * 获取文件夹链接
     *
     * @param path 路径
     * @return {@link ResultResponse}<{@link JSONObject}>
     */
    @GetMapping("/getDirectoryUrl")
    public ResultResponse<JSONObject> getDirectoryUrl(HttpServletRequest request, String path) throws Exception {
        String userId = JwtUtils.verifyJwtToken(request.getHeader(Constant.AUTHORIZATION));
        JSONObject result = fileService.getDirectoryUrl(userId, path);
        return ResultResponse.success(result);
    }

    /**
     * 从回收站中恢复文件
     *
     * @param objectName 对象名称
     * @return {@link ResultResponse}<{@link Object}>
     * @throws Exception
     */
    @PostMapping("/recoverFile")
    public ResultResponse<Object> recoverFile(HttpServletRequest request, String objectName) throws Exception {
        String userId = JwtUtils.verifyJwtToken(request.getHeader(Constant.AUTHORIZATION));
        fileService.recoverTrash(userId, objectName);
        return ResultResponse.success(null);
    }
}
