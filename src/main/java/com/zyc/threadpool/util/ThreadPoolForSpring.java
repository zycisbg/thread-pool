package com.zyc.threadpool.util;

import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

/**
 * 先继承一个ThreadPoolTaskExecutor
 * 可以自己设置参数
 * 如果不设置的话，就用默认参数
 * private int corePoolSize = 1;
 * private int maxPoolSize = 2147483647;
 * private int keepAliveSeconds = 60;
 * private int queueCapacity = 2147483647;
 * private boolean allowCoreThreadTimeOut = false;
 * 跟cachedPool挺像的  具体含义上边有
 */
@Component
public class ThreadPoolForSpring extends ThreadPoolTaskExecutor {

}
