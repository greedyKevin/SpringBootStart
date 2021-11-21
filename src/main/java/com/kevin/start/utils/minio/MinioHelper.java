//package com.keivn.start.utils.minio;
//
//import com.keivn.start.exception.ServiceException;
//import io.minio.*;
//import io.minio.errors.*;
//import lombok.Cleanup;
//import lombok.extern.slf4j.Slf4j;
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Component;
//import org.springframework.util.StringUtils;
//import org.springframework.web.multipart.MultipartFile;
//
//import javax.annotation.PostConstruct;
//import javax.validation.constraints.NotNull;
//import java.io.*;
//import java.security.InvalidKeyException;
//import java.security.NoSuchAlgorithmException;
//
///**
// * MinIO 工具类
// *
// * @author huang jiahui
// * @date 2021/11/17 17:24
// */
//@Slf4j
//@Component
//public class MinioHelper {
//
//    /**
//     * minio接口地址
//     */
//    @Value("${minio.url}")
//    private String endpoint;
//
//    /**
//     * minio用户名
//     */
//    @Value("${minio.accessKey}")
//    private String accessKey;
//
//    /**
//     * minio 密码
//     */
//    @Value("${minio.secretKey}")
//    private String secretKey;
//
//    /**
//     * minio客户端 单例模式
//     */
//    private static MinioClient minioClient;
//
//    @PostConstruct
//    private void init() {
//        if (minioClient == null) {
//            minioClient = MinioClient.builder()
//                    .endpoint(endpoint)
//                    .credentials(accessKey, secretKey)
//                    .build();
//        }
//    }
//
//    /**
//     * 创建桶
//     * @param bucketName
//     * @return boolean
//     */
//    public static boolean createBucket(String bucketName) {
//        try {
//            log.info("Minio创建桶 {}", bucketName);
//            boolean found = minioClient.bucketExists(BucketExistsArgs.builder()
//                    .bucket(bucketName).build());
//
//            // 如果桶存在
//            if (found) {
//                log.info("Minio桶 {}已存在", bucketName);
//                return false;
//            }
//            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
//            log.info("Minio创建桶 {}完成", bucketName);
//            return true;
//        } catch (ServerException e) {
//            log.warn("Minio连接错误", e);
//        } catch (ErrorResponseException e) {
//            log.warn("Minio错误返回", e);
//        } catch (Exception e) {
//            log.warn("Minio创建桶失败", e);
//        }
//
//        return false;
//    }
//
//    /**
//     * 上传文件
//     * @param bucketName 桶名
//     * @param path  路径名（文件夹名），不填则在桶下
//     * @param files 文件名（一个或多个）
//     */
//    public static void upload(String bucketName, String path, @NotNull MultipartFile[] files) {
//        for (MultipartFile file : files) {
//            String fileName = file.getOriginalFilename();
//
//            try {
//                // 文件不存在
//                if (!StringUtils.hasText(fileName)) {
//                    log.warn("空文件名");
//                    throw new RuntimeException("文件名为空");
//                }
//
//                String objectName = fileName;
//
//                // 如果路径存在则拼接
//                if (StringUtils.hasText(path)) {
//                    objectName = path + fileName;
//                }
//
//                log.info("上传文件：{}", fileName);
//                InputStream inputStream = file.getInputStream();
//                minioClient.putObject(PutObjectArgs.builder()
//                        .bucket(bucketName).object(objectName)
//                        .stream(inputStream, inputStream.available(), -1)
//                        .build());
//
//                log.info("上传文件：{}完成", fileName);
//            } catch (ServerException e) {
//                log.warn("Minio连接错误", e);
//                log.warn("上传文件{}失败",fileName);
//            } catch (IOException e) {
//                log.warn("IO错误", e);
//                log.warn("上传文件{}失败",fileName);
//            } catch (Exception e) {
//                log.warn("文件上传失败", e);
//                log.warn("上传文件{}失败",fileName);
//            }
//        }
//    }
//
//
//    /**
//     * 删除某一个文件
//     * @param bucketName 桶名
//     * @param path  路径名（文件夹名），不填则在桶下
//     * @param fileName 文件名
//     */
//    public static void delete(String bucketName, String path, String fileName) {
//        // 文件名不能为空
//        if (!StringUtils.hasText(fileName)) {
//            log.warn("文件名错误");
//            throw new RuntimeException("文件名错误");
//        }
//
//        String objectName = fileName;
//
//        // 如果路径存在则拼接
//        if (StringUtils.hasText(path)) {
//            objectName = path + fileName;
//        }
//
//        try {
//            minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucketName).object(objectName).build());
//        } catch (ServerException e) {
//            log.warn("Minio连接错误", e);
//        } catch (ErrorResponseException e) {
//            log.warn("错误的返回", e);
//        } catch (IOException e) {
//            log.warn("文件错误", e);
//        } catch (Exception e) {
//            log.warn("文件删除失败", e);
//        }
//    }
//
//    /**
//     * 下载单文件
//     * @param bucketName 桶名
//     * @param path  路径
//     * @param fileName 文件名
//     * @param storageLocation 本地路径（C:\Users\Kevin\Desktop\help\1.png） 最后为文件名
//     *
//     */
//    public static void download(String bucketName, String path, String fileName, String storageLocation) throws Exception {
//        // 文件名不能为空
//        if (!StringUtils.hasText(fileName)) {
//            log.warn("文件名错误");
//            throw new RuntimeException("文件名错误");
//        }
//
//        String objectName = fileName;
//
//        // 如果路径存在则拼接
//        if (StringUtils.hasText(path)) {
//            objectName = path + fileName;
//        }
//
//        try{
//            @Cleanup InputStream inputStream = minioClient.getObject(GetObjectArgs.builder()
//                    .bucket(bucketName)
//                    .object(objectName)
//                    .build());
//            @Cleanup FileOutputStream fileOutputStream = new FileOutputStream(storageLocation);
//
//            int len;
//
//            while((len = inputStream.read())!=-1){
//                fileOutputStream.write(len);
//            }
//        } catch (ServerException e) {
//            log.warn("Minio连接错误", e);
//            log.warn("下载桶:{}, 文件名:{}失败",bucketName,objectName);
//            throw e;
//        } catch (ErrorResponseException e) {
//            log.warn("错误的返回", e);
//            log.warn("下载桶:{}, 文件名:{}失败",bucketName,objectName);
//            throw e;
//        } catch (IOException e) {
//            log.warn("文件IO错误", e);
//            log.warn("下载桶:{}, 文件名:{}失败",bucketName,objectName);
//            throw e;
//        } catch (Exception e) {
//            log.warn("文件下载失败", e);
//            log.warn("下载桶:{}, 文件名:{}失败",bucketName,objectName);
//            throw new ServiceException("文件下载失败");
//        }
//    }
//}
//
//
