package com.keivn.start.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.keivn.start.domain.Test;
import com.keivn.start.mapper.TestMapper;
import lombok.AllArgsConstructor;
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
        testMapper.updateById(test);
        return testMapper.selectById(test.getId());
    }

    public String delete(String id){
        testMapper.deleteById(id);
        return id;
    }
}
