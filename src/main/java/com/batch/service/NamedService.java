package com.batch.service;

import lombok.RequiredArgsConstructor;
import org.springframework.web.client.RestTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NamedService {

    private final RestTemplate restTemplate;



}
