package com.zyc.threadpool.service.impl;

import com.zyc.threadpool.dao.MessageRecordMapper;
import com.zyc.threadpool.dao.UserMapper;
import com.zyc.threadpool.model.MessageRecord;
import com.zyc.threadpool.model.MessageRecordExample;
import com.zyc.threadpool.model.User;
import com.zyc.threadpool.model.UserExample;
import com.zyc.threadpool.service.UserService;
import com.zyc.threadpool.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

@Service
public class UserServiceImpl implements UserService{

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MessageRecordMapper messageRecordMapper;

    @Autowired
    private ThreadPoolForSpring threadPoolForSpring;

    public void massTextingByParallelStream(){
        long start = System.currentTimeMillis();

        List<User> allUser = this.findAllUser();
        allUser.parallelStream().forEach(user -> {
            MessageRecord messageRecord = new MessageRecord();
            messageRecord.setUserId(user.getId());
            messageRecord.setUserPhone(user.getPhone());
            messageRecord.setMessageContent(user.getName()+",你好，这是你的短信。");
            messageRecord.setCurrentTimeMillis(System.currentTimeMillis()+"");
            messageRecordMapper.insertSelective(messageRecord);
            System.out.println("给用户id：["+user.getId()+"]发送短信。当前线程:["+Thread.currentThread().getName()+"]");
        });
        System.out.println("耗时：["+ (System.currentTimeMillis()-start)+"]毫秒");
    }

    public void getUserAndMessageRecord() throws Exception {
        //开启异步
        Future<List<User>> usersFuture  = threadPoolForSpring.submit(()-> {
            System.out.println("当前线程:"+Thread.currentThread().getName());
            return this.findAllUser();
        });
        MessageRecordExample recordExample = new MessageRecordExample();

        List<MessageRecord> messageRecords = messageRecordMapper.selectByExample(recordExample);

        System.out.println("同步查询messageRecords:"+messageRecords.size()+"条");
        System.out.println("异步查询users:"+usersFuture.get().size()+"条");

    }

    /**
     * @return
     * 有返回结果的forkJoinPool
     */
    @Override
    public Map massTextingByForkJoinPoolByTask() {
        Map<String,Object> resultMap = new HashMap(2);
        ForkJoinPool pool = new ForkJoinPool();
        List<User> users = null;
        Integer sum = null;
        long start = System.currentTimeMillis();
        try {
            users = this.findAllUser();
            if(users==null || users.size()==0){
                resultMap.put("isSuccess",false);
                resultMap.put("message","没有用户");
            }else{
                //invoke 和submit的区别在于 invoke是同步的。
                sum = pool.invoke(new MessageTask(users, this));
                resultMap.put("isSuccess",true);
                resultMap.put("message","发送成功");
            }
        }catch (Exception e){
            resultMap.put("isSuccess",false);
            resultMap.put("message","系统异常");
            return resultMap;
        }finally {
            pool.shutdown();
            System.out.println(sum);
            System.out.println("耗时：["+ (System.currentTimeMillis()-start)+"]毫秒");
            return resultMap;
        }


    }
    
    /**
     * 2509
     * 2394
     * 2161
     * 2467
     * 注：由于 这个没有countDownLatch 阻塞主线程，所以不能用单元测试来跑，而且统计下来的执行时间 要从数据库看
     * @return
     */
    @Override
    public Map massTextingByForkJoinPool() {
        Map<String,Object> resultMap = new HashMap(2);
        //一般就用这种创建方式就行。这种创建方式会创建一个 cpu核数-1 的线程池，是最合理的。
        //用new ForkJoinPool()也可以。会创建一个 cpu核数 的线程池
        ForkJoinPool pool = ForkJoinPool.commonPool();
        List<User> users = null;
        long start = System.currentTimeMillis();
        try {
            users = this.findAllUser();
            if(users==null || users.size()==0){
                resultMap.put("isSuccess",false);
                resultMap.put("message","没有用户");
            }else{
                //在这里执行运行就可以，execute没有返回结果，submit和invoke有返回结果。
                pool.execute(new MessageAction(users,this));
                resultMap.put("isSuccess",true);
                resultMap.put("message","发送成功");
            }
        }catch (Exception e){
            resultMap.put("isSuccess",false);
            resultMap.put("message","系统异常");
            return resultMap;
        }finally {
            pool.shutdown();
            return resultMap;
        }

    }

