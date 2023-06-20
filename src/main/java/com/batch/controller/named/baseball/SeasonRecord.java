package com.batch.controller.named.baseball;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SeasonRecord {

    private int ranking;
    private int gameCount;
    private int winCount;
    private int defeatCount;
    private int drawCount;
    private String winPercentage;
    private String streak;
    private String performance;
    // 각 필드에 대한 getter와 setter 필요
}
