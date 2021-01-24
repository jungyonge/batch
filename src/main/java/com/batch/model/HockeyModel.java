package com.batch.model;

import lombok.Data;

@Data
public class HockeyModel {

    String gameId;
    String league;
    String ground;
    String date;
    String dayOfWeek;
    String time;

    Integer aTeamRestDay;
    String aTeam;
    Integer aTeamTotalPoint;
    Integer bTeamTotalPoint;
    String bTeam;
    Integer bTeamRestDay;

    Double handiCap;
    String handiCapResult;
    String odd;


    Double pointLine;
    String pointLineResult;
    Integer aTeamFirstQPoint;
    Integer bTeamFirstQPoint;
    Double firstQHandiCap;
    String firstQHandiCapResult;
    Double firstQPointLine;
    String firstQPointLineResult;
    String firstPoint;
    Integer firstQPoint;
    Integer secondQPoint;
    Integer thirdQPoint;
    Integer extendQPoint;
    Integer shotoutQPoint;
    Integer firstQTotalPoint;
    Integer secondQTotalPoint;
    Integer thirdQTotalPoint;
    Integer extendQTotalPoint;
    Integer shotoutQTotalPoint;
}