    /**
     * threads   time(ms)
     * fix30     -  3485
     * 30     -  2995
     * 30     -  3092
     * 10     -  2890
     * 10     -  2976
     * 10     -  3023
     * 4      -  3584
     * 4      -  3775
     * 4      -  3781
     * cache  -  14025
     * cache  -  7093
     * cache  -  10235
     * single -  8950
     * single -  8948
     * single -  9364
     *
     * 注：本机器cpu为4核
     * @return
     */
    @Override
    public Map massTextingByThreadPoolExecutorAndLatch() {
        Map<String,Object> resultMap = new HashMap(2);
        //创建定长线程池
        ExecutorService executorService = Executors.newFixedThreadPool(8);
        //声明倒数计数器
        CountDownLatch latch = null;
        //要处理的用户
        List<User> users = null;
        long start = System.currentTimeMillis();
        try {
            users = this.findAllUser();
            if(users==null || users.size()==0){
                resultMap.put("isSuccess",false);
                resultMap.put("message","没有用户");
            }else{
                //通过构造器创建 user总数的通过CountDownLatch
                latch = new CountDownLatch(users.size());
                for (User user : users){
                    //循环执行。
                    executorService.submit(new MessageCallable(user,this,latch));
                }
                //主线程阻塞等待所有的子线程循环执行完毕users.size()的数量
                //如果，子线程中的CountDownLatch没有countDown。await 会一直等待，
                //当然也可以使用  latch(long timeout, TimeUnit unit)这个方法来规定阻塞多少时间。
                latch.await();
                resultMap.put("isSuccess",true);
                resultMap.put("message","发送成功");
            }
        }catch (Exception e){
            resultMap.put("isSuccess",false);
            resultMap.put("message","系统异常");
        }finally {

            System.out.println("耗时：["+ (System.currentTimeMillis()-start)+"]毫秒");
            return resultMap;
        }

    }

    @Override
    public Map massTextingByThreadPoolExecutorAndLatchC() {
        Map<String,Object> resultMap = new HashMap(2);
        //创建定长线程池
        ExecutorService executorService = Executors.newFixedThreadPool(8);
        CompletionService cs = new ExecutorCompletionService(executorService);
        //声明倒数计数器
        CountDownLatch latch = null;
        //要处理的用户
        List<User> users = null;
        long start = System.currentTimeMillis();
        try {
            users = this.findAllUser();
            if(users==null || users.size()==0){
                resultMap.put("isSuccess",false);
                resultMap.put("message","没有用户");
            }else{
                //通过构造器创建 user总数的通过CountDownLatch
                latch = new CountDownLatch(users.size());
                for (User user : users){
                    //循环执行。通过cs来获取结果
                    cs.submit(new MessageCallable(user,this,latch));
                }
                //主线程阻塞等待所有的子线程循环执行完毕users.size()的数量
                //如果，子线程中的CountDownLatch没有countDown。await 会一直等待，
                //当然也可以使用  latch(long timeout, TimeUnit unit)这个方法来规定阻塞多少时间。
                latch.await();
                for(int i=0;i<users.size();i++){
                    System.out.println(cs.take().get());
                }
                resultMap.put("isSuccess",true);
                resultMap.put("message","发送成功");
            }
        }catch (Exception e){
            resultMap.put("isSuccess",false);
            resultMap.put("message","系统异常");
        }finally {

            System.out.println("耗时：["+ (System.currentTimeMillis()-start)+"]毫秒");
            return resultMap;
        }

    }

    public Map massTextingByThreadPoolExecutorAndBarrier(){
        Map<String,Object> resultMap = new HashMap(2);
        List<User> users = this.findAllUser().subList(0,10);
        //创建定长线程池
        ExecutorService executorService = Executors.newFixedThreadPool(users.size());
        //创建cyclicBarrier，
        //cyclicBarrier 有两个构造器，
        // CyclicBarrier(int parties, Runnable barrierAction)这个构造器第一个参数是阻塞多少线程，第二个参数是所有子线程等待完毕要执行的线程
        CyclicBarrier cyclicBarrier = new CyclicBarrier(users.size(),()->
            System.out.println("在所有的子线程await之后执行")
        );
        for(User user:users){
            executorService.submit(new MessageCallableByBarrier(user,cyclicBarrier));
        }
        System.out.println("主线程不会阻塞");
        return resultMap;
    }


    /***************************************************************************************************************************************/

    @Override
    public Integer massTexting(List<User> users) throws Exception{
        MessageRecord messageRecord = new MessageRecord();
        Integer sum = 0;
        try {
            for (User user:users){
                messageRecord.setUserId(user.getId());
                messageRecord.setUserPhone(user.getPhone());
                messageRecord.setMessageContent(user.getName()+",你好，这是你的短信。");
                messageRecord.setCurrentTimeMillis(System.currentTimeMillis()+"");
                messageRecordMapper.insertSelective(messageRecord);
                sum = sum + user.getId();
                System.out.println("给用户id：["+user.getId()+"]发送短信。当前线程:["+Thread.currentThread().getName()+"]");
            }
        }catch (Exception e){
            e.printStackTrace();
            throw new Exception("发送短信异常");
        }finally {
            return sum;
        }


    }

    /**
     *
     * @param user
     * @throws Exception
     */
    @Override
    public void sendMessage(User user) throws Exception{
        MessageRecord messageRecord = new MessageRecord();
        try {
            messageRecord.setUserId(user.getId());
            messageRecord.setUserPhone(user.getPhone());
            messageRecord.setMessageContent(user.getName()+",你好，这是你的短信。");
            messageRecord.setCurrentTimeMillis(System.currentTimeMillis()+"");
            messageRecordMapper.insertSelective(messageRecord);
            System.out.println("给用户id：["+user.getId()+"]发送短信。当前线程:["+Thread.currentThread().getName()+"]");
        }catch (Exception e){
            throw new Exception("发送短信异常");
        }

    }

    @Override
    public void addUser(User user) {
        userMapper.insert(user);
    }

    @Override
    public List<User> findAllUser() {
        UserExample example = new UserExample();
        return userMapper.selectByExample(example);
    }
}
