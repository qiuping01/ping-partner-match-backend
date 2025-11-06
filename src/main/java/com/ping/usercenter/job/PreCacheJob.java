package com.ping.usercenter.job;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ping.usercenter.model.domain.User;
import com.ping.usercenter.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 缓存预热任务
 *
 * @author qiuping
 */
@Component
@Slf4j
public class PreCacheJob {

    @Resource
    private UserService userService;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    // 重点用户
    private List<Long> mainUserList = Arrays.asList(1L);


    // https://cron.ciding.cc/
    @Scheduled(cron = "0 11 20 * * ? ")
    public void doCacheRecommendUser() {
        for (Long userId : mainUserList) {
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            Page<User> userPage = userService.page(new Page<>(1, 20), queryWrapper);
            String redisKey = String.format("partner-match:user:recommend:%s"
                    , userId);
            ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
            // 写缓存
            try {
                // 30s 过期
                valueOperations.set(redisKey, userPage, 30000,
                        TimeUnit.MILLISECONDS);
            } catch (Exception e) {
                log.error("redis set key error", e);
            }
        }
    }
}
