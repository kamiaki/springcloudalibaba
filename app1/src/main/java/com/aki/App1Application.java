package com.aki;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

// 测试gitee提交1
@SpringBootApplication
// nacos 客户端
@EnableDiscoveryClient
@EnableFeignClients //feign
public class App1Application {
    public static void main(String[] args) {
        SpringApplication.run(App1Application.class, args);
    }
}
