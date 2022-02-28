package com.augurit.service.service;

import com.augurit.service.utils.redis.RedisUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 授权Service
 *
 * @author huang jiahui
 * @date 2021/11/22 14:34
 */
@Service
@AllArgsConstructor
@Slf4j
public class AuthorizationService {

    /**
     * redis工具类
     */
    private final RedisUtils redisUtils;

    /**
     * 文件授权
     * @param userId 被授权用户id
     * @param fileId 文件id
     * @param level  权限等级
     * @return boolean
     */
    public boolean authorize(String userId, String fileId, Integer level) {
       return redisUtils.hset(userId, fileId, level);
    }


    /**
     * 查看授权
     * @param userId 用户id
     * @param fileId 文件id
     * @return {@link Integer} 授权等级
     */
    public Integer checkAccess(String userId, String fileId) {

        Map<Object, Object> map = redisUtils.hmget(userId);
        Object result = map.get(fileId);

        return (Integer) result;
    }

}
