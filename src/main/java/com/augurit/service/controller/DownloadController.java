package com.augurit.service.controller;

import com.augurit.service.constant.Constant;
import com.augurit.service.constant.UrlConstant;
import com.augurit.service.service.FileService;
import com.augurit.service.utils.jwt.JwtUtils;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.util.UUID;

/**
 * 下载相关Controller
 *
 * @author huang jiahui
 * @date 2022/1/18 11:50
 */
@RestController
@AllArgsConstructor
@RequestMapping("/file")
public class DownloadController {
    private final FileService fileService;

    /**
     * 下载单文件
     * @param path     路径
     * @param fileName 文件名
     */
    @PostMapping("/download")
    public void download(HttpServletRequest request,
                         HttpServletResponse response,
                         String path, String fileName) throws Exception {
        String userId = JwtUtils.verifyJwtToken(request.getHeader(Constant.AUTHORIZATION));

        OutputStream out = response.getOutputStream();

        fileService.download(out, userId, path, fileName);
    }

    /**
     * 下载所有文件 zip包形式
     *
     * @param path 路径 test/
     */
    @PostMapping("/downloadAll")
    public void downAll(HttpServletRequest request,
                        HttpServletResponse response,
                        String path)
            throws Exception {
        String userId = JwtUtils.verifyJwtToken(request.getHeader(Constant.AUTHORIZATION));

        OutputStream out = response.getOutputStream();

        fileService.downloadDirectory(out, userId, path);
    }

    /**
     * 不用权限下载zip包：可彪大佬用
     * 例子：file/downloadWithoutAuth/1471022845367246849/GLTF模型/广州海珠综合大楼/广东省综合大楼/v0/综合大楼.gltf
     */
    @GetMapping("/downloadWithoutAuth/**")
    public void downloadAllWithoutAuth(HttpServletRequest request,
                                       HttpServletResponse response)
            throws Exception {

        String uri = request.getRequestURI().replace(UrlConstant.FILE_DownloadWithoutAuth,"");

        uri = URLDecoder.decode(uri, "UTF-8");

        String userId = uri.split("/")[0];

        String path = uri.replace(userId.concat("/"),"");

        response.reset();
        response.setHeader("Content-Disposition", "attachment; filename=\""+ UUID.randomUUID() +".zip\"");
        response.setContentType("application/octet-stream; charset=UTF-8");
        OutputStream out = response.getOutputStream();
        fileService.downloadDirectoryWithoutAuth(out, userId, path);
    }

    /**
     * 在开放权限下，根据url下载文件
     *  例子：file/get/1471022845367246849/GLTF模型/广州海珠综合大楼/广东省综合大楼/v0/综合大楼.gltf
     */
    @GetMapping("/get/**")
    public void get(HttpServletRequest request, HttpServletResponse response) throws Exception {
        OutputStream out = response.getOutputStream();

        fileService.get(request, out);
    }
}
