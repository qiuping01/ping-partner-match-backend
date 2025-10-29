package com.ping.usercenter.once;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;

import com.ping.usercenter.mapper.UserMapper;
import com.ping.usercenter.model.domain.User;
import com.ping.usercenter.service.UserService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;

//@Component  // 将其变成SpringBoot的一个Bean,当项目启动时就会加载当前类
public class InsertUsers {

    @Resource
    private UserMapper userMapper;

    @Resource
    private UserService userService;

    private ExecutorService executorService = new ThreadPoolExecutor(60
            , 1000
            , 10000
            , TimeUnit.MINUTES
            , new ArrayBlockingQueue<>(10000));

    /**
     * 并发批量插入用户
     */
    @Scheduled(initialDelay = 5000, fixedDelay = Long.MAX_VALUE)
    public void doInsertUsers() {
        StopWatch stopWatch = new StopWatch();
        System.out.println("开始插入大量假数据");
        stopWatch.start();
        doConcurrencyInsert();
        stopWatch.stop();
        System.out.println("总计插入时间：" + stopWatch.getTotalTimeMillis());
    }

    private void doConcurrencyInsert() {
        final int INSERT_NUM = 100000;
        // 分十组
        int j = 0;
        int batchSize = 5000;
        // 任务数组
        List<CompletableFuture<Void>> futureList = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            List<User> userList = new ArrayList<>();
            while (true) {
                j++;
                User user = new User();
                user.setUsername("第" + i + "组的分批假用户" + j);  // 添加序号区分
                user.setUserAccount("fakeFakePing" + j);
                user.setAvatarUrl("https://img0.baidu.com/it/u=3191928921,42379733&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=500");
                user.setGender(0);
                user.setUserPassword("12345678");
                user.setPhone("123" + j);  // 避免重复
                user.setEmail(j + "123@qq.com");  // 避免重复
                user.setUserStatus(0);
                user.setUserRole(0);
                user.setPlanetCode("111111111");
                user.setTags("[]");
                user.setProfile("我是分批假用户" + j);
                userList.add(user);
                if (j % batchSize == 0) {
                    break;
                }
            }
            // 异步执行
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                System.out.println("threadName: " + Thread.currentThread().getName());
                userService.saveBatch(userList, batchSize);
            },executorService);
            futureList.add(future);
        }
        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[]{})).join();
    }
}
