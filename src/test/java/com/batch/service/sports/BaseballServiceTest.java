package com.batch.service.sports;

import java.text.ParseException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class) //Junit5
@SpringBootTest
@ActiveProfiles("company") // 괄호 안에 실행 환경을 명시해준다.
class BaseballServiceTest {

    @Autowired
    private BaseballService baseballService;

    @Test
    void test() throws ParseException {
//        namedService.getBaseball();
        baseballService.convertBaseballModel();
    }

}