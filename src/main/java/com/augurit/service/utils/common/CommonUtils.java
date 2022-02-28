package com.augurit.service.utils.common;


import com.alibaba.fastjson.JSONObject;
import org.apache.commons.io.FileUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;

/**
 * 常用函数
 *
 * @author huang jiahui
 * @date 2022/1/11 9:22
 */
public class CommonUtils {

    /**
     *
     */
    private static final int COMPARE_SIZE = 50;

    /**
     * 对比两个文件内容是否相同
     *
     * @param multipartFile 上传文件
     * @param file          本地文件
     * @return boolean true: 相同； false: 不相同
     */
    public static boolean fileCompare(MultipartFile multipartFile, File file) throws Exception {
        long multiSize = multipartFile.getSize();
        long fileSize = file.length();
        // 比较长度
        if (multiSize != fileSize) {
            return false;
        }

        byte[] multiBytes = multipartFile.getBytes();
        byte[] fileBytes = FileUtils.readFileToByteArray(file);

        int mLen = multiBytes.length;
        int fLen = fileBytes.length;
        // byte size是否相同
        if (mLen != fLen) {
            return false;
        }
        // 如果整体小于100
        if (mLen <= COMPARE_SIZE + COMPARE_SIZE) {
            for (int i = 0; i < mLen; i++) {
                if (multiBytes[i] != fileBytes[i]) {
                    return false;
                }
            }
            return true;
        }

        // 前50个
        for (int i = 0; i < COMPARE_SIZE; i++) {
            if (multiBytes[i] != fileBytes[i]) {
                return false;
            }
        }

        // 后50个
        for (int i = mLen - 1; i >= mLen - COMPARE_SIZE; i--) {
            if (multiBytes[i] != fileBytes[i]) {
                return false;
            }
        }

        return true;
    }

    public static JSONObject getJson(InputStream inputStream) throws IOException {

        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));

        StringBuilder sb = new StringBuilder();

        String line;

        while ((line = br.readLine()) != null) {
            sb.append(line);
        }

        return JSONObject.parseObject(sb.toString());
    }
}
