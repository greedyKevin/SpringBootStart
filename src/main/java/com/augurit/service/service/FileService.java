package com.augurit.service.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.augurit.service.config.exception.ServiceException;
import com.augurit.service.constant.Constant;
import com.augurit.service.constant.HttpRequestParameter;
import com.augurit.service.constant.RedisKeyConstant;
import com.augurit.service.constant.UrlConstant;
import com.augurit.service.entity.FileInformation;
import com.augurit.service.entity.Trash;
import com.augurit.service.entity.User;
import com.augurit.service.entity.base.BaseMetaData;
import com.augurit.service.utils.common.CommonUtils;
import com.augurit.service.utils.minio.MinioHelper;
import com.augurit.service.utils.redis.RedisUtils;
import io.minio.Result;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.ServerException;
import io.minio.messages.Item;
import lombok.Cleanup;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import java.io.*;
import java.math.BigInteger;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

/**
 * 文件相关service
 *
 * @author huang jiahui
 * @date 2021/11/22 11:11
 */
@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Slf4j
public class FileService {

    /**
     * redis工具类
     */
    private final RedisUtils redisUtils;

    @Value("${host}")
    private String host;
    @Value("${filePath}")
    private String filePath;

    /**
     * 根据创建者id获取所有数据
     *
     * @param userId 创建者id
     * @return List<JSONObject>
     */
    @SuppressWarnings("all")
    public List<JSONObject> getAll(String userId, String path) throws Exception {
        try {
            String key = RedisKeyConstant.USER.concat(userId + "/" + path);

            MinioHelper.createBucket(userId);

            if (!StringUtils.hasText(path)) {
                path = "";
            }
            Iterable<Result<Item>> results = MinioHelper.getAll(userId, path);

            JSONObject pathMetadata = null;
            Map<String, JSONObject> map = null;

            if (!"".equals(path)) {
                // 存缓存
                List<JSONObject> resultList = (List<JSONObject>) redisUtils.get(key);
                if (resultList != null) {
                    return resultList;
                }
                // 获取路径元数据
                pathMetadata = MinioHelper.getJsonFile(userId, path, Constant.METADATA);
                map = JSON.parseObject(pathMetadata.getString("objects"), Map.class);
            }

            List<JSONObject> list = new LinkedList<>();

            // 遍历所有元素

            for (Result<Item> result : results) {

                Item item = result.get();

                String name = item.objectName();

                JSONObject json = new JSONObject();
                // 如果是目录
                if (item.isDir()) {

                    JSONObject dirMetadata = getJsonFile(userId, name, Constant.METADATA);

                    // 如果标记为删除
                    if (dirMetadata != null && dirMetadata.size() != 0 && (boolean) dirMetadata.get("isDeleted")) {
                        continue;
                    }

                    // 第一级目录
                    if (name.split("/").length == 1) {
                        json.put("isBucket", true);
                    } else {
                        // 设置为文件夹
                        json.put("isDirectory", true);
                    }
                    // 花里胡哨的转化
                    // path： aaa/bbb/ccc.txt  name: ccc.txt
                    name = name.substring(0, name.lastIndexOf("/"));
                    name = name.replaceAll(".*/", "");
                    json.put("name", name.replaceAll("/", ""));
                    json.put("path", path + name + "/");

                } else if (!name.endsWith(Constant.METADATA)
                        && !name.endsWith(Constant.TRASH_BIN)
                        && !name.endsWith(Constant.TMP_FILE_FLAG)) {
                    // 其他文件
                    String fileName = name.replace(path, "");
                    // 读取metadata.json
                    if (pathMetadata != null) {
                        // 读取json里的key（”objects“）
                        // 将objects + filename 转换为 BaseMetaData
                        BaseMetaData bmd = JSON.parseObject(map.get(fileName).toJSONString(), BaseMetaData.class);
                        if (bmd.isDeleted()) {
                            continue;
                        }
                    }

                    json.put("isDirectory", false);
                    json.put("name", fileName);
                    json.put("path", name);
                    json.put("size", item.size());

                    Date date = Date.from(item.lastModified().withZoneSameInstant(ZoneId.systemDefault()).toInstant());
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String str = sdf.format(date);

                    json.put("updateTime", str);
                }

                if (json.isEmpty()) {
                    continue;
                }
                list.add(json);
            }
            // 写入缓存
            if (list.size() > 50) {
                redisUtils.set(key, list, 5 * 60);
            }

            return list;
        } catch (Exception e) {
            log.warn("获取文件失败", e);
            throw e;
        }
    }

