package com.zodiac;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling   // 啟用排程（每日自動備份用）
public class ZodiacApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(ZodiacApiApplication.class, args);
    }
}
