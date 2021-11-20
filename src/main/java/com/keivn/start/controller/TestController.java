package com.keivn.start.controller;

import com.keivn.start.entity.Test;
import com.keivn.start.result.ResultResponse;
import com.keivn.start.service.TestService;
import com.keivn.start.utils.redis.RedisUtil;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Test Controller
 *
 * @author huang jiahui
 * @date 2021/11/11 11:45
 */
@RestController
@RequestMapping("/test")
@AllArgsConstructor
public class TestController {


    /**
     * test service
     */
    private final TestService testService;

    /**
     * 根据id获取
     * @param id id
     * @return {@link ResultResponse}
     */
    @GetMapping("/get/{id}")
    public ResultResponse get(@PathVariable String id){
        // 根据id获取结果
        Test result = testService.get(id);
        // 返回结果
        return ResultResponse.success(result);
    }

    /**
     * 获取全部
     * @return {@link ResultResponse}
     */
    @GetMapping("/list")
    public ResultResponse list(){
         List<Test> result = testService.list();
         return ResultResponse.success(result);
    }

    /**
     * 添加一个
     *
     * @param test {@see Test}
     * @return {@link ResultResponse}
     */
    @PostMapping("/add")
    public ResultResponse add(Test test){
        Test result = testService.add(test);
        return ResultResponse.success(result);
    }

    /**
     * 更新
     * @param test
     *
     * @return {@link ResultResponse}
     */
    @PutMapping("/update")
    public ResultResponse update(Test test){
        Test result = testService.update(test);
        return ResultResponse.success(result);
    }

    /**
     * 删除
     * @param id
     * @return {@link ResultResponse}
     */
    @DeleteMapping("/delete/{id}")
    public ResultResponse delete(@PathVariable String id){
        String result = testService.delete(id);
        return ResultResponse.success(result);
    }

}
