package com.augurit.service.controller;

import com.augurit.service.service.VersionService;
import com.augurit.service.utils.result.ResultResponse;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * hfs版本控制
 *
 * @author huang jiahui
 * @date 2022/1/19 11:16
 */
@RestController
@AllArgsConstructor
@RequestMapping("/version")
public class VersionController {
    private final VersionService versionService;

    /**
     * 前端版本控制
     * @param version 版本号
     * @return {@link ResultResponse}<{@link Boolean}>
     */
    @GetMapping("/check")
    public ResultResponse<Boolean> check(String version){
        boolean result = versionService.checkVersion(version);
        return ResultResponse.success(result);
    }
}
