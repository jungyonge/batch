package com.batch;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = { "spring.config.location=classpath:application.yml" })
@SpringBootTest
class BatchApplicationTests {

    @Test
    void contextLoads() {
    }

}
