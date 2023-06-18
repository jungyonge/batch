package com.batch.service;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

class NamedServiceTest {

    private final NamedService namedService = new NamedService(new RestTemplate());

    @Test
    void test() {
        namedService.getBaseball();
    }
}