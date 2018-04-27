package com.zyc.threadpool;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
@MapperScan("com.zyc.threadpool.dao")
public class ThreadPoolApplication {

	public static void main(String[] args) {
		SpringApplication.run(ThreadPoolApplication.class, args);
	}
}
