package com.augurit.service.utils.common;


/**
 * 处理bit与byte
 *
 * @author huang jiahui
 * @date 2022/1/10 17:24
 */
public class BitConverter {

    public static Byte[] intToByteArray(int i) {
        Byte[] result = new Byte[4];
        result[0] = (byte) ((i >> 24) & 0xFF);
        result[1] = (byte) ((i >> 16) & 0xFF);
        result[2] = (byte) ((i >> 8) & 0xFF);
        result[3] = (byte) (i & 0xFF);
        return result;
    }


    public static int byteArrayToInt(byte[] bytes) {
        int value = 0;
        for (int i = 0; i < 4; i++) {
            int shift = (3 - i) * 8;
            value += (bytes[i] & 0xFF) << shift;
        }
        return value;
    }
}
