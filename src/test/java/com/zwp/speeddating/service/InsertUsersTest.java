package com.zwp.speeddating.service;

import com.zwp.speeddating.model.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * 导入用户测试
 *
 * @author zwp
 */
@SpringBootTest
public class InsertUsersTest {

    @Resource
    private UserService userService;

    private ExecutorService executorService = new ThreadPoolExecutor(40, 1000, 10000, TimeUnit.MINUTES, new ArrayBlockingQueue<>(10000));
    // 创建了一个 ThreadPoolExecutor 线程池，其配置如下：
    // 核心线程数： 40 (始终保持 40 个线程活动)
    // 最大线程数： 1000 (最多可以创建 1000 个线程)
    // 线程存活时间： 10000 分钟 (约 6.94 天) (非核心线程在空闲超过 10000 分钟后会被回收，但实际上这个时间非常长，几乎等同于不回收)
    // 时间单位： 分钟 (TimeUnit.MINUTES)
    // 工作队列： ArrayBlockingQueue，容量为 10000 (使用有界数组阻塞队列，容量为 10000),避免了任务无限制堆积导致内存溢出。当队列满时，线程池会尝试创建更多线程来处理任务

    /**
     * 批量插入用户
     */
    @Test
    public void doInsertUsers() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        final int INSERT_NUM = 100000;
        List<User> userList = new ArrayList<>();
        for (int i = 0; i < INSERT_NUM; i++) {
            User user = new User();
            user.setUsername("假zwp");
            user.setUserAccount("fakezwp");
            user.setAvatarUrl("https://636f-codenav-8grj8px727565176-1256524210.tcb.qcloud.la/img/logo.png");
            user.setGender(0);
            user.setUserPassword("12345678");
            user.setPhone("123");
            user.setEmail("123@qq.com");
            user.setTags("[]");
            user.setUserStatus(0);
            user.setUserRole(0);
            user.setPlanetCode("11111111");
            userList.add(user);
        }
        // 20 秒 10 万条
        userService.saveBatch(userList, 10000);
        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeMillis());
    }

    /**
     * 并发批量插入用户
     */
    @Test
    public void doConcurrencyInsertUsers() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        // 分100组
        int batchSize = 5000;
        int j = 0; // 初始化计数器j
        List<CompletableFuture<Void>> futureList = new ArrayList<>(); // 用于存储CompletableFuture对象的 List,即异步批量插入任务的集合

        for (int i = 0; i < 100; i++) {
            List<User> userList = new ArrayList<>(); // 为每个并发任务创建一个新的 userList
            while (true) { // 内层循环：创建用户直到达到 batchSize
                j++; // 递增用户计数器 j
                User user = new User();
                user.setUsername("假zwp");
                user.setUserAccount("fakezwp");
                user.setAvatarUrl("https://636f-codenav-8grj8px727565176-1256524210.tcb.qcloud.la/img/logo.png");
                user.setGender(0);
                user.setUserPassword("12345678");
                user.setPhone("123");
                user.setEmail("123@qq.com");
                user.setTags("[]");
                user.setUserStatus(0);
                user.setUserRole(0);
                user.setPlanetCode("11111111");
                userList.add(user);
                if (j % batchSize == 0) {
                    break;
                }
            }
            // 异步执行
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> { // 创建一个CompletableFuture用于异步执行
                System.out.println("threadName: " + Thread.currentThread().getName()); // 打印线程名称用于调试
                userService.saveBatch(userList, batchSize); // 调用 userService.saveBatch 批量插入用户批次
            }, executorService); // 使用 executorService (线程池) 执行任务
            futureList.add(future); // 将 CompletableFuture 添加到 futureList
        }
        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[]{})).join(); // 等待所有 CompletableFuture 任务完成
        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeMillis());
    }
}
