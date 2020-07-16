package com.batch.model;

import lombok.Data;

@Data
public class BaseballModel {

    String gameId;
    String league;
    String ground;
    String stadium;

    String date;
    String dayOfWeek;
    String time;

    Integer aTeamRestDay;
    String aTeamPitcher;
    String aTeam;
    Integer aTeamTotalPoint;
    Integer bTeamTotalPoint;
    String bTeam;
    String bTeamPitcher;
    Integer bTeamRestDay;

    Double handiCap;
    String handiCapResult;
    String odd;

    Double pointLine;
    String pointLineResult;

    Integer aTeamThirdPoint;
    Integer bTeamThirdPoint;

    Integer aTeamFourthPoint;
    Integer bTeamFourthPoint;

    Integer aTeamFifthPoint;
    Integer bTeamFifthPoint;

    Double thirdHandiCap;
    String thirdHandiCapResult;

    Double thirdPointLine;
    String thirdPointLineResult;

    Double fourthHandiCap;
    String fourthHandiCapResult;

    Double fourthPointLine;
    String fourthPointLineResult;

    Double fifthHandiCap;
    String fifthHandiCapResult;

    Double fifthPointLine;
    String fifthPointLineResult;

    String firstStrikeOut;
    String firstHomerun;
    String firstBaseOnBall;

    Integer firstScore;
    Integer secondScore;
    Integer thirdScore;
    Integer fourthScore;
    Integer fifthScore;
    Integer sixthScore;
    Integer seventhScore;
    Integer eighthScore;
    Integer ninthScore;
    Integer extendScore;

    Boolean extendYn;

    public int getTotalScore(){
        int total = firstScore +  secondScore + thirdScore +  fourthScore +   fifthScore + sixthScore + seventhScore + eighthScore + ninthScore + extendScore;
        return total;
    }

    public int get3InningScore(){
        int total = firstScore +  secondScore + thirdScore ;
        return total;
    }

    public int get4InningScore(){
        int total = firstScore +  secondScore + thirdScore +  fourthScore ;
        return total;
    }

    public int get5InningScore(){
        int total = firstScore +  secondScore + thirdScore +  fourthScore +   fifthScore ;
        return total;
    }
}
