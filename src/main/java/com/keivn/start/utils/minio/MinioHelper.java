package com.keivn.start.utils.minio;

import com.keivn.start.exception.ServiceException;
import io.minio.*;
import io.minio.errors.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;
import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * MinIO 工具类
 *
 * @author huang jiahui
 * @date 2021/11/17 17:24
 */
@Slf4j
@Component
public class MinioHelper {

    /**
     * minio接口地址
     */
    @Value("${minio.url}")
    private String endpoint;

    /**
     * minio用户名
     */
    @Value("${minio.accessKey}")
    private String accessKey;

    /**
     * minio 密码
     */
    @Value("${minio.secretKey}")
    private String secretKey;

    /**
     * minio客户端 单例模式???
     */
    private static MinioClient minioClient;

    @PostConstruct
    private void init() {
        if (minioClient == null) {
            minioClient = MinioClient.builder()
                    .endpoint(endpoint)
                    .credentials(accessKey, secretKey)
                    .build();
        }
    }

    public static boolean createBucket(String bucketName) {
        try {
            log.info("Minio创建桶 {}", bucketName);
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(bucketName).build());

            if (found) {
                log.info("Minio桶 {}已存在", bucketName);
                return false;
            }
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            log.info("Minio创建桶 {}完成", bucketName);

            return true;
        } catch (ServerException e) {
            log.warn("Minio连接错误", e);
        } catch (ErrorResponseException e) {
            log.warn("Minio错误返回", e);
            throw new ServiceException("Minio错误返回", e);
        } catch (Exception e) {
            log.warn("Minio创建桶失败", e);
        }

        return false;
    }

    public static void upload(String bucketName, String path, @NotNull MultipartFile file) {
        try {
            String fileName = file.getOriginalFilename();

            // todo
            if (!StringUtils.hasText(fileName)) {
                log.warn("空文件名");
                return;
            }

            String objectName = fileName;

            if (StringUtils.hasText(path)) {
                objectName = path + fileName;
            }


            log.info("上传文件：{}", fileName);
            InputStream inputStream = file.getInputStream();
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName).object(objectName)
                    .stream(inputStream, inputStream.available(), -1)
                    .build());
            inputStream.close();
            log.info("上传文件：{}完成", fileName);
        } catch (ServerException e) {
            e.printStackTrace();
        } catch (InsufficientDataException e) {
            e.printStackTrace();
        } catch (ErrorResponseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (InvalidResponseException e) {
            e.printStackTrace();
        } catch (XmlParserException e) {
            e.printStackTrace();
        } catch (InternalException e) {
            e.printStackTrace();
        }
    }

    public static void delete(String bucketName, String path, String fileName) {
        // todo
        if (!StringUtils.hasText(fileName)) {
            log.info("文件名错误");
            return;
        }

        String objectName = fileName;

        if (StringUtils.hasText(path)) {
            objectName = path + fileName;
        }

        try {
            minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucketName).object(objectName).build());
        } catch (ServerException e) {
            e.printStackTrace();
        } catch (InsufficientDataException e) {
            e.printStackTrace();
        } catch (ErrorResponseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (InvalidResponseException e) {
            e.printStackTrace();
        } catch (XmlParserException e) {
            e.printStackTrace();
        } catch (InternalException e) {
            e.printStackTrace();
        }


    }
}


