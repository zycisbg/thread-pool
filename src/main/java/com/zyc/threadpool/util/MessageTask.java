package com.zyc.threadpool.util;

import com.sun.org.apache.regexp.internal.RE;
import com.zyc.threadpool.model.User;
import com.zyc.threadpool.service.UserService;

import java.util.List;
import java.util.concurrent.RecursiveTask;

public class MessageTask extends RecursiveTask<Integer>{
    //用户服务
    private UserService userService;
    //临界值
    private static final int COUNT = 500;
    //需要发短信的用户
    private List<User> users;

    public MessageTask(List<User> users, UserService userService){
        super();
        this.users = users;
        this.userService = userService;
    }

    @Override
    protected Integer compute() {
        if(users.size()>COUNT){
            int middle=users.size()/2;
            MessageTask left = new MessageTask(users.subList(0,middle),userService);
            MessageTask right = new MessageTask(users.subList(middle,users.size()),userService);
            left.fork();
            right.fork();
            //返回左半部分和右半部分相加的结果
            //返回结果其实就是 join  有return  就是与 RecursiveAction最大的区别
            return left.join()+right.join();
        }else{
            try {
                return userService.massTexting(users);
            }catch (Exception e){

            }
            return null;
        }
    }
}
