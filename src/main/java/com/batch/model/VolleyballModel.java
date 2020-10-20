package com.batch.model;

import lombok.Data;

@Data
public class VolleyballModel {

    String gameId;
    String league;
    String ground;

    String date;
    String dayOfWeek;
    String time;

    Integer aTeamRestDay;
    String aTeam;
    Integer aTeamSetScore;
    Integer aTeamTotalPoint;
    Integer bTeamTotalPoint;
    Integer bTeamSetScore;
    String bTeam;
    Integer bTeamRestDay;

    Double handiCap;
    String handiCapResult;
    String odd;

    Double setHandiCap;
    String setHandiCapResult;

    Double pointLine;
    String pointLineResult;

    Integer aTeamFirstQPoint;
    Integer bTeamFirstQPoint;

    Double firstQHandiCap;
    String firstQHandiCapResult;
    String firstQResult;
    Double firstQPointLine;
    String firstQPointLineResult;

    Integer aTeamSecondQPoint;
    Integer bTeamSecondQPoint;
    String secondQResult;
    String secondQHandiCapResult;
    String secondQPointLineResult;

    Integer aTeamThirdQPoint;
    Integer bTeamThirdQPoint;
    String thirdQResult;
    String thirdQHandiCapResult;
    String thirdQPointLineResult;

    Integer aTeamFourthQPoint;
    Integer bTeamFourthQPoint;
    String fourthQResult;
    String fourthQHandiCapResult;
    String fourthQPointLineResult;

    Integer aTeamFifthQPoint;
    Integer bTeamFifthQPoint;
    String fifthQResult;
    String fifthQHandiCapResult;
    String fifthQPointLineResult;

    String firstPoint;
    String firstBlock;
    String firstServe;

    String first5Point;
    String first7Point;
    String first10Point;

    Integer firstQPoint;
    Integer secondQPoint;
    Integer thirdQPoint;
    Integer fourthQPoint;
    Integer fifthQPoint;

    Integer firstQTotalPoint;
    Integer secondQTotalPoint;
    Integer thirdQTotalPoint;
    Integer fourthQTotalPoint;
    Integer fifthQTotalPoint;


    public int getTotalPoint(){
        int total = firstQPoint +  secondQPoint + thirdQPoint +  fourthQPoint +   fifthQPoint;
        return total;
    }
}
