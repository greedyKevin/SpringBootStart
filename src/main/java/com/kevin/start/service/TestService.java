package com.kevin.start.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.kevin.start.entity.Test;
import com.kevin.start.mapper.TestMapper;
import lombok.AllArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * TestService
 *
 * @author huang jiahui
 * @date 2021/11/11 11:48
 */
@Service
@AllArgsConstructor
public class TestService {

    private final TestMapper testMapper;

    public Test get(String id){
        return testMapper.selectById(id);
    }

    public List<Test> list(){
        return testMapper.selectList(new QueryWrapper<>());
    }

    public Test add(Test test){
        testMapper.insert(test);
        return test;
    }

    public Test update(Test test){
        Test result = testMapper.selectById(test.getId());
        BeanUtils.copyProperties(result,test);
        testMapper.updateById(result);
        return result;
    }

    public String delete(String id){
        testMapper.deleteById(id);
        return id;
    }
}
