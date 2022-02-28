package com.augurit.service.service;

import com.alibaba.fastjson.JSONObject;
import com.augurit.service.constant.Constant;
import com.augurit.service.utils.minio.MinioHelper;
import lombok.AllArgsConstructor;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * 物理删除服务
 *
 * @author huang jiahui
 * @date 2022/1/8 13:45
 */
@Service
@AllArgsConstructor
@Slf4j
@SuppressWarnings("all")
public class PhysicalDeleteService {

    public void delete(String bucketName,String path, String fileName) throws Exception{
        MinioHelper.physicalDelete(bucketName,path,fileName);
        // 更新回收站文件
        JSONObject json = MinioHelper.getJsonFile(bucketName, "", Constant.TRASH_BIN);
        json.remove(path.concat(fileName));
        @Cleanup InputStream in = new ByteArrayInputStream(json.toJSONString().getBytes());
        MinioHelper.uploadInputStream(bucketName, "",Constant.TRASH_BIN,in);
    }

    public void deleteDirectory(String bucketName,String path) throws Exception{
        JSONObject json = MinioHelper.getJsonFile(bucketName, "", Constant.TRASH_BIN);
        MinioHelper.physicalDeleteDirectory(bucketName, path,json);
    }
}
