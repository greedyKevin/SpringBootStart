package com.augurit.service.utils.minio;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.augurit.service.config.exception.ServiceException;
import com.augurit.service.constant.Constant;
import com.augurit.service.entity.base.BaseMetaData;
import com.augurit.service.utils.common.CommonUtils;
import io.minio.*;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.ServerException;
import io.minio.http.Method;
import io.minio.messages.Item;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipOutputStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

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
     * minio客户端 单例模式
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

    /**
     * 创建桶
     *
     * @param bucketName 桶名
     */
    public static void createBucket(String bucketName) throws Exception {
        try {
            log.debug("Minio创建桶 {}", bucketName);
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(bucketName).build());

            // 如果桶存在
            if (found) {
                log.debug("Minio桶 {}已存在", bucketName);
                return;
            }
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            log.debug("Minio创建桶 {}完成", bucketName);
        } catch (ServerException e) {
            log.warn("Minio连接错误", e);
            throw e;
        } catch (ErrorResponseException e) {
            log.warn("Minio错误返回", e);
            throw e;
        } catch (Exception e) {
            log.warn("Minio创建桶失败", e);
            throw new ServiceException("Minio创建桶失败");
        }
    }

    /**
     * 上传文件
     *
     * @param bucketName 桶名
     * @param path       路径名（文件夹名），不填则在桶下
     * @param fileName   文件名
     */
    public static void uploadInputStream(String bucketName, String path, String fileName, InputStream inputStream) throws Exception {
        try {
            // 文件不存在
            if (!StringUtils.hasText(fileName)) {
                log.warn("空文件名");
                throw new RuntimeException("文件名为空");
            }

            String objectName = fileName;

            // 如果路径存在则拼接
            if (StringUtils.hasText(path)) {
                objectName = path + fileName;
            }

            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName).object(objectName)
                    .stream(inputStream, -1, 10485760)
                    .build());

            log.info("上传文件：{}完成,用户：{}, 路径名：{}", fileName, bucketName, path);
        } catch (ServerException e) {
            log.warn("Minio连接错误", e);
            log.warn("上传文件{}失败,用户：{}, 路径名：{}", fileName, bucketName, path);
            throw e;
        } catch (IOException e) {
            log.warn("IO错误", e);
            log.warn("上传文件{}失败,用户：{}, 路径名：{}", fileName, bucketName, path);
            throw e;
        } catch (Exception e) {
            log.warn("文件上传失败", e);
            log.warn("上传文件{}失败,用户：{}, 路径名：{}", fileName, bucketName, path);
            throw new ServiceException("文件上传失败");
        }
    }


    /**
     * 删除某一个文件
     *
     * @param bucketName 桶名
     * @param path       路径名（文件夹名），不填则在桶下
     * @param fileName   文件名
     */
    public static void delete(String bucketName, String path, String fileName) throws Exception {
        // 文件名不能为空
        if (!StringUtils.hasText(fileName)) {
            log.warn("文件名错误");
            throw new RuntimeException("文件名错误");
        }

        String objectName = fileName;

        // 如果路径存在则拼接
        if (StringUtils.hasText(path)) {
            objectName = path + fileName;
        }

        try {
            minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucketName).object(objectName).build());
            log.info("删除文件：{}完成,用户：{}, 路径名：{}", fileName, bucketName, path);
        } catch (ServerException e) {
            log.warn("Minio连接错误", e);
            log.warn("删除文件{}失败,用户：{}, 路径名：{}", fileName, bucketName, path);
            throw e;
        } catch (ErrorResponseException e) {
            log.warn("错误的返回", e);
            log.warn("删除文件{}失败,用户：{}, 路径名：{}", fileName, bucketName, path);
            throw e;
        } catch (IOException e) {
            log.warn("文件错误", e);
            log.warn("删除文件{}失败,用户：{}, 路径名：{}", fileName, bucketName, path);
            throw e;
        } catch (Exception e) {
            log.warn("文件删除失败", e);
            log.warn("删除文件{}失败,用户：{}, 路径名：{}", fileName, bucketName, path);
            throw new ServiceException("文件删除失败");
        }
    }

    /**
     * 下载单文件
     *
     * @param bucketName 桶名
     * @param path       路径
     * @param fileName   文件名
     */
    public static void download(OutputStream out, String bucketName, String path, String fileName) throws Exception {
        // 文件名不能为空
        if (!StringUtils.hasText(fileName)) {
            log.warn("文件名错误");
            throw new Exception("文件名错误");
        }

        String objectName = fileName;

        // 如果路径存在则拼接
        if (StringUtils.hasText(path)) {
            objectName = path + fileName;
        }

        try {
            @Cleanup InputStream inputStream = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build());

            byte[] bytes = new byte[1024 * 4];

            int len;

            while ((len = inputStream.read(bytes)) != -1) {
                out.write(bytes, 0, len);
            }

            out.flush();

        } catch (ServerException e) {
            log.warn("Minio连接错误", e);
            log.warn("下载桶:{}, 文件名:{}失败", bucketName, objectName);
            throw e;
        } catch (ErrorResponseException e) {
            log.warn("错误的返回", e);
            log.warn("下载桶:{}, 文件名:{}失败", bucketName, objectName);
            throw e;
        } catch (IOException e) {
            log.warn("文件IO错误", e);
            log.warn("下载桶:{}, 文件名:{}失败", bucketName, objectName);
            throw e;
        } catch (Exception e) {
            log.warn("文件下载失败", e);
            log.warn("下载桶:{}, 文件名:{}失败", bucketName, objectName);
            throw new ServiceException("文件下载失败");
        }
    }

    public static void downloadAllWithoutAuth(OutputStream out, String bucketName, String path) throws Exception {
        ZipOutputStream zip = new ZipOutputStream(new BufferedOutputStream(out));

        findAllWithoutAuth(bucketName, path, zip, path);

        zip.finish();

        zip.close();

    }

    private static void findAllWithoutAuth(String bucketName, String path, ZipOutputStream zip, String originalPath) throws Exception {
        try {
            Iterable<Result<Item>> results = minioClient.listObjects(ListObjectsArgs.builder()
                    .bucket(bucketName).prefix(path).build());

            JSONObject metadata = getJsonFile(bucketName, path, Constant.METADATA);

            assert metadata != null;
            Map<String, JSONObject> objects = JSON.parseObject(metadata.getString("objects"), Map.class);


            for (Result<Item> result : results) {
                Item item = result.get();
                String objectName = item.objectName();

                // 如果是文件夹
                if (item.isDir()) {
                    JSONObject dirMetadata = getJsonFile(bucketName, objectName, Constant.METADATA);

                    // 如果被删除 跳过
                    assert dirMetadata != null;
                    if ((boolean) dirMetadata.get("isDeleted")) {
                        continue;
                    }
                    findAllWithoutAuth(bucketName, objectName, zip, originalPath);
                    // 如果不是文件夹标记
                } else if (!objectName.endsWith(Constant.METADATA)) {
                    String fileName = objectName.replace(path, "");
                    BaseMetaData bmd = JSON.parseObject(objects.get(fileName).toJSONString(), BaseMetaData.class);

                    if (bmd.isDeleted()) {
                        continue;
                    }

                    @Cleanup InputStream inputStream = minioClient.getObject(GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build());

                    zip.putNextEntry(new ZipEntry(objectName.replace(originalPath, "")));

                    byte[] bytes = new byte[1024 * 4];

                    int len;

                    while ((len = inputStream.read(bytes)) != -1) {
                        zip.write(bytes, 0, len);
                    }
                }
            }
        } catch (ServerException e) {
            log.warn("Minio连接错误", e);
            log.warn("查找失败,用户{}, 路径：{}", path, bucketName);
            throw e;
        } catch (InsufficientDataException e) {
            log.warn("查找失败,用户{}, 路径：{}", path, bucketName);
            throw e;
        } catch (ErrorResponseException e) {
            log.warn("错误的返回", e);
            log.warn("查找失败,用户{}, 路径：{}", path, bucketName);
            throw e;
        } catch (IOException e) {
            log.warn("文件IO错误", e);
            log.warn("查找失败,用户{}, 路径：{}", path, bucketName);
            throw e;
        } catch (Exception e) {
            log.warn("查找失败", e);
            log.warn("查找失败,用户{}, 路径：{}", path, bucketName);
            throw new ServiceException("批量下载失败");
        }
    }

    public static void downloadAll(OutputStream out, String bucketName, String path) throws Exception {
        try {
            log.info("批量下载,用户{},路径：{}", path, bucketName);

            ZipOutputStream zip = new ZipOutputStream(new BufferedOutputStream(out));
            // 用户选择的目录
            String parentPath =path;

            String[] strArray = path.split("/");
            // 大于一个 很多根目录
            if (strArray.length > 1) {
                parentPath = String.join("/", Arrays.copyOfRange(strArray,0,strArray.length-1))
                        .concat("/");
            }

            findAll(bucketName, path, zip,parentPath);

            zip.finish();

            zip.close();

            log.info("批量下载完成,用户{}, 路径：{}", path, bucketName);
        } catch (ServerException e) {
            log.warn("Minio连接错误", e);
            log.warn("批量下载,用户{}, 路径：{}", path, bucketName);
            throw e;
        } catch (InsufficientDataException e) {
            log.warn("批量下载,用户{}, 路径：{}", path, bucketName);
            throw e;
        } catch (ErrorResponseException e) {
            log.warn("错误的返回", e);
            log.warn("批量下载,用户{}, 路径：{}", path, bucketName);
            throw e;
        } catch (IOException e) {
            log.warn("文件IO错误", e);
            log.warn("批量下载,用户{}, 路径：{}", path, bucketName);
            throw e;
        } catch (Exception e) {
            log.warn("批量下载失败", e);
            log.warn("批量下载,用户{}, 路径：{}", path, bucketName);
            throw new ServiceException("批量下载失败");
        }
    }

    public static Iterable<Result<Item>> listObjects(String bucketName, String objectName) {
        return minioClient.listObjects(ListObjectsArgs.builder()
                .bucket(bucketName).prefix(objectName).build());
    }


    /**
     * 压缩辅助函数
     *
     * @param bucketName 桶名
     * @param path       路径
     * @param zip        压缩文件输出流
     * @throws Exception 报错信息
     */
    @SuppressWarnings("unchecked")
    private static void findAll(String bucketName, String path, ZipOutputStream zip, String parentPath) throws Exception {
        try {
            Iterable<Result<Item>> results = minioClient.listObjects(ListObjectsArgs.builder()
                    .bucket(bucketName).prefix(path).build());

            JSONObject metadata = getJsonFile(bucketName, path, Constant.METADATA);

            assert metadata != null;
            Map<String, JSONObject> objects = JSON.parseObject(metadata.getString("objects"), Map.class);


            for (Result<Item> result : results) {
                Item item = result.get();
                String objectName = item.objectName();

                // 如果是文件夹
                if (item.isDir()) {
                    JSONObject dirMetadata = getJsonFile(bucketName, objectName, Constant.METADATA);

                    // 如果被删除 跳过
                    assert dirMetadata != null;
                    if ((boolean) dirMetadata.get("isDeleted")) {
                        continue;
                    }
                    findAll(bucketName, objectName, zip, parentPath);
                    // 如果不是文件夹标记
                } else if (!objectName.endsWith(Constant.METADATA)) {
                    String fileName = objectName.replace(path, "");
                    BaseMetaData bmd = JSON.parseObject(objects.get(fileName).toJSONString(), BaseMetaData.class);

                    if (bmd.isDeleted()) {
                        continue;
                    }

                    @Cleanup InputStream inputStream = minioClient.getObject(GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build());

                    zip.putNextEntry(new ZipEntry(objectName.replace(parentPath,"")));

                    byte[] bytes = new byte[1024 * 4];

                    int len;

                    while ((len = inputStream.read(bytes)) != -1) {
                        zip.write(bytes, 0, len);
                    }
                }
            }
        } catch (ServerException e) {
            log.warn("Minio连接错误", e);
            log.warn("查找失败,用户{}, 路径：{}", path, bucketName);
            throw e;
        } catch (InsufficientDataException e) {
            log.warn("查找失败,用户{}, 路径：{}", path, bucketName);
            throw e;
        } catch (ErrorResponseException e) {
            log.warn("错误的返回", e);
            log.warn("查找失败,用户{}, 路径：{}", path, bucketName);
            throw e;
        } catch (IOException e) {
            log.warn("文件IO错误", e);
            log.warn("查找失败,用户{}, 路径：{}", path, bucketName);
            throw e;
        } catch (Exception e) {
            log.warn("查找失败", e);
            log.warn("查找失败,用户{}, 路径：{}", path, bucketName);
            throw new ServiceException("批量下载失败");
        }
    }

    /**
     * 删除所有文件
     *
     * @param bucketName 桶名
     * @param path       路径
     */
    public static void deleteAll(String bucketName, String path) throws Exception {
        try {
            log.info("批量删除，桶名{},路径{}", bucketName, path);

//            copyObject(bucketName,path,"delete.tmp","test","delete.tmp");

            Iterable<Result<Item>> results = minioClient.listObjects(ListObjectsArgs.builder()
                    .bucket(bucketName).prefix(path).build());

            for (Result<Item> result : results) {
                Item item = result.get();

                String objectName = item.objectName();
                // 如果是文件夹
                if (item.isDir()) {
                    deleteAll(bucketName, objectName);
                } else {
                    String fileName = objectName;

                    if (StringUtils.hasText(path)) {
                        fileName = objectName.replace(path, "");
                    }
                    delete(bucketName, path, fileName);
                }
            }
            log.info("批量删除完成，路径{},桶名{}", bucketName, path);
        } catch (ServerException e) {
            log.warn("Minio连接错误", e);
            log.warn("批量删除,路径：{},桶名{}", path, bucketName);
            throw e;
        } catch (InsufficientDataException e) {
            log.warn("批量删除,路径：{},桶名{}", path, bucketName);
            throw e;
        } catch (ErrorResponseException e) {
            log.warn("错误的返回", e);
            log.warn("批量删除,路径：{},桶名{}", path, bucketName);
            throw e;
        } catch (IOException e) {
            log.warn("文件IO错误", e);
            log.warn("批量删除,路径：{},桶名{}", path, bucketName);
            throw e;
        } catch (Exception e) {
            log.warn("批量删除失败", e);
            log.warn("批量删除,路径：{},桶名{}", path, bucketName);
            throw new ServiceException("批量删除失败");
        }
    }

    public static void copyObject(String bucketName, String path, String fileName,
                                  String srcBucket, String srcObject) throws Exception {
        try {

            log.info("复制到 桶名：{},路径：{},文件名：{};来源 桶名：{}，objectName: {}", bucketName, path, fileName, srcBucket, srcObject);
            String objectName = fileName;

            // 如果路径存在则拼接
            if (StringUtils.hasText(path)) {
                objectName = path + fileName;
            }

            minioClient.copyObject(CopyObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .source(CopySource.builder().bucket(srcBucket).object(srcObject).build())
                    .build());
            log.info("复制到 桶名：{},路径：{},文件名{};来源 桶名：{}，objectName: {} 完成", bucketName, path, fileName, srcBucket, srcObject);
        } catch (ServerException e) {
            log.warn("Minio连接错误", e);
        } catch (ErrorResponseException e) {
            log.warn("错误的返回", e);
            throw e;
        } catch (IOException e) {
            log.warn("文件IO错误", e);
            throw e;
        } catch (Exception e) {
            log.warn("复制失败", e);
            throw new ServiceException("复制失败");
        }
    }

    /**
     * 获取当前路径下的所有文件
     *
     * @param bucketName 桶名
     * @param path       路径
     * @return {@link Iterable}<{@link Result}<{@link Item}>>
     */
    public static Iterable<Result<Item>> getAll(String bucketName, String path) {

        return minioClient.listObjects(ListObjectsArgs.builder().bucket(bucketName).
                prefix(path).build());
    }

    /**
     * 获取json类文件
     *
     * @param bucketName 桶名
     * @param path       路径
     * @return {@link JSONObject}
     */
    public static JSONObject getJsonFile(String bucketName, String path, String fileName) throws Exception {

        String objectName = path + fileName;
        AtomicInteger count = new AtomicInteger(0);
        try {

            Iterable<Result<Item>> results = listObjects(bucketName, objectName);

            for (Result<Item> ignored : results) {
                count.incrementAndGet();
            }

            if (count.get() != 1) {
                return null;
            }

            @Cleanup InputStream inputStream = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build());

            return CommonUtils.getJson(inputStream);

        } catch (ServerException e) {
            log.warn("Minio连接错误", e);
            log.warn("获取JSON文件: 用户:{}, 文件名:{}失败", bucketName, objectName);
            throw e;
        } catch (ErrorResponseException e) {
            log.warn("错误的返回", e);
            log.warn("获取JSON文件: 用户:{}, 文件名:{}失败", bucketName, objectName);
            throw e;
        } catch (IOException e) {
            log.warn("文件IO错误", e);
            log.warn("获取JSON文件: 用户:{}, 文件名:{}失败", bucketName, objectName);
            throw e;
        } catch (Exception e) {
            log.warn("获取JSON文件失败", e);
            log.warn("获取JSON文件: 用户:{}, 文件名:{}失败", bucketName, objectName);
            throw new ServiceException("文件下载失败");
        }
    }

    public static InputStream getObject(String bucketName, String path, String fileName) throws Exception {
        String objectName = path + fileName;


        return minioClient.getObject(GetObjectArgs.builder()
                .bucket(bucketName)
                .object(objectName)
                .build());
    }

    /**
     * 创建文件夹
     *
     * @param bucketName 桶名
     * @param objectName 文件夹名
     */
    public static void createDirectory(String bucketName, String objectName) throws Exception {
        try {
            log.info("用户：{}，创建文件夹：{}", bucketName, objectName);
            minioClient.putObject(PutObjectArgs.builder().bucket(bucketName).object(objectName).stream(
                    new ByteArrayInputStream(new byte[]{}), 0, -1)
                    .build());

            JSONObject json = new JSONObject();
            json.put("objects", new HashMap<String, Object>());

            String name = objectName.substring(0, objectName.length() - 1);
            name = name.replaceAll(".*/", "");
            json.put("name", name);
            json.put("path", objectName);
            json.put("isDeleted", false);

            // 上传标记文件
            @Cleanup InputStream inputStream = new ByteArrayInputStream(json.toString().getBytes());

            uploadInputStream(bucketName, "", objectName + Constant.METADATA, inputStream);

            log.info("用户：{}，创建文件夹：{}完成。", bucketName, objectName);
        } catch (ServerException e) {
            log.warn("Minio连接错误", e);
            log.warn("用户:{}, 创建文件夹名:{}失败", bucketName, objectName);
            throw e;
        } catch (ErrorResponseException e) {
            log.warn("错误的返回", e);
            log.warn("用户:{}, 创建文件夹名:{}失败", bucketName, objectName);
            throw e;
        } catch (IOException e) {
            log.warn("文件IO错误", e);
            log.warn("用户:{}, 创建文件夹名:{}失败", bucketName, objectName);
            throw e;
        } catch (Exception e) {
            log.warn("创建文件夹失败", e);
            log.warn("用户:{}, 文件名:{}失败", bucketName, objectName);
            throw new ServiceException("创建文件夹失败");
        }
    }

    public static String getUrl(String bucketName, String path, String fileName, int n) throws Exception {
        try {
            String objectName = path + fileName;
            return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(bucketName)
                    .expiry(n, TimeUnit.MINUTES)
                    .object(objectName).build());
        } catch (Exception e) {
            log.warn("获取url失败", e);
            throw e;
        }
    }

    public static boolean hasObject(String bucketName, String objectName) {
        Iterable<Result<Item>> results = MinioHelper.listObjects(bucketName, objectName);

        AtomicInteger flag = new AtomicInteger(0);

        for (Result<Item> ignored : results) {
            flag.incrementAndGet();
        }

        return flag.get() != 0;
    }

    /**
     * 物理删除文件
     */
    @SuppressWarnings("all")
    public static void physicalDelete(String bucketName, String path, String fileName) throws Exception {
        // 更新metadata文件
        JSONObject metadata = MinioHelper.getJsonFile(bucketName, path, Constant.METADATA);
        Map<String, JSONObject> objects = JSON.parseObject(metadata.getString("objects"), Map.class);

        objects.remove(fileName);
        metadata.put(Constant.OBJECTS_KEY, objects);
        @Cleanup InputStream in = new ByteArrayInputStream(metadata.toJSONString().getBytes());
        MinioHelper.uploadInputStream(bucketName, path, Constant.METADATA, in);

        MinioHelper.delete(bucketName, path, fileName);
    }

    /**
     * 物理删除文件夹
     */
    @SuppressWarnings("all")
    public static void physicalDeleteDirectory(String bucketName, String path, JSONObject json) throws Exception {
        json.remove(path);
        @Cleanup InputStream in = new ByteArrayInputStream(json.toJSONString().getBytes());
        MinioHelper.uploadInputStream(bucketName, "", Constant.TRASH_BIN, in);

        MinioHelper.deleteAll(bucketName, path);
    }

}


