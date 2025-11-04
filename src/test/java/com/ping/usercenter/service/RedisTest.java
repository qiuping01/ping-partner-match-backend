package com.ping.usercenter.service;
import java.util.Date;

import com.ping.usercenter.model.domain.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import javax.annotation.Resource;

@SpringBootTest
public class RedisTest {

    @Resource
    private RedisTemplate redisTemplate;

    @Test
    void test(){
        ValueOperations valueOperations = redisTemplate.opsForValue(); // 所有操作
        // 增
        valueOperations.set("pingString","dog");
        valueOperations.set("pingInt",1);
        valueOperations.set("pingDouble",2.00);
        valueOperations.set("pingUser",newUser());
        // 查
        Object ping = valueOperations.get("pingString");
        Assertions.assertEquals("dog", (String) ping);
        ping = valueOperations.get("pingInt");
        Assertions.assertTrue( 1 == (Integer) ping);
        ping = valueOperations.get("pingDouble");
        Assertions.assertTrue( 2.00 == (Double) ping);
        System.out.println(valueOperations.get("pingUser"));
        // 改
        valueOperations.set("pingString","codeMan");
        // 删
        redisTemplate.delete("pingString");
    }

    User newUser(){
        User user = new User();
        user.setId(0L);
        user.setUsername("Test");
        user.setUserAccount("");
        user.setAvatarUrl("");
        user.setGender(0);
        user.setUserPassword("");
        user.setPhone("");
        user.setEmail("");
        user.setUserStatus(0);
        user.setCreateTime(new Date());
        user.setUpdateTime(new Date());
        user.setIsDelete(0);
        user.setUserRole(0);
        user.setPlanetCode("");
        user.setTags("");
        user.setProfile("");
        return user;
    }


}