    /**
     * 合并分片
     *
     * @param bucketName 桶名
     * @param path       路径
     * @param fileName   文件名
     * @param chunks     总数
     */
    @SuppressWarnings("all")
    public void mergeFile(String bucketName, String path, String fileName, Integer chunks) throws Exception {

        try {
            User user = (User) redisUtils.get(RedisKeyConstant.USER.concat(bucketName));


            String realPath = filePath.concat(bucketName).concat("/").concat(path);

            File file = new File(realPath, fileName);
            @Cleanup BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(file));

            List<File> files = this.listPartsFile(realPath, fileName);
            int size = files.size();

            for (int i = 1; i <= chunks; i++) {
                File tmpFile = new File(realPath, (i + "_").concat(fileName.concat(Constant.TMP_FILE_FLAG)));
                byte[] bytes = FileUtils.readFileToByteArray(tmpFile);

                os.write(bytes);
                os.flush();
                tmpFile.delete();
            }
            os.flush();
            os.close();

            File tmp = new File(realPath, fileName.concat(Constant.TMP_FILE_FLAG));
            if (tmp.exists()) {
                tmp.delete();
            }

            // 清除缓存
            String key = bucketName.concat("/").concat(path);
            redisUtils.del(key);

            // 清理垃圾桶
            this.cleanTrashBin(bucketName, path, fileName);


            JSONObject metaJson = MinioHelper.getJsonFile(bucketName, path, Constant.METADATA);
            Map<String, JSONObject> objects = JSON.parseObject(metaJson.getString("objects"), Map.class);

            // 元数据
            BaseMetaData metaData = BaseMetaData.builder()
                    .creatorId(bucketName)
                    .date(LocalDateTime.now())
                    .creator(user.getName())
                    .title(fileName)
                    .build();

            objects.put(metaData.getTitle(), (JSONObject) JSONObject.toJSON(metaData));
            metaJson.put("objects", objects);

            @Cleanup InputStream inputStream = new ByteArrayInputStream(metaJson.toJSONString().getBytes());
            // 上传元数据
            MinioHelper.uploadInputStream(bucketName, path, Constant.METADATA, inputStream);
        } catch (Exception e) {
            log.warn("合并文件失败", e);
            throw e;
        }
    }

    private void cleanTrashBin(String bucketName, String path, String fileName) throws Exception {
        // 检查垃圾桶
        if (MinioHelper.hasObject(bucketName, Constant.TRASH_BIN)) {
            JSONObject trashJSON = getTrash(bucketName);
            trashJSON.remove(path.concat(fileName));
            updateJsonFile(trashJSON, bucketName, "", Constant.TRASH_BIN);
        }
    }

    /**
     * 从文件读信息
     */
    private FileInformation readFileInformation(String realPath, String fileName) throws IOException {
        File file = new File(realPath, fileName.concat(Constant.TMP_FILE_FLAG));
        if (!file.exists()) {
            return null;
        }

        @Cleanup InputStream in = new FileInputStream(file);
        return JSON.toJavaObject(CommonUtils.getJson(in), FileInformation.class);
    }

    /**
     * 删除分片文件
     */
    private void deletePartsFile(String path, String fileName) {
        List<File> list = listPartsFile(path, fileName);

        if (list == null) {
            return;
        }

        for (File file : list) {
            file.delete();
        }
    }

    /**
     * 获取分片文件
     */
    private List<File> listPartsFile(String path, String fileName) {
        File file = new File(path);

        if (!file.exists()) {
            return null;
        }

        File[] files = file.listFiles();
        assert files != null;

        String tmp = fileName.concat(Constant.TMP_FILE_FLAG);
        String regex = ".*_".concat(tmp.replaceAll("\\.", "\\\\."));

        List<File> result = new ArrayList<>();

        for (File f : files) {
            String fName = f.getName();
            if (fName.matches(regex) && !f.isDirectory()) {
                result.add(f);
            }
        }
        if (CollectionUtils.isEmpty(result)) {
            return null;
        }

        return result;
    }

    /**
     * 上传时先进行校验
     */
    public Integer checkFirst(HttpServletRequest request, String bucketName,
                              String path, String fileName) throws Exception {

        String realPath = filePath.concat(bucketName).concat("/").concat(path);

        String tmpName = fileName.concat(Constant.TMP_FILE_FLAG);

        //读文件
        FileInformation fi = readFileInformation(realPath, fileName);

        BigInteger size = Optional.ofNullable(request.getParameter("size"))
                .map(BigInteger::new)
                .orElse(null);
        Integer partSize = Optional.ofNullable(request.getParameter("partSize"))
                .map(Integer::parseInt)
                .orElse(null);
        String md5 = Optional.ofNullable(request.getParameter("md5")).orElse(null);
        // 文件不存在创建 or 验证不通过 -->全部重新上传
        if (fi == null || !fi.isMatch(size, partSize, md5)) {
            //删除分片文件
            this.deletePartsFile(realPath, fileName);

            fi = new FileInformation(size, partSize, md5);

            @Cleanup BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(realPath.concat(tmpName)));
            byte[] bytes = JSON.toJSONString(fi).getBytes();

            os.write(bytes);
            os.flush();
            os.close();

            return null;
        }

        List<File> partList = listPartsFile(realPath, fileName);
        if (partList != null) {
            return partList.size() + 1;
        }

        return null;
    }

    public void updateMetaAfterUpload(String bucketName, String path, String... fileName) throws Exception {
        JSONObject metadata = getMetadata(bucketName, path);
        User user = (User) redisUtils.get(RedisKeyConstant.USER.concat(bucketName));

        Map<String, JSONObject> objects = JSON.parseObject(metadata.getString("objects"), Map.class);

        for (String fn : fileName) {
            BaseMetaData metaData = BaseMetaData.builder()
                    .creatorId(bucketName)
                    .date(LocalDateTime.now())
                    .creator(user.getName())
                    .title(fn)
                    .build();

            objects.put(metaData.getTitle(),(JSONObject) JSONObject.toJSON(metaData));
        }

        metadata.put("objects",objects);

        // 清除缓存
        String key = bucketName.concat("/").concat(path);
        redisUtils.del(key);

        @Cleanup InputStream inputStream = new ByteArrayInputStream(metadata.toJSONString().getBytes());
        // 上传元数据
        MinioHelper.uploadInputStream(bucketName, path, Constant.METADATA, inputStream);
    }


    /**
     * 免登录上传
     */
    public void uploadWithoutAuth(String bucketName, String path, @NotNull MultipartFile[] files) throws Exception {
        try {
            String[] pathArray = path.split("/");
            int len = pathArray.length;
            String tmp = "";
            for (int i = 0; i < len; i++) {
                tmp = tmp.concat(pathArray[i]).concat("/");
                // 如果文件夹不存在
                if (!MinioHelper.hasObject(bucketName, tmp)) {
                    this.createDirectory(bucketName, tmp);
                }
            }

            JSONObject metaJson = MinioHelper.getJsonFile(bucketName, path, Constant.METADATA);
            Map<String, JSONObject> objects = JSON.parseObject(metaJson.getString("objects"), Map.class);

            for (MultipartFile file : files) {
                String fileName = file.getOriginalFilename();
                @Cleanup InputStream inputStream = file.getInputStream();
                // 清除缓存
                String key = bucketName.concat("/").concat(path);
                redisUtils.del(key);

                // 检查垃圾桶
                this.cleanTrashBin(bucketName, path, fileName);

                // 清理垃圾桶文件
                this.cleanTrashBin(bucketName, path, fileName);

                // 否则直接上传
                log.info("上传文件：{},用户：{}, 路径名：{}", fileName, bucketName, path);
                MinioHelper.uploadInputStream(bucketName, path, fileName, inputStream);

                BaseMetaData metaData = BaseMetaData.builder()
                        .creatorId("匿名用户：")
                        .date(LocalDateTime.now())
                        .creator("非授权上传")
                        .title(fileName)
                        .build();
                objects.put(metaData.getTitle(), (JSONObject) JSONObject.toJSON(metaData));

                log.info("上传文件：{}完成,用户：{}, 路径名：{}", fileName, bucketName, path);
            }
            // 更新json文件 flag:update file directly
            metaJson.put("objects", objects);

            @Cleanup InputStream inputStream = new ByteArrayInputStream(metaJson.toJSONString().getBytes());
            // 上传元数据
            MinioHelper.uploadInputStream(bucketName, path, Constant.METADATA, inputStream);
        } catch (Exception e) {
            log.warn("直接上传失败", e);
            log.warn("直接上传失败，用户：{},路径：{}", bucketName, path);
            throw e;
        }
    }

    /**
     * 上传文件
     *
     * @param bucketName 用户id作为minio桶名
     * @param path       路径名（文件夹名），不填则在桶下
     * @param files      文件名（一个或多个）
     */
    @Transactional(rollbackFor = Exception.class)
    @SuppressWarnings("all")
    public void upload(HttpServletRequest request, String bucketName, String path,
                       @NotNull MultipartFile[] files) throws Exception {
        // 创建桶 重复则跳过
        MinioHelper.createBucket(bucketName);
        User user = (User) redisUtils.get(RedisKeyConstant.USER.concat(bucketName));
        // 忽略是否以 /结尾
        if (!path.endsWith("/")) {
            path = path.concat("/");
        }
        // 文件夹不存在 创建
        if (!MinioHelper.hasObject(bucketName, path)) {
            createDirectory(bucketName, path);
        }
        // 磁盘路径
        String realPath = filePath.concat(bucketName).concat("/").concat(path);
        if (!StringUtils.hasText(path)) {
            throw new RuntimeException("未选择桶");
        }

        // 获取元数据
//        JSONObject metaJson = null;
        // 获取文件夹下的子数据
//        Map<String, JSONObject> objects = null;

        BigInteger size = Optional.ofNullable(request.getParameter(HttpRequestParameter.SIZE))
                .map(BigInteger::new)
                .orElse(null);
        Integer partSize = Optional.ofNullable(request.getParameter(HttpRequestParameter.PART_SIZE))
                .map(Integer::parseInt)
                .orElse(null);
        String md5 = Optional.ofNullable(request.getParameter(HttpRequestParameter.MD5)).orElse(null);

        Integer chunk = Optional.ofNullable(request.getParameter(HttpRequestParameter.CHUNK))
                .map(Integer::parseInt)
                .orElse(null);
        Integer chunks = Optional.ofNullable(request.getParameter(HttpRequestParameter.CHUNKS))
                .map(Integer::parseInt)
                .orElse(null);

        Boolean isSpilt = Optional.ofNullable(request.getParameter(HttpRequestParameter.IS_SPLIT))
                .map(Boolean::valueOf)
                .orElse(false);

        // 如果没有分片
        if (chunks == null) {
//            metaJson = MinioHelper.getJsonFile(bucketName, path, Constant.METADATA);
//            objects = JSON.parseObject(metaJson.getString("objects"), Map.class);
        }

        try {
            for (MultipartFile file : files) {
                String fileName = file.getOriginalFilename();
                @Cleanup InputStream inputStream = file.getInputStream();
                // 前端分片上传
                if (isSpilt) {
                    String tmpFileName = (chunk + "_").concat(fileName.concat(Constant.TMP_FILE_FLAG));

                    MinioHelper.uploadInputStream(bucketName, path, tmpFileName, inputStream);

                    return;
                } else {
                    // 清除缓存
                    String key = bucketName.concat("/").concat(path);
                    redisUtils.del(key);

                    // 检查垃圾桶
                    if (MinioHelper.hasObject(bucketName, Constant.TRASH_BIN)) {
                        JSONObject trashJSON = getTrash(bucketName);
                        trashJSON.remove(path.concat(fileName));
                        updateJsonFile(trashJSON, bucketName, "", Constant.TRASH_BIN);
                    }

                    // 否则直接上传
                    log.info("上传文件：{},用户：{}, 路径名：{}", fileName, bucketName, path);
                    MinioHelper.uploadInputStream(bucketName, path, fileName, inputStream);
                }

                // 元数据
                BaseMetaData metaData = BaseMetaData.builder()
                        .creatorId(bucketName)
                        .date(LocalDateTime.now())
                        .creator(user.getName())
                        .title(fileName)
                        .build();

//                objects.put(metaData.getTitle(), (JSONObject) JSONObject.toJSON(metaData));

                log.info("上传文件：{}完成,用户：{}, 路径名：{}", fileName, bucketName, path);
            }
            // 更新json文件 flag:update file directly
//            metaJson.put("objects", objects);

//            @Cleanup InputStream inputStream = new ByteArrayInputStream(metaJson.toJSONString().getBytes());
            // 上传元数据
//            MinioHelper.uploadInputStream(bucketName, path, Constant.METADATA, inputStream);

        } catch (ServerException e) {
            log.warn("Minio连接错误", e);
            log.warn("上传文件失败, 桶名{}, 路径名：{}", bucketName, path);
            throw e;
        } catch (IOException e) {
            log.warn("IO错误", e);
            log.warn("上传文件失败, 桶名{}, 路径名：{}", bucketName, path);
            throw e;
        } catch (Exception e) {
            log.warn("文件上传失败", e);
            log.warn("上传文件失败,桶名{}, 路径名：{}", bucketName, path);
            throw new ServiceException("文件上传失败");
        }
    }

    /**
     * 逻辑删除后，更新元数据
     *
     * @param metadata   文件夹元数据
     * @param bucketName 桶名
     * @param path       路径
     * @param fileName   文件名
     */
    @SuppressWarnings("all")
    private void updateMetadataAfterFileDelete(JSONObject metadata, String bucketName,
                                               String path, String fileName) throws Exception {
        try {
            Map<String, JSONObject> map = JSON.parseObject(metadata.getString("objects"), Map.class);
            // 将objects + filename 转换为 BaseMetaData
            BaseMetaData bmd = JSON.parseObject(map.get(fileName).toJSONString(), BaseMetaData.class);
            // 逻辑删除
            bmd.setDeleted(true);
            // 写回文件
            map.put(fileName, (JSONObject) JSONObject.toJSON(bmd));
            metadata.put("objects", map);

            @Cleanup InputStream inputStream = new ByteArrayInputStream(metadata.toJSONString().getBytes());
            //更新逻辑删除文件
            MinioHelper.uploadInputStream(bucketName, path, Constant.METADATA, inputStream);

        } catch (Exception e) {
            log.warn("文件删除失败", e);
            log.warn("删除文件{}失败,桶名：{}, 路径名：{}", fileName, bucketName, path);
            throw e;
        }
    }

    /**
     * 递归物理删除文件夹下的东西
     */
    private void deleteDirectoryRecursively() {

    }

    /**
     * 逻辑删除后，更新回收站json
     *
     * @param bucketName  桶
     * @param path        路径
     * @param fileName    文件名
     * @param isDirectory 是否文件夹
     */
    private void updateTrashBinAfterDelete(String bucketName, String path,
                                           String fileName, boolean isDirectory, Trash ts) throws Exception {
        try {
            //获取删除信息
            JSONObject trash = MinioHelper.getJsonFile(bucketName, "", Constant.TRASH_BIN);
            // 文件不存在
            if (trash == null) {
                trash = new JSONObject();
            } else {
                Trash tmp = JSON.parseObject(trash.getString(path), Trash.class);
                // 存在 且为文件夹
                if (isDirectory && tmp != null) {
                    trash.remove(path);
                    // 文件夹下的子objects
                    List<Trash> trashList = tmp.getObjects();
                    List<Trash> tsList = ts.getObjects();

                    for (Trash t : trashList) {
                        String tmpName = t.getObjectName();

                        if (tsList != null && tsList.stream().anyMatch(a -> a.getObjectName().equals(tmpName))) {
                            continue;
                        }

                        if (tmpName.endsWith("/")) {
                            MinioHelper.physicalDeleteDirectory(bucketName, tmpName, trash);
                        } else {
                            MinioHelper.physicalDelete(bucketName, t.getPath(), t.getName());
                            trash.remove(tmpName);
                        }
                    }
                }
            }

            if (isDirectory) {
                trash.put(path, ts);
            } else {
                ts = new Trash();
                ts.setName(fileName);
                ts.setPath(path);
                ts.setObjectName(path + fileName);
                trash.put(path + fileName, ts);
            }

            @Cleanup InputStream in = new ByteArrayInputStream(trash.toJSONString().getBytes());
            MinioHelper.uploadInputStream(bucketName, "", Constant.TRASH_BIN, in);
        } catch (Exception e) {
            log.warn("更新回收站信息失败", e);
            throw e;
        }
    }

    private void updateJsonFile(JSONObject json,
                                String bucketName, String path, String fileName) throws Exception {
        @Cleanup InputStream in = new ByteArrayInputStream(json.toJSONString().getBytes());
        MinioHelper.uploadInputStream(bucketName, path, fileName, in);
    }


    /**
     * 逻辑删除某一个文件
     *
     * @param bucketName 用户id 作为minio桶名
     * @param path       路径名（文件夹名），不填则在桶下
     * @param fileName   文件名
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(String bucketName, String path, String fileName) throws Exception {
        try {

            // 清除缓存
            String key = bucketName.concat("/").concat(path);
            redisUtils.del(key);

            // 读取metadata.json
            JSONObject metadata = MinioHelper.getJsonFile(bucketName, path, Constant.METADATA);
            // 更新metadata
            this.updateMetadataAfterFileDelete(metadata, bucketName, path, fileName);
            // 更新回收站信息
            this.updateTrashBinAfterDelete(bucketName, path, fileName, false, null);
        } catch (Exception e) {
            log.warn("文件删除失败", e);
            log.warn("删除文件{}失败,桶名：{}, 路径名：{}", fileName, bucketName, path);
            throw e;
        }
    }

    @SuppressWarnings("all")
    private Trash updateAfterDeleteDirectory(String bucketName, String path)
            throws Exception {

        JSONObject metadata = MinioHelper.getJsonFile(bucketName, path, Constant.METADATA);
        Map<String, JSONObject> map = JSON.parseObject(metadata.getString("objects"), Map.class);
        metadata.put("isDeleted", true);

        // 递归逻辑删除
        Iterable<Result<Item>> results = MinioHelper.listObjects(bucketName, path);

        Trash trashResult = new Trash();
        trashResult.setPath(path);
        trashResult.setObjectName(path);

        String tmpName = path.substring(0, path.lastIndexOf("/"));
        tmpName = tmpName.replaceAll(".*/", "");
        trashResult.setName(tmpName);

        for (Result<Item> result : results) {
            Item item = result.get();
            String objectName = item.objectName();
            int slashIndex = objectName.lastIndexOf("/");

            // 如果是文件夹递归调用
            if (item.isDir()) {
                JSONObject dirMetadata = getMetadata(bucketName, objectName);

                // 为空跳过
                if ((boolean) dirMetadata.get("isDeleted")) {
                    continue;
                }

                Trash trash = updateAfterDeleteDirectory(bucketName, objectName);
                trashResult.addChild(trash);
            } else {
                String fileName = objectName.substring(slashIndex + 1);

                if (fileName.endsWith(Constant.METADATA)) {
                    continue;
                }

                BaseMetaData bmd = JSON.parseObject(map.get(fileName).toJSONString(), BaseMetaData.class);

                // 已删除 跳过
                if (bmd.isDeleted()) {
                    continue;
                }

                // 逻辑删除
                bmd.setDeleted(true);
                // 写回文件
                map.put(fileName, (JSONObject) JSONObject.toJSON(bmd));

                Trash trash = new Trash();
                trash.setName(fileName);
                trash.setPath(path);
                trash.setObjectName(objectName);

                trashResult.addChild(trash);
            }
        }

        metadata.put("objects", map);

        @Cleanup InputStream inputStream = new ByteArrayInputStream(metadata.toJSONString().getBytes());
        MinioHelper.uploadInputStream(bucketName, path, Constant.METADATA, inputStream);

        return trashResult;
    }

    /**
     * 删除所有桶/文件夹下的所有文件
     *
     * @param bucketName 桶名
     * @param path       路径 以/结尾
     */
    public void deleteDirectory(String bucketName, String path) throws Exception {
        try {
//            flag:update file directly
            // 标记元数据删除
            Trash trash = this.updateAfterDeleteDirectory(bucketName, path);
            //记录删除信息 to trash bin
            String fileName = path.substring(0, path.lastIndexOf("/"));
            fileName = fileName.replaceAll(".*/", "");
            this.updateTrashBinAfterDelete(bucketName, path, fileName, true, trash);
            // 清除缓存
            String key = getPeriodLevel(bucketName, path);
            redisUtils.del(key);
        } catch (Exception e) {
            log.warn("批量删除失败", e);
            log.warn("批量删除,路径：{},桶名{}", path, bucketName);
            throw e;
        }
    }

    private String getPeriodLevelGeneral(String objectName) {
        String[] array = objectName.split("/");
        int len = array.length;
        StringBuffer sb = new StringBuffer();

        for (int i = 0; i < len - 1; i++) {
            sb.append(array[i]).append("/");
        }
        return sb.toString();
    }

    /**
     * 获取上一级目录
     *
     * @param bucketName 桶名
     * @param objectName 对象名
     * @return {@link String}
     */
    private String getPeriodLevel(String bucketName, String objectName) {
        String str = getPeriodLevelGeneral(objectName);
        return bucketName.concat("/").concat(str);
    }

    public void get(HttpServletRequest request, OutputStream out) throws Exception {
        String uri = request.getRequestURI();
        uri = URLDecoder.decode(uri, "UTF-8");
        uri = uri.replace("/file/get/", "");

        int lastIndex = uri.lastIndexOf("/");
        int firstIndex = uri.indexOf("/");

        String bucketName = uri.substring(0, firstIndex);
        String path = uri.substring(firstIndex + 1, lastIndex + 1);
        // 文件夹
        if (uri.endsWith("/")) {
            downloadDirectory(out, bucketName, path);
        } else {
            // 文件
            String fileName = uri.substring(lastIndex + 1);
            download(out, bucketName, path, fileName);
        }
    }

    /**
     * 下载单文件
     *
     * @param bucketName 桶名
     * @param path       路径
     * @param fileName   文件名
     */
    public void download(OutputStream out, String bucketName, String path, String fileName) throws Exception {
        try {
            log.info("下载桶:{}, 路径:{}, 文件名:{}开始", bucketName, path, fileName);
            MinioHelper.download(out, bucketName, path, fileName);
            log.info("下载桶:{}, 路径:{}, 文件名:{}结束", bucketName, path, fileName);
        } catch (ServerException e) {
            log.warn("Minio连接错误", e);
            log.warn("下载桶:{}, 文件名:{}失败", bucketName, fileName);
            throw e;
        } catch (ErrorResponseException e) {
            log.warn("错误的返回", e);
            log.warn("下载桶:{}, 文件名:{}失败", bucketName, fileName);
            throw e;
        } catch (IOException e) {
            log.warn("文件IO错误", e);
            log.warn("下载桶:{}, 文件名:{}失败", bucketName, fileName);
            throw e;
        } catch (Exception e) {
            log.warn("文件下载失败", e);
            log.warn("下载桶:{}, 文件名:{}失败", bucketName, fileName);
            throw new ServiceException("文件下载失败");
        }
    }

    public void downloadDirectoryWithoutAuth(OutputStream out, String bucketName, String path) throws Exception {
        try {
            boolean flag = !bucketName.endsWith("/");
            // 文件不存在
            if (flag && !MinioHelper.hasObject(bucketName, path)) {
                throw new RuntimeException("该文件不存在");
            }

            if (flag) {
                path = path.substring(0, path.lastIndexOf("/") + 1);
            }

            MinioHelper.downloadAllWithoutAuth(out, bucketName, path);
        } catch (Exception e) {
            log.warn("");
            throw e;
        }
    }

    /**
     * 下载某一个文件夹所有文件
     *
     * @param bucketName 桶名
     * @param path       路径
     */
    public void downloadDirectory(OutputStream out, String bucketName, String path) throws Exception {
        try {
            MinioHelper.downloadAll(out, bucketName, path);
        } catch (ServerException e) {
            log.warn("Minio连接错误", e);
            log.warn("批量下载,路径：{},桶名{}", path, bucketName);
            throw e;
        } catch (InsufficientDataException e) {
            log.warn("批量下载,路径：{},桶名{}", path, bucketName);
            throw e;
        } catch (ErrorResponseException e) {
            log.warn("错误的返回", e);
            log.warn("批量下载,路径：{},桶名{}", path, bucketName);
            throw e;
        } catch (IOException e) {
            log.warn("文件IO错误", e);
            log.warn("批量下载,路径：{},桶名{}", path, bucketName);
            throw e;
        } catch (Exception e) {
            log.warn("批量下载,路径：{},桶名{}", path, bucketName);
            throw new ServiceException("批量下载失败");
        }
    }

    @SuppressWarnings("all")
    public void unzip(String bucketName, String path, String fileName) throws Exception {

        String key = bucketName.concat("/").concat(path);
        redisUtils.del(key);

        // 临时文件-随机名字
        @Cleanup InputStream inputStream = MinioHelper.getObject(bucketName, path, fileName);
        String tmpFileName = UUID.randomUUID() + Constant.ZIP_SUFFIX;
        File file = new File(tmpFileName);

        FileUtils.copyInputStreamToFile(inputStream, file);

        ZipFile zipFile = new ZipFile(file, "GBK");

        try {
            if (!StringUtils.hasText(path)) {
                path = "";
            }

            if (!fileName.endsWith(Constant.ZIP_SUFFIX)) {
                throw new ServiceException("文件非压缩文件");
            }

            log.info("解压文件: 桶：{}, 路径：{}, 文件：{}", bucketName, path, fileName);

            ZipEntry zipEntry;

            Enumeration<ZipEntry> zipEntries = zipFile.getEntries();

            List<String> tmpRecord = new ArrayList<>();

            while (zipEntries.hasMoreElements()) {
                zipEntry = zipEntries.nextElement();
                String zipEntryName = zipEntry.getName();

                String level;
                String period;
                // 如果不是文件夹
                if (!zipEntry.isDirectory()) {

                    MinioHelper.uploadInputStream(bucketName, path, zipEntryName, zipFile.getInputStream(zipEntry));

                    level = getPeriodLevelGeneral(zipEntryName);
                } else {
                    level = zipEntryName;
                }
                // 上一级
                period = getPeriodLevelGeneral(level);

                if (StringUtils.hasText(level) && !tmpRecord.contains(level)) {
                    tmpRecord.add(level);
                }

                if (StringUtils.hasText(period) && !tmpRecord.contains(period)) {
                    tmpRecord.add(period);
                }
            }

            for (String str : tmpRecord) {
                MinioHelper.createDirectory(bucketName, path + str);
                updateMetadata(bucketName, path + str);
            }

            this.createDirectory(bucketName, path);
            updateMetadata(bucketName, path);

            log.info("解压文件完成: 桶：{}, 路径：{}, 文件：{}", bucketName, path, fileName);
        } catch (Exception e) {
            log.warn("文件解压失败", e);
            throw e;
        } finally {
            zipFile.close();
            file.delete();
        }

    }

    /**
     * 获取元数据
     */
    public JSONObject getMetadata(String bucketName, String path) throws Exception {
        try {
            return this.getJsonFile(bucketName, path, Constant.METADATA);
        } catch (Exception e) {
            log.warn("获取元数据错误", e);
            throw e;
        }
    }


