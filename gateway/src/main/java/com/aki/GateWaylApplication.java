package com.aki;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient  //可以是其他注册中心
public class GateWaylApplication {
    public static void main(String[] args) {
        SpringApplication.run(GateWaylApplication.class, args);
    }
}
