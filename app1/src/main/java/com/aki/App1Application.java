package com.aki;

import com.alibaba.csp.sentinel.annotation.aspectj.SentinelResourceAspect;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

// 测试gitee提交1
@SpringBootApplication
// nacos 客户端
@EnableDiscoveryClient
@EnableFeignClients //feign
@Slf4j
public class App1Application {
    public static void main(String[] args) {
        log.info("测试日志");
        log.warn("测试日志");
        log.error("测试日志");
        ConfigurableApplicationContext run = SpringApplication.run(App1Application.class, args);
        String property = run.getEnvironment().getProperty("myParameter.value1");
        System.out.println("=================");
        System.out.println(property);
    }

    // 注解支持的配置Bean
    @Bean
    public SentinelResourceAspect sentinelResourceAspect() {
        return new SentinelResourceAspect();
    }
}
