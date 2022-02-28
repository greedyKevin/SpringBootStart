package com.augurit.service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 版本
 *
 * @author huang jiahui
 * @date 2022/1/19 11:18
 */
@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Slf4j
public class VersionService {
    @Value("${version}")
    private String version;

    public boolean checkVersion(String version){
        return this.version.equals(version);
    }
}
