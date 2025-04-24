package com.zwp.speeddating.job;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zwp.speeddating.model.domain.User;
import com.zwp.speeddating.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 缓存预热任务
 *
 * @author zwp
 */
@Component
@Slf4j
public class PreCacheJob {

    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate redisTemplate;

    private RedissonClient redissonClient;

    private List<Long> mainUserList = Arrays.asList(1L); // 预热用户id为1的用户的推荐列表

    // 每天23:59:00执行，预热推荐用户
    @Scheduled(cron = "0 59 23 * * ?")
    public void doCacheRecommendUser() {
        RLock lock = redissonClient.getLock("zwp:precachejob:docache:lock");
        try {
            // 只有一个线程获取到锁
            if (lock.tryLock(0, -1, TimeUnit.MILLISECONDS )) {
                // 尝试获取锁，等待时间0，租约时间为-1自动续约，Redisson 的 "Watchdog" 机制会负责定期延长锁的租约，以防止锁在任务执行过程中过期。
                // Redisson 的 "Watchdog" 机制默认设置30s祖约时间，锁的有效时间默认为 30 秒，Redisson 会每隔 10 秒（1/3 超时时间）自动续约到30s,直到手动释放锁。
                System.out.println("getLock:" + Thread.currentThread().getId());
                for (Long userId : mainUserList) {
                    QueryWrapper<User> queryWrapper = new QueryWrapper<>();
                    Page<User> userPage = userService.page(new Page<>(1, 20), queryWrapper);
                    String redisKey = String.format("zwp:user:recommend:%s", userId);
                    ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
                    // 写缓存
                    try {
                        valueOperations.set(redisKey, userPage, 5, TimeUnit.MINUTES);
                        // 缓存数据在5分钟后会自动失效，Redis会将其删除
                    } catch (Exception e) {
                        log.error("redis set key error", e);
                    }
                }
            }
        } catch (InterruptedException e) {
            log.error("doCacheRecommendUser error", e);
        } finally {
            // 只能释放自己的锁
            if (lock.isHeldByCurrentThread()) {
                System.out.println("getLock:" + Thread.currentThread().getId());
                lock.unlock();
            }
        }
    }
}
