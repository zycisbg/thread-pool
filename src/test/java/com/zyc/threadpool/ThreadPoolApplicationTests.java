package com.zyc.threadpool;

import com.zyc.threadpool.service.UserService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;


@RunWith(SpringRunner.class)
@SpringBootTest
public class ThreadPoolApplicationTests {

	@Autowired
	private UserService userService;

	@Test
	public void test1() {
        userService.massTextingByThreadPoolExecutorAndLatch();
	}

    @Test
    public void test2() {
        userService.massTextingByForkJoinPool();
    }

    @Test
    public void test3() throws Exception{
        Integer aLong = userService.massTexting(userService.findAllUser());
        System.out.println(aLong);
    }

    @Test
    public void test4(){
	    userService.massTextingByThreadPoolExecutorAndBarrier();
    }

    @Test
    public void test5() throws Exception{
	    userService.getUserAndMessageRecord();
    }

    @Test
    public void test6(){
	    userService.massTextingByParallelStream();
    }

    @Test
    public void test7(){
	    userService.massTextingByThreadPoolExecutorAndLatchC();
    }

}
