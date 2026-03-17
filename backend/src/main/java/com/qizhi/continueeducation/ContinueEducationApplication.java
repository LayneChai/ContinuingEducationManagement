package com.qizhi.continueeducation;

import com.qizhi.continueeducation.config.properties.DeepSeekProperties;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@MapperScan("com.qizhi.continueeducation.**.mapper")
@EnableConfigurationProperties(DeepSeekProperties.class)
@SpringBootApplication
public class ContinueEducationApplication {

    public static void main(String[] args) {
        SpringApplication.run(ContinueEducationApplication.class, args);
    }
}
