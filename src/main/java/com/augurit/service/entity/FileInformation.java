package com.augurit.service.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

/**
 * 分片文件信息
 *
 * @author huang jiahui
 * @date 2022/1/11 13:55
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileInformation {
    private BigInteger size;
    private Integer partSize;
    private String md5;

    public boolean isMatch(BigInteger size, Integer partSize, String md5){

        return this.size.equals(size)
                && this.partSize.equals(partSize)
                && this.md5.equals(md5);
    }
}
