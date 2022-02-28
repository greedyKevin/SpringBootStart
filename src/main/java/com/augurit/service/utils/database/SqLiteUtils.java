package com.augurit.service.utils.database;

import com.alibaba.fastjson.JSONObject;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * sqlite 工具类
 * @author huang jiahui
 * @date 2021/12/22 14:16
 */
public class SqLiteUtils {

    public static void closeConnection(Connection connection) throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    public static Connection getConnection(String dbName) throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        return DriverManager.getConnection("jdbc:sqlite:" + dbName);
    }

    public static List<String> getTables(Connection connection) throws SQLException{
        ResultSet resultSet = connection.createStatement()
                .executeQuery("SELECT * from sqlite_master where type='table'");

        List<String> result = new ArrayList<>();

        while (resultSet.next()) {
            result.add(resultSet.getString("name"));
        }

        if(result.size() == 0) {
            return null;
        }

        return result;
    }

    public static List<String> getTableField(Connection connection, String tableName) throws SQLException {
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("select * from "+tableName + " limit 1");
        ResultSetMetaData metaData = resultSet.getMetaData();

        List<String> results = new ArrayList<>();
        int count = metaData.getColumnCount();

        for (int i = 1; i <= count; i++) {
            results.add(metaData.getColumnName(i));
        }

        return results;
    }


    public static JSONObject getTableData(Connection connection,
                                                String tableName, int page, int limit,
                                                String...fields) throws SQLException{
        Statement statement = connection.createStatement();
        StringBuilder sb = new StringBuilder("select ");

        ResultSet resultSet = statement.executeQuery("select count(1) from " + tableName);
        int total = resultSet.getInt(1);

        int len = fields.length;

        for (int i= 0;i<len; i++) {
            if (i == len-1) {
                sb.append(fields[i]).append(" ");
                continue;
            }
            sb.append(fields[i]).append(", ");
        }

        sb.append("from ").append(tableName).append(" limit ").append(limit)
            .append(" offset ").append((page-1) * limit);

        // select something from tableName limit x offset y
        String sql = sb.toString();

        resultSet = statement.executeQuery(sql);

        ResultSetMetaData metaData = resultSet.getMetaData();

        int count = metaData.getColumnCount();

        List<JSONObject> list = new ArrayList<>();

        while (resultSet.next()) {
            JSONObject json = new JSONObject();
            for (int i = 1; i <= count; i++) {
                String columnName = metaData.getColumnName(i);
                String value = resultSet.getString(columnName);
                json.put(columnName,value);
            }
            list.add(json);
        }
        JSONObject json = new JSONObject();
        json.put("total",total);
        json.put("rows",list);
        return json;
    }

    public static JSONObject getTableDataCondition(Connection connection,
                                                String tableName, int page, int limit,
                                                String query) throws SQLException{
        Statement statement = connection.createStatement();
        StringBuilder sb = new StringBuilder("select * from " + tableName + " ");

        sb.append(query);

        sb.append(" limit ").append(limit)
                .append(" offset ").append((page-1) * limit);


        ResultSet resultSet = statement.executeQuery("select count(1) from " + tableName + " " + query);
        int total = resultSet.getInt(1);

        // select something from tableName limit x offset y
        String sql = sb.toString();

        resultSet = statement.executeQuery(sql);

        ResultSetMetaData metaData = resultSet.getMetaData();

        int count = metaData.getColumnCount();

        List<JSONObject> list = new ArrayList<>();

        while (resultSet.next()) {
            JSONObject json = new JSONObject();
            for (int i = 1; i <= count; i++) {
                String columnName = metaData.getColumnName(i);
                String value = resultSet.getString(columnName);
                json.put(columnName,value);
            }
            list.add(json);
        }
        JSONObject json = new JSONObject();
        json.put("total",total);
        json.put("rows",list);
        return json;
    }
}
