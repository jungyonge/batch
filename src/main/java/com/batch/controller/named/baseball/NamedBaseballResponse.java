package com.batch.controller.named.baseball;


import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class NamedBaseballResponse {

    private int id;
    private String sportsType;
    private League league;
    private Season season;
    private String startDatetime;
    private String realStartDateTime;
    private String venueName;
    private String gameStatus;
    private String result;
    private Broadcast broadcast;
    private int period;
    private String displayTime;
    private String videoLink;
    private boolean hasLineup;
    private boolean oddsFlag;
    private boolean boardFlag;
    private Teams teams;
    private String inningDivision;
    private boolean firstBaseOccupied;
    private boolean secondBaseOccupied;
    private boolean thirdBaseOccupied;
    private int strike;
    private int out;
    private int ball;
    private Player currentPitcher;
    private Player currentBatter;
    private Special special;
    private RepresentativeOdds representativeOdds;
    private Odds odds;
    private String betbleUri;
    private boolean betbleLiveDisableFlag;
    private boolean hasManualRelay;
    private int cheerCount;
    private int viewCount;
    private int analysisCount;
    private boolean majorHot;
    private boolean hot;
    // 각 필드에 대한 getter와 setter 필요
}


