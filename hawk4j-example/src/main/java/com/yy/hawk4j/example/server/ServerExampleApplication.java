package com.yy.hawk4j.example.server;

import com.yy.hawk4j.core.enable.EnableDynamicThreadPool;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableDynamicThreadPool
@SpringBootApplication(scanBasePackages = {"com.yy.hawk4j.example.core", "com.yy.hawk4j.example.server"})
public class ServerExampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServerExampleApplication.class, args);
    }

}
