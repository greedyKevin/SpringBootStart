package com.augurit.service.controller;

import com.alibaba.fastjson.JSONObject;
import com.augurit.service.constant.Constant;
import com.augurit.service.service.FileDatabaseService;
import com.augurit.service.utils.jwt.JwtUtils;
import com.augurit.service.utils.result.ResultResponse;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * .db文件controller
 *
 * @author huang jiahui
 * @date 2021/12/23 10:20
 */
@RestController
@AllArgsConstructor
public class FileDatabaseController {

    private final FileDatabaseService fileDatabaseService;

    /**
     * 获取db文件的全部表
     *
     * @param path     路径
     * @param fileName .db文件名
     * @return {@link ResultResponse}<{@link Object}>
     */
    @GetMapping("/getTables")
    public ResultResponse<Object> getTables(HttpServletRequest request, String path, String fileName) throws Exception {
        String userId = JwtUtils.verifyJwtToken(request.getHeader(Constant.AUTHORIZATION));
        JSONObject tables = fileDatabaseService.getTables(userId, path, fileName);
        return ResultResponse.success(tables);
    }

    /**
     * 获取表的所有字段
     *
     * @param tableName 表名
     * @param dbPath    db文件绝对路径
     * @return {@link ResultResponse}<{@link Object}>
     */
    @GetMapping("/getTableField")
    public ResultResponse<Object> getTableField(String dbPath, String tableName) throws Exception {
        JSONObject result = fileDatabaseService.getTableField(dbPath, tableName);
        return ResultResponse.success(result);
    }

    /**
     * 获取数据库表的数据
     *
     * @param tableName 表名
     * @param page      当前页
     * @param rows      每页条数 默认 100
     * @param fields    字段名称
     * @param dbPath    db文件绝对路径
     * @return {@link ResultResponse}<{@link Object}>
     */
    @GetMapping("/getTableData")
    public ResultResponse<Object> getTableData(String dbPath, String tableName,
                                               Integer page,
                                               @RequestParam(defaultValue = "100") Integer rows,
                                               String...fields) throws Exception {
        JSONObject result = fileDatabaseService.getTableData(dbPath, tableName, page, rows, fields);
        return ResultResponse.success(result);
    }

    @GetMapping("/getTableDataCondition")
    public ResultResponse<Object> getTableDataWhere(String dbPath, String tableName,
                                               Integer page,
                                               @RequestParam(defaultValue = "100") Integer rows,
                                               String query) throws Exception {
        JSONObject result = fileDatabaseService.getTableDataCondition(dbPath, tableName, page, rows, query);
        return ResultResponse.success(result);
    }
}

