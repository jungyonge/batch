package com.batch.service;

import com.batch.controller.named.baseball.NamedBaseballResponse;
import com.batch.controller.named.baseball.gamehistory.NamedGameHistoryResponse;
import com.batch.model.BaseballModel;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class NamedService {

    private final RestTemplate restTemplate;

    private final ModelMapper modelMapper = new ModelMapper();

    private static final String BASEBALL_URL = "https://sports-api.named.com/v1.0/sports/baseball/games?date=#matchDate&status=ALL";

    private static final String BASEBALL_PITCHER_URL = "https://sports-api.named.com/v1.0/sports/baseball/games/#gameId/lineup";

    public void getBaseball() {

        String reqUrl = BASEBALL_URL.replace("#matchDate", "2023-06-17");
        ResponseEntity<NamedBaseballResponse[]> res = restTemplate.getForEntity(reqUrl,
            NamedBaseballResponse[].class);

        NamedBaseballResponse[] resArr = res.getBody();
        BaseballModel baseballModel = modelMapper.map(resArr[0], BaseballModel.class);

        System.out.println(baseballModel);
    }

    public void getPitcher() {

        String reqUrl = BASEBALL_PITCHER_URL.replace("#gameId", "2023-06-17");

        ResponseEntity<NamedGameHistoryResponse[]> res = restTemplate.getForEntity(reqUrl,
            NamedGameHistoryResponse[].class);

        NamedGameHistoryResponse[] resArr = res.getBody();
        BaseballModel baseballModel = modelMapper.map(resArr[0], BaseballModel.class);

        System.out.println(baseballModel);
    }


}
