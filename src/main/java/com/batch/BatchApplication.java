package com.batch;

import com.ulisesbocchio.jasyptspringbootstarter.JasyptSpringBootAutoConfiguration;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableBatchProcessing
//@EnableScheduling
@SpringBootApplication(scanBasePackages = "com.batch", exclude = JasyptSpringBootAutoConfiguration.class)
public class BatchApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(BatchApplication.class, args);
    }

}
