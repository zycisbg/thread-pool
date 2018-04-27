package com.zyc.threadpool.util;

import com.zyc.threadpool.model.User;
import com.zyc.threadpool.service.UserService;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;

/**
 * 子线程
 */
public class MessageCallableByBarrier implements Callable{
    private User user;

    private CyclicBarrier barrier;

    public MessageCallableByBarrier(User user,CyclicBarrier barrier){
        this.user = user;
        this.barrier = barrier;
    }

    @Override
    public Object call() throws Exception{

        System.out.println("我是用户："+user.getName()+",开始等待。");
        barrier.await();
        System.out.println("我是用户："+user.getName()+",全部等待完毕。一起执行");
        return null;
    }
}
