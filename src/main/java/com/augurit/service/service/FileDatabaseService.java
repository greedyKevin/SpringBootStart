package com.augurit.service.service;

import com.alibaba.fastjson.JSONObject;
import com.augurit.service.utils.database.SqLiteUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.util.List;

/**
 * .db文件服务
 *
 * @author huang jiahui
 * @date 2021/12/23 10:21
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class FileDatabaseService {

    @Value("${filePath}")
    private String filePath;

    private String toAbsolutePath(String bucketName, String path, String fileName) {
        return filePath + bucketName.concat("/").concat(path).concat(fileName);
    }

    public JSONObject getTables(String bucketName, String path, String fileName) throws Exception {
        try {
            String realPath = toAbsolutePath(bucketName, path, fileName);
            Connection connection = SqLiteUtils.getConnection(realPath);
            JSONObject json = new JSONObject();

            List<String> list = SqLiteUtils.getTables(connection);
            json.put("dbPath", realPath);
            json.put("list", list);

            SqLiteUtils.closeConnection(connection);
            return json;
        } catch (Exception e) {
            log.warn("获取数据库表失败", e);
            throw e;
        }
    }

    public JSONObject getTableField(String dbPath , String tableName) throws Exception {
        try {
            Connection connection = SqLiteUtils.getConnection(dbPath);

            List<String> tableField = SqLiteUtils.getTableField(connection, tableName);

            SqLiteUtils.closeConnection(connection);
            JSONObject json = new JSONObject();
            json.put("dbPath", dbPath);
            json.put("list", tableField);

            return json;

        } catch (Exception e) {
            log.warn("获取表:{}字段失败", tableName, e);
            throw e;
        }
    }

    public JSONObject getTableData(String dbPath,String tableName,
                                   int page, int limit, String... fields) throws Exception {
        try {
            Connection connection = SqLiteUtils.getConnection(dbPath);

            JSONObject tableData = SqLiteUtils.getTableData(connection, tableName, page, limit, fields);

            SqLiteUtils.closeConnection(connection);
            return tableData;
        } catch (Exception e) {
            log.warn("获取表：{}数据失败", tableName, e);
            throw e;
        }
    }

    public JSONObject getTableDataCondition(String dbPath,String tableName,
                                        int page, int limit, String query) throws Exception {
        try{
            Connection connection = SqLiteUtils.getConnection(dbPath);
            JSONObject tableData = SqLiteUtils.getTableDataCondition(connection, tableName, page, limit, query);
            SqLiteUtils.closeConnection(connection);
            return tableData;
        }catch (Exception e) {
            log.warn("获取表：{}数据失败", tableName, e);
            throw e;
        }
    }
}
