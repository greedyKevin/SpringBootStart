package com.augurit.service.service;

import com.alibaba.fastjson.JSONObject;
import com.augurit.service.constant.Constant;
import com.augurit.service.constant.RedisKeyConstant;
import com.augurit.service.utils.minio.MinioHelper;
import com.augurit.service.utils.redis.RedisUtils;
import lombok.AllArgsConstructor;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 开放路径service
 *
 * @author huang jiahui
 * @date 2022/1/7 13:29
 */
@Service
@Slf4j
@AllArgsConstructor
public class AuthPathService {

    private final RedisUtils redisUtils;

    @SuppressWarnings("all")
    public List<String> getAll(String userId) throws Exception {
        // 不存在该文件
        if (!MinioHelper.hasObject(userId, Constant.AUTH_PATH)) {
            return null;
        }

        String key = RedisKeyConstant.AUTH_PATH.concat(userId);

        List<String> list = (List<String>) redisUtils.get(key);

        if (list != null) {
            return list;
        }

        JSONObject json = MinioHelper.getJsonFile(userId, "", Constant.AUTH_PATH);
        list = json.getObject(Constant.AUTH_PATH_KEY, List.class);

        // 写入缓存
        redisUtils.set(key,list);

        return list;
    }

    /**
     * @param userId
     * @param path
     * @throws Exception
     */
    @SuppressWarnings("all")
    public void add(String userId, String path) throws Exception {
        String absPath = getAbsPath(userId, path);

        // 没有该文件， 新增
        if (!MinioHelper.hasObject(userId, Constant.AUTH_PATH)) {
            JSONObject json = new JSONObject();
            List<String> list = new ArrayList<>();
            list.add(absPath);
            json.put(Constant.AUTH_PATH_KEY, list);

            String key = RedisKeyConstant.AUTH_PATH.concat(userId);
            // 写入缓存
            redisUtils.set(key, list);
            //更新文件
            @Cleanup InputStream in = new ByteArrayInputStream(json.toJSONString().getBytes());
            MinioHelper.uploadInputStream(userId, "", Constant.AUTH_PATH, in);
            return;
        }

        JSONObject json = MinioHelper.getJsonFile(userId, "", Constant.AUTH_PATH);
        List<String> list = json.getObject(Constant.AUTH_PATH_KEY, List.class);

        if (list.contains(absPath)) {
            throw new Exception("路径已存在");
        }

        list.add(absPath);
        json.put(Constant.AUTH_PATH_KEY, list);

        String key = RedisKeyConstant.AUTH_PATH.concat(userId);
        // 写入缓存
        redisUtils.set(key, list);
        // 更新文件
        @Cleanup InputStream in = new ByteArrayInputStream(json.toJSONString().getBytes());
        MinioHelper.uploadInputStream(userId, "", Constant.AUTH_PATH, in);
    }

    @SuppressWarnings("all")
    public void delete(String userId, String path) throws Exception {
        String absPath = getAbsPath(userId, path);

        String key = RedisKeyConstant.AUTH_PATH.concat(userId);
        // 清除缓存
        redisUtils.del(key);

        // 文件不存在
        if (!MinioHelper.hasObject(userId, Constant.AUTH_PATH)) {
            return;
        }
        // 读取文件
        JSONObject json = MinioHelper.getJsonFile(userId, "", Constant.AUTH_PATH);
        List<String> list = json.getObject(Constant.AUTH_PATH_KEY, List.class);

        // 不存在
        if (!list.contains(absPath)) {
            return;
        }

        list.remove(absPath);
        json.put(Constant.AUTH_PATH_KEY, list);
        //更新文件
        @Cleanup InputStream in = new ByteArrayInputStream(json.toJSONString().getBytes());
        MinioHelper.uploadInputStream(userId, "", Constant.AUTH_PATH, in);
    }


    private String getAbsPath(String userId, String path) {
        String absPath;

        if (!StringUtils.hasText(path)) {
            absPath = userId.concat("/.*");
            return absPath;
        }

        if (path.endsWith("/")) {
            absPath = userId.concat("/").concat(path).concat(".*");
        } else {
            absPath = userId.concat("/").concat(path);
        }

        return absPath;
    }
}
