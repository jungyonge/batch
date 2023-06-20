package com.batch.controller.named.baseball;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Team {

    private int id;
    private String name;
    private String shortName;
    private String imgPath;
    private List<PeriodData> periodData;
    private int hitCount;
    private int errorCount;
    private int baseOnBallCount;
    private Player startPitcher;
    private SeasonRecord seasonRecord;
    // 각 필드에 대한 getter와 setter 필요
}
