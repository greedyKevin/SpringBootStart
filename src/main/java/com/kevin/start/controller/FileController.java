//package com.keivn.start.controller;
//
//import com.keivn.start.result.ResultResponse;
//import com.keivn.start.utils.minio.MinioHelper;
//import lombok.extern.slf4j.Slf4j;
//
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RestController;
//import org.springframework.web.multipart.MultipartFile;
//
//import javax.validation.constraints.NotNull;
//
///**
// * todo
// *
// * @author huang jiahui
// * @date 2021/11/18 13:44
// */
//@RestController
//@Slf4j
//public class FileController {
//
//    @PostMapping("/create-bucket")
//    public ResultResponse createBucket(String bucketName) {
//
//
//        boolean done = MinioHelper.createBucket(bucketName);
//
//        if (!done) {
//            return ResultResponse.fail("此桶名已经存在",null);
//        }
//
//        return ResultResponse.success(null);
//    }
//
//    @PostMapping("/upload")
//    public ResultResponse upload(String bucketName, String path, @NotNull MultipartFile[] file) {
//        MinioHelper.upload(bucketName, path, file);
//        return ResultResponse.success(null);
//    }
//
//    @PostMapping("/delete")
//    public ResultResponse delete(String bucketName, String path, String fileName) {
//
//        try {
//            MinioHelper.delete(bucketName, path, fileName);
//            return ResultResponse.success(null);
//        } catch (Exception e) {
//            log.warn(e.getMessage(), e);
//            return new ResultResponse(500, e.getMessage(), null);
//        }
//    }
//
//    @PostMapping("/download")
//    public ResultResponse download(String bucketName, String path, String fileName, String storageLocation) throws Exception {
//
//        MinioHelper.download(bucketName, path, fileName, storageLocation);
//        return ResultResponse.success(null);
//
//    }
//
//
//}
