package com.augurit.service.controller;

import com.augurit.service.constant.Constant;
import com.augurit.service.constant.HttpRequestParameter;
import com.augurit.service.service.FileService;
import com.augurit.service.utils.jwt.JwtUtils;
import com.augurit.service.utils.result.ResultResponse;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;

/**
 * 上传相关
 *
 * @author huang jiahui
 * @date 2022/1/18 11:41
 */
@RestController
@AllArgsConstructor
@RequestMapping("/file")
public class UploadController {

    private final FileService fileService;

    /**
     * vue-simple-uploader 分片上传
     *
     * @param path     路径
     * @param fileName 文件名
     * @return {@link ResultResponse}<{@link Object}>
     * @throws Exception
     */
    @PostMapping("/checkBeforeUpload")
    public ResultResponse<Object> checkBeforeUpload(HttpServletRequest request,
                                                    String path, String fileName) throws Exception {
        String userId = JwtUtils.verifyJwtToken(request.getHeader(Constant.AUTHORIZATION));
        Integer count = fileService.checkFirst(request, userId, path, fileName);
        return ResultResponse.success(count);
    }

    /**
     * 合并上传
     *
     * @param path     路径
     * @param fileName 文件名
     */
    @PostMapping("/checkAfterUpload")
    public void checkAfterUpload(HttpServletRequest request, String path, String fileName) throws Exception {
        String userId = JwtUtils.verifyJwtToken(request.getHeader(Constant.AUTHORIZATION));
        String chunks = request.getParameter(HttpRequestParameter.CHUNKS);
        fileService.mergeFile(userId, path, fileName, Integer.parseInt(chunks));
    }

    @PostMapping("/updateMetaAfterUpload")
    public void listAfterUpload(HttpServletRequest request, String path, String...fileName) throws Exception{
        String userId = JwtUtils.verifyJwtToken(request.getHeader(Constant.AUTHORIZATION));
        fileService.updateMetaAfterUpload(userId, path,fileName);
    }



    /**
     * 免登陆上传
     *
     * @param bucketName 桶名
     * @param path       路径
     * @param file       文件
     */
    @PostMapping("/uploadWithoutAuth")
    public ResultResponse<Object> uploadWithoutAuth(String bucketName,
                                                    String path, MultipartFile[] file) throws Exception {

        fileService.uploadWithoutAuth(bucketName, path, file);

        return ResultResponse.success(null);
    }

    /**
     * 上传文件
     *
     * @param path 用户文件夹
     * @param file 单个或多个文件
     */
    @PostMapping("/upload")
    public ResultResponse<Object> upload(String path, @NotNull MultipartFile[] file,
                                         HttpServletRequest request) throws Exception {

        String userId = JwtUtils.verifyJwtToken(request.getHeader(Constant.AUTHORIZATION));

        fileService.upload(request, userId, path, file);

        return ResultResponse.success(null);
    }
}
