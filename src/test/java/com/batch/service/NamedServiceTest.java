package com.batch.service;

import java.text.ParseException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;

@SpringBootTest
@Profile("company")
class NamedServiceTest {

    @Autowired
    private NamedService namedService;

    @Test
    void test() throws ParseException {
//        namedService.getBaseball();
        namedService.getBaseball("2022-06-21");
    }
}