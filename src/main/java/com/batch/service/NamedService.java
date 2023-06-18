package com.batch.service;

import com.batch.controller.named.NamedBaseballResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class NamedService {

    private final RestTemplate restTemplate;


    public void getBaseball() {

        ResponseEntity<NamedBaseballResponse[]> res = restTemplate.getForEntity(
            "https://sports-api.named.com/v1.0/sports/baseball/games?date=2023-06-17&status=ALL",
            NamedBaseballResponse[].class);

        NamedBaseballResponse[] resArr = res.getBody();

        System.out.println(resArr);
    }



}
