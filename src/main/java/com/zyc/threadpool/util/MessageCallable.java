package com.zyc.threadpool.util;

import com.zyc.threadpool.model.User;
import com.zyc.threadpool.service.UserService;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

/**
 * 发短信的线程。
 * 简单说下，Callable 和Runnable 的区别，
 * Callable有返回结果，带泛型；泛型为返回结果的类型，
 * Runnable 没有返回结果。
 * 下边这个线程有返回结果，就不写了。
 */
public class MessageCallable implements Callable{
    //需要被操作的用户
    private User user;
    //服务类
    private UserService userService;
    //倒数计数器，为了阻塞主线程
    private CountDownLatch latch;

    /**
     * 有参构造器，为了给这三个赋值
     * @param user
     * @param userService
     * @param latch
     */
    public MessageCallable(User user,UserService userService,CountDownLatch latch){
        this.user = user;
        this.userService = userService;
        this.latch = latch;
    }

    @Override
    public Object call() throws Exception{
        try {
            //调用发短信方法。
            userService.sendMessage(user);
        }finally {
            //必须执行在finally中。
            //如果 没有 countDown  会导致主线程在子线程都执行后也阻塞
            latch.countDown();
            //可以把返回结果在这里返回
            return user.getId();
        }
    }
}
