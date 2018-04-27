package com.zyc.threadpool.util;

import com.zyc.threadpool.model.User;
import com.zyc.threadpool.service.UserService;

import java.util.List;
import java.util.concurrent.RecursiveAction;

public class MessageAction extends RecursiveAction {
    //用户服务
    private UserService userService;
    //临界值
    private static final int COUNT = 500;
    //需要被处理的数据
    private List<User> users;
    //通过构造器传参
    public MessageAction(List<User> users, UserService userService){
        super();
        this.users = users;
        this.userService = userService;
    }

    //该方法是必须实现的。
    @Override
    protected void compute() {
        //如果users.size() 没有拆分到临界值，那么继续拆分
        if(users.size()>COUNT){
            //用了二分查找来拆分，一个从中间向左找，一个从中间向右找。一直递归到小于临界值
            int middle=users.size()/2;
            MessageAction left = new MessageAction(users.subList(0,middle),userService);
            MessageAction right = new MessageAction(users.subList(middle,users.size()),userService);
            left.fork();
            right.fork();
        }else{
            try {
                //小于临界值后执行
                userService.massTexting(users);
            }catch (Exception e){

            }
        }
    }
}
