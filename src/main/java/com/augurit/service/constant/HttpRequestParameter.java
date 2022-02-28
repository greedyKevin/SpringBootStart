package com.augurit.service.constant;


/**
 * http请求自定参数
 *
 * @author huang jiahui
 * @date 2022/1/17 15:31
 */
public class HttpRequestParameter {

    /**
     * 文件大小
     */
    public static final String SIZE = "size";
    /**
     * 每片分片大小
     */
    public static final String PART_SIZE = "partSize";
    /**
     * 整体文件MD5
     */
    public static final String MD5 = "md5";
    /**
     * 分片index
     */
    public static final String CHUNK = "chunkNumber";
    /**
     * 总分片数量
     */
    public static final String CHUNKS = "chunks";

    public static final String IS_SPLIT = "isSplit";
}