//    public void uploadRelate(String bucketName, FilePO target, MultipartFile[] files) throws Exception {
//        // 更新后的路径  ex: 1.txt/
//        String targetPath = target.getPath() + target.getName() + "/";
//        // 原object名称 xxx/1.txt
//        String srcObject = target.getPath() + target.getName();
//
//        // 从原文件复制到同路径下，名字加上-tmp
//        MinioHelper.copyObject(bucketName, target.getPath(), target.getName() + "-tmp", bucketName, srcObject);
//        // 删除原文件
//        MinioHelper.delete(bucketName, target.getPath(), target.getName());
//        // 复制到文件包
//        MinioHelper.copyObject(bucketName, targetPath, target.getName(), bucketName, srcObject + "-tmp");
//        // 删除tmp文件
//        MinioHelper.delete(bucketName, target.getPath(), target.getName() + "-tmp");
//
//        // 调用上传方法
//        this.upload(bucketName, targetPath, files);
//
//    }


//    public void setConfig(String bucketName, String path, String str) throws Exception {
//        try {
//
//            if (!StringUtils.hasText(path)) {
//                path = "";
//            }
//
//            String fileName = "config.json";
//
//            @Cleanup FileOutputStream fileOutputStream = new FileOutputStream(fileName);
//
//            fileOutputStream.write(str.getBytes());
//
//            MinioHelper.upload(bucketName, path, fileName);
//
//        } catch (ServerException e) {
//            log.warn("Minio连接错误", e);
//            log.warn("批量删除,路径：{},桶名{}", path, bucketName);
//            throw e;
//        } catch (InsufficientDataException e) {
//            log.warn("批量删除,路径：{},桶名{}", path, bucketName);
//            throw e;
//        } catch (ErrorResponseException e) {
//            log.warn("错误的返回", e);
//            log.warn("批量删除,路径：{},桶名{}", path, bucketName);
//            throw e;
//        } catch (IOException e) {
//            log.warn("文件IO错误", e);
//            log.warn("批量删除,路径：{},桶名{}", path, bucketName);
//            throw e;
//        } catch (Exception e) {
//            log.warn("批量删除失败", e);
//            log.warn("批量删除,路径：{},桶名{}", path, bucketName);
//            throw new ServiceException("批量删除失败");
//        }
//    }


    /**
     * 读取json类文件
     */
    public JSONObject getJsonFile(String bucketName, String path, String fileName) throws Exception {
        if (!StringUtils.hasText(path)) {
            path = "";
        }
        return MinioHelper.getJsonFile(bucketName, path, fileName);
    }

    @SuppressWarnings("unused")
    public InputStream getObject(String bucketName, String path, String fileName) throws Exception {
        if (!StringUtils.hasText(fileName)) {
            throw new ServiceException("文件名错误");
        }

        if (!StringUtils.hasText(path)) {
            path = "";
        }

        return MinioHelper.getObject(bucketName, path, fileName);
    }

    /**
     * 创建文件夹
     * <p>
     * ps：我又在写什么 2022/1/5  1600
     *
     * @param bucketName 桶名
     * @param name       文件夹名字
     */
    public void createDirectory(String bucketName, String name) throws Exception {

        String path = "";
        String[] array = name.split("/");
        int n = array.length;
        if (n >= 2) {
            path = name.replaceAll(array[n - 1].concat("/"), "");
        }

        // 清除缓存
        String key = bucketName.concat("/").concat(path);
        String[] keys = redisUtils.keys(key.concat("*"));
        redisUtils.del(keys);

        // 文件夹存在
        if (MinioHelper.hasObject(bucketName, name)) {

            Optional.ofNullable(this.getTrash(bucketName)).map(a -> a.remove(name));

            JSONObject metadata = getMetadata(bucketName, name);

            // 如果不是被删除
            if (!(boolean) metadata.get("isDeleted")) {
                return;
            }

            metadata.put("isDeleted", false);

            // 更新metadata
            @Cleanup InputStream inputStream = new ByteArrayInputStream(metadata.toJSONString().getBytes());
            MinioHelper.uploadInputStream(bucketName, name, Constant.METADATA, inputStream);

            return;
        }

        MinioHelper.createDirectory(bucketName, name);
    }

    /**
     * 获取下载url地址
     *
     * @param bucketName 桶名
     * @param path       路径
     * @param fileName   文件名
     * @return {@link String}
     */
    public String getUrl(String bucketName, String path, String fileName) throws Exception {
        try {
            if (!StringUtils.hasText(path)) {
                path = "";
            }

            if (!StringUtils.hasText(fileName)) {
                fileName = "";
            }

            String key = host.concat(UrlConstant.FILE_GET)
                    .concat(bucketName)
                    .concat("/")
                    .concat(path)
                    .concat(fileName);
            return key;
        } catch (Exception e) {
            log.warn("获取链接失败", e);
            throw e;
        }
    }

    /**
     * 获取文件夹url based on file/get
     */
    public JSONObject getDirectoryUrl(String bucketName, String path) throws Exception {
        try {
            JSONObject json = new JSONObject();

            Iterable<Result<Item>> results = MinioHelper.listObjects(bucketName, path);

            for (Result<Item> result : results) {
                Item item = result.get();
                String objectName = item.objectName();
                String name = objectName.replace(path, "");
                if (!item.isDir() && !objectName.endsWith(Constant.METADATA)) {
                    String keySuffix = host.concat(UrlConstant.FILE_GET)
                            .concat(bucketName)
                            .concat("/")
                            .concat(path);
                    json.put(name, keySuffix.concat(name));
                }
            }
            return json;
        } catch (Exception e) {
            log.warn("获取文件夹链接失败", e);
            throw e;
        }
    }

    /**
     * 获得回收站
     */
    public JSONObject getTrash(String bucketName) throws Exception {
        try {
            return MinioHelper.getJsonFile(bucketName, "", Constant.TRASH_BIN);
        } catch (Exception e) {
            log.warn("获取回收站失败", e);
            throw e;
        }
    }

    /**
     * 回复垃圾文件
     *
     * @param bucketName 桶名
     * @param objectName 对象名
     */
    public void recoverTrash(String bucketName, String objectName) throws Exception {
        // 获取trash_record.json
        JSONObject trash = getTrash(bucketName);
        //文件夹
        if (objectName.endsWith("/")) {
            // 获取trash信息
            Trash ts = trash.getObject(objectName, Trash.class);
            recoverTrashRecursive(bucketName, objectName, ts);
        } else {
            // 文件
            recoverTrashFile(bucketName, objectName);
        }

        // 清除删除记录
        trash.remove(objectName);
        @Cleanup InputStream inputStream = new ByteArrayInputStream(trash.toJSONString().getBytes());
        MinioHelper.uploadInputStream(bucketName, "", Constant.TRASH_BIN, inputStream);

        // 清除缓存
        String key = getPeriodLevel(bucketName, objectName);
        String[] keys = redisUtils.keys(key.concat("*"));
        redisUtils.del(keys);
    }

    /**
     * 恢复文件
     */
    @SuppressWarnings("all")
    private void recoverTrashFile(String bucketName, String objectName) throws Exception {
        try {
            int lastSlashIndex = objectName.lastIndexOf("/");
            // 获取路径
            String path = objectName.substring(0, lastSlashIndex + 1);
            // 文件名
            String fileName = objectName.substring(lastSlashIndex + 1);

            JSONObject metadata = getMetadata(bucketName, path);
            Map<String, JSONObject> map = JSON.parseObject(metadata.getString("objects"), Map.class);

            BaseMetaData bmd = JSON.parseObject(map.get(fileName).toJSONString(), BaseMetaData.class);

            bmd.setDeleted(false);

            map.put(fileName, (JSONObject) JSONObject.toJSON(bmd));

            metadata.put("objects", map);

            updateJsonFile(metadata, bucketName, path, Constant.METADATA);

        } catch (Exception e) {
            log.warn("恢复文件失败", e);
            throw e;
        }
    }

    /**
     * 恢复文件夹
     */
    @SuppressWarnings("unchecked")
    private void recoverTrashRecursive(String bucketName, String objectName, Trash trash) throws Exception {
        try {
            if (objectName.endsWith("/")) {
                // 获取meta文件
                JSONObject metaJson = getMetadata(bucketName, objectName);
                metaJson.put("isDeleted", false);

                Map<String, JSONObject> objects = JSON.parseObject(metaJson.getString("objects"), Map.class);

                // 获取子文件
                List<Trash> trashList = trash.getObjects();

                if (!CollectionUtils.isEmpty(trashList)) {
                    for (Trash ts : trashList) {
                        // 如果是路径相同的文件
                        if (!ts.getObjectName().endsWith("/") && ts.getPath().equals(objectName)) {
                            int n = objectName.lastIndexOf("/");
                            // 文件名
                            String fileName = ts.getObjectName().substring(n + 1);
                            // 获取metadata信息
                            BaseMetaData bmd = JSON.parseObject(objects.get(fileName).toJSONString(), BaseMetaData.class);
                            bmd.setDeleted(false);

                            objects.put(fileName, (JSONObject) JSONObject.toJSON(bmd));
                        } else {
                            // 递归文件夹
                            recoverTrashRecursive(bucketName, ts.getObjectName(), ts);
                        }
                    }
                }
                metaJson.put("objects", objects);

                @Cleanup InputStream is = new ByteArrayInputStream(metaJson.toJSONString().getBytes());
                MinioHelper.uploadInputStream(bucketName, objectName, Constant.METADATA, is);
            }
        } catch (Exception e) {
            log.warn("恢复文件失败", e);
            throw e;
        }
    }

    /**
     * 重命名文件夹
     *
     * @param bucketName 桶名
     * @param path       原路径
     * @param newName    新名字
     */
    public void renameDirectory(String bucketName, String path, String newName) throws Exception {
        // 预处理
        try {
            if (path.endsWith("/")) {
                path = path.substring(0, path.length() - 1);
            }
            int index = path.lastIndexOf("/");
            // 相对路径前缀
            String absPathPrefix = path.substring(0, index + 1);
            // 前缀
            String realPathPrefix = filePath.concat(bucketName).concat("/").concat(absPathPrefix);
            // 文件夹名
            String pathName = path.substring(index + 1);

            File dir = new File(realPathPrefix.concat(pathName));

            if (!dir.exists()) {
                throw new Exception("文件夹不存在");
            }
            if (!newName.endsWith("/")) {
                newName = newName.concat("/");
            }

            if (MinioHelper.hasObject(bucketName, newName)) {
                throw new Exception("已存在相同文件夹，请重新命名");
            }

            JSONObject json = this.getMetadata(bucketName, path.concat("/"));
            json.put("path", absPathPrefix.concat(newName));
            json.put("name", newName.substring(0, newName.length() - 1));

            @Cleanup InputStream in = new ByteArrayInputStream(json.toJSONString().getBytes());
            MinioHelper.uploadInputStream(bucketName, path.concat("/"), Constant.METADATA, in);

            dir.renameTo(new File(realPathPrefix.concat(newName)));

            redisUtils.del(bucketName.concat("/"));
            redisUtils.del(bucketName.concat("/").concat(absPathPrefix));
        } catch (Exception e) {
            log.warn("修改文件夹名失败", e);
            throw e;
        }
    }


    /**
     * 补锅专用函数！！！！！！！！！！！！！！
     */
    public void fixItManually(String bucketName, String path) throws Exception {
        MinioHelper.createDirectory(bucketName, path);
        updateMetadata(bucketName, path);
    }


    @SuppressWarnings("unchecked")
    private void updateMetadata(String bucketName, String path) throws Exception {

        User user = (User) redisUtils.get(RedisKeyConstant.USER.concat(bucketName));

        Iterable<Result<Item>> results = MinioHelper.getAll(bucketName, path);

        JSONObject metaJson = MinioHelper.getJsonFile(bucketName, path, Constant.METADATA);
        assert metaJson != null;
        Map<String, JSONObject> objects = JSON.parseObject(metaJson.getString("objects"), Map.class);

        for (Result<Item> result : results) {
            Item item = result.get();
            String objectName = item.objectName();
            String fileName = objectName.replace(path, "");

            // 如果不是文件夹或者不是metadata.json文件
            if (!item.isDir() && !objectName.endsWith(Constant.METADATA)) {

                JSONObject tmp = objects.get(fileName);

                // 如果是删除的跳过
                if (tmp != null) {
                    BaseMetaData bmd = JSON.parseObject(tmp.toJSONString(), BaseMetaData.class);
                    if (bmd != null && bmd.isDeleted()) {
                        continue;
                    }
                }

                BaseMetaData metaData = BaseMetaData.builder()
                        .creatorId(bucketName)
                        .creator(user.getName())
                        .date(LocalDateTime.now())
                        .title(fileName)
                        .build();

                objects.put(fileName, (JSONObject) JSONObject.toJSON(metaData));
            }
        }
        // 更新json文件
        metaJson.put("objects", objects);

        @Cleanup InputStream inputStream = new ByteArrayInputStream(metaJson.toJSONString().getBytes());
        // 上传元数据
        MinioHelper.uploadInputStream(bucketName, path, Constant.METADATA, inputStream);
    }
}
