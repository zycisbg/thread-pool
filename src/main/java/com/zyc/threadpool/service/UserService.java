package com.zyc.threadpool.service;

import com.zyc.threadpool.model.User;

import java.util.List;
import java.util.Map;

public interface UserService {

    Integer massTexting(List<User> users) throws Exception;

    Map massTextingByThreadPoolExecutorAndLatch();

    Map massTextingByThreadPoolExecutorAndBarrier();

    Map massTextingByThreadPoolExecutorAndLatchC();

    Map massTextingByForkJoinPoolByTask();

    void getUserAndMessageRecord() throws Exception;

    void massTextingByParallelStream();

    Map massTextingByForkJoinPool();

    void sendMessage(User user)throws Exception;

    void addUser(User user);

    List<User> findAllUser();
}
