package com.zwp.speeddating.service;

import org.junit.jupiter.api.Test;
import org.redisson.api.RList;
import org.redisson.api.RLock;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

@SpringBootTest
public class RedissonTest {

    @Autowired
    private RedissonClient redissonClient;

    @Test
    void test() {
        // list,数据存在本地JVM内存中
        ArrayList<Object> list = new ArrayList<>();
        list.add("zwp");
        System.out.println("list" + list.get(0));
//        list.remove(0);

        // 数据存在redis内存中
        RList<Object> rList = redissonClient.getList("test-list");
        rList.add("zwp");
        System.out.println("rList" + rList.get(0));
//        rList.remove(0);

        // map
        HashMap<Object, Integer> map = new HashMap<>();
        map.put("zwp", 1);
        map.get("zwp");

        RMap<Object, Object> map1 = redissonClient.getMap("test-map");
        map1.put("zwp", 1);
    }

    //set

    //stack

    @Test
    void testWatchDog() {
        // 锁实现机制是通过 Redis 的 SET NX（只有不存在时设置键）命令和超时时间控制
        RLock lock = redissonClient.getLock("speeddating:precachejob:docache:lock");
        try {
            // 只有一个线程能获取到锁
            // 等待锁的时间为 0 毫秒，表示不等待，立即尝试获取锁。
            /*锁的自动释放时间为 -1，表示锁没有固定的超时时间。
            Redisson 会自动启动一个“看门狗机制”（WatchDog）。锁的有效时间默认为 30 秒，Redisson 会每隔 10 秒（1/3 超时时间）自动续约一次，直到线程释放锁*/
            if (lock.tryLock(0, -1, TimeUnit.MILLISECONDS)) {
                // 每隔 10 秒，Redisson 会调用 Redis 的 EXPIRE 命令，重新设置锁的有效期为 30 秒
                Thread.sleep(300000); // 模拟任务执行的长时间操作(5min后，执行finally释放锁)
                System.out.println("getLock: " + Thread.currentThread().getId());
            }
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        } finally {
            // 只能释放自己的锁
            if (lock.isHeldByCurrentThread()) {
                System.out.println("unLock: " + Thread.currentThread().getId());
                lock.unlock(); // 一旦任务完成并释放了锁（调用 lock.unlock()），看门狗会停止对锁的续期
            }
        }
    }
}
