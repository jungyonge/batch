package com.batch.service;

import com.batch.controller.named.baseball.NamedBaseballResponse;
import com.batch.controller.named.baseball.gamehistory.NamedGameHistoryResponse;
import java.text.ParseException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class NamedService {

    private final RestTemplate restTemplate;

    private static final String BASEBALL_URL = "https://sports-api.named.com/v1.0/sports/baseball/games?date=#matchDate&status=ALL";

    private static final String BASEBALL_PITCHER_URL = "https://sports-api.named.com/v1.0/sports/baseball/games/#gameId/lineup";

    public NamedBaseballResponse[] getBaseball(String matchDate) throws ParseException {

        String reqUrl = BASEBALL_URL.replace("#matchDate", matchDate);
        ResponseEntity<NamedBaseballResponse[]> res = restTemplate.getForEntity(reqUrl,
            NamedBaseballResponse[].class);

        return res.getBody();
    }

    public NamedGameHistoryResponse getPitcher(String gameId) {

        String reqUrl = BASEBALL_PITCHER_URL.replace("#gameId", gameId);

        ResponseEntity<NamedGameHistoryResponse> res = restTemplate.getForEntity(reqUrl,
            NamedGameHistoryResponse.class);

        return res.getBody();
    }


}
